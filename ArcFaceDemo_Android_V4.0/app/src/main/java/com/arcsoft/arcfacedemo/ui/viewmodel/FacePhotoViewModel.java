package com.arcsoft.arcfacedemo.ui.viewmodel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.arcsoft.arcfacedemo.ArcFaceApplication;
import com.arcsoft.arcfacedemo.R;
import com.arcsoft.arcfacedemo.data.FaceRepository;
import com.arcsoft.arcfacedemo.facedb.FaceDatabase;
import com.arcsoft.arcfacedemo.facedb.dao.FaceDao;
import com.arcsoft.arcfacedemo.facedb.entity.FaceEntity;
import com.arcsoft.arcfacedemo.faceserver.FaceServer;
import com.arcsoft.arcfacedemo.ui.callback.BatchRegisterCallback;
import com.arcsoft.arcfacedemo.ui.callback.OnRegisterFinishedCallback;
import com.arcsoft.arcfacedemo.util.FileUtil;
import com.arcsoft.imageutil.ArcSoftImageFormat;
import com.arcsoft.imageutil.ArcSoftImageUtil;
import com.arcsoft.imageutil.ArcSoftImageUtilError;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class FacePhotoViewModel extends ViewModel {
    private static final String TAG = "FacePhotoViewModel";
    private static final String SUFFIX_JPEG = ".jpeg";
    private static final String SUFFIX_JPG = ".jpg";
    private static final String SUFFIX_PNG = ".png";

    private MutableLiveData<List<FaceEntity>> faceEntityList = new MutableLiveData<>();
    private MutableLiveData<Integer> totalFaceCount = new MutableLiveData<>();
    private MutableLiveData<Boolean> initFinished = new MutableLiveData<>();
    private FaceDao faceDao;
    private FaceRepository faceRepository;
    private static final int PAGE_SIZE = 20;
    private static final int VISIBLE_THRESHOLD = 5;
    private Disposable disposable;

    // 总数
    private int faceCount = -1;

    public MutableLiveData<Boolean> getInitFinished() {
        return initFinished;
    }

    public MutableLiveData<List<FaceEntity>> getFaceEntityList() {
        return faceEntityList;
    }

    public MutableLiveData<Integer> getTotalFaceCount() {
        return totalFaceCount;
    }

    public FacePhotoViewModel() {
        faceDao = FaceDatabase.getInstance(ArcFaceApplication.getApplication()).faceDao();
    }


    public void deleteFace(FaceEntity faceEntity) {
        if (faceEntityList.getValue() != null) {
            faceEntityList.getValue().remove(faceEntity);
        }
        FaceServer.getInstance().removeOneFace(faceEntity);
        faceRepository.delete(faceEntity);
        decreaseFaceCount();
        faceEntityList.postValue(faceEntityList.getValue());
    }


    public void updateFace(int position, FaceEntity faceEntity) {
        faceDao.updateFaceEntity(faceEntity);
        List<FaceEntity> faceEntityList = getFaceEntityList().getValue();
        if (faceEntityList != null) {
            faceEntityList.set(position, faceEntity);
            getFaceEntityList().postValue(faceEntityList);
        }
    }

    /**
     * 加载数据
     *
     * @param reload true：重新加载 ， false：分页加载
     */
    public synchronized void loadData(boolean reload) {
        if (faceCount == -1 || reload) {
            faceCount = faceRepository.getTotalFaceCount();
            totalFaceCount.postValue(faceCount);
        }
        List<FaceEntity> faceEntityList = getFaceEntityList().getValue();
        if (faceEntityList == null) {
            faceEntityList = new LinkedList<>();
        }
        List<FaceEntity> faceEntities = reload ? faceRepository.reload() : faceRepository.loadMore();
        if (reload) {
            faceEntityList.clear();
        }
        faceEntityList.addAll(faceEntities);
        getFaceEntityList().postValue(faceEntityList);
    }


    public void listScrolled(int lastVisibleItem, int totalItemCount) {
        if (lastVisibleItem + VISIBLE_THRESHOLD >= totalItemCount) {
            loadData(false);
        }
    }

    public void registerFace(Bitmap bitmap, OnRegisterFinishedCallback callback) {
        Bitmap alignedBitmap = ArcSoftImageUtil.getAlignedBitmap(bitmap, true);
        Observable.create((ObservableOnSubscribe<byte[]>) emitter -> {
            byte[] bgr24Data = ArcSoftImageUtil.createImageData(alignedBitmap.getWidth(), alignedBitmap.getHeight(), ArcSoftImageFormat.BGR24);
            int transformCode = ArcSoftImageUtil.bitmapToImageData(alignedBitmap, bgr24Data, ArcSoftImageFormat.BGR24);
            if (transformCode == ArcSoftImageUtilError.CODE_SUCCESS) {
                emitter.onNext(bgr24Data);
            } else {
                emitter.onError(new Exception("transform failed, code is " + transformCode));
            }
        })
                .flatMap((Function<byte[], ObservableSource<FaceEntity>>) bgr24Data -> {
                    Observable<FaceEntity> faceEntityObservable = Observable.just(faceRepository.registerBgr24(
                            ArcFaceApplication.getApplication(), bgr24Data,
                            alignedBitmap.getWidth(), alignedBitmap.getHeight(),
                            String.valueOf(System.currentTimeMillis())));
                    loadData(true);
                    // 注册成功时，数据也同步更新下
                    return faceEntityObservable;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<FaceEntity>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(FaceEntity faceEntity) {
                        if (faceEntity != null) {
                            callback.onRegisterFinished(null, true);
                        } else {
                            callback.onRegisterFinished(null, false);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.onRegisterFinished(null, false);
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    @SuppressLint("CheckResult")
    public void clearAllFaces() {
        Observable.create((ObservableOnSubscribe<Integer>) emitter -> emitter.onNext(faceRepository.clearAll()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    getFaceEntityList().postValue(new LinkedList<>());
                    getTotalFaceCount().postValue(0);
                });
    }

    @SuppressLint("CheckResult")
    public void registerFromFile(Context context, File dir, BatchRegisterCallback callback) {
        if (!dir.exists()) {
            callback.onFinish(0, 0, 0, context.getString(R.string.please_put_photos, dir.getAbsolutePath()));
            return;
        }
        File[] files = dir.listFiles((dir1, name) -> {
            String nameLowerCase = name.toLowerCase();
            return nameLowerCase.endsWith(SUFFIX_JPG) || nameLowerCase.endsWith(SUFFIX_JPEG) || nameLowerCase.endsWith(SUFFIX_PNG);
        });

        if (files == null || files.length == 0) {
            callback.onFinish(0, 0, 0, context.getString(R.string.please_put_photos, dir.getAbsolutePath()));
            return;
        }

        int total = files.length;
        final int[] failed = {0};
        final int[] success = {0};
        Observable.fromArray(files)
                .flatMap((Function<File, ObservableSource<Boolean>>) file -> {
                    byte[] bytes = FileUtil.fileToData(file);
                    String name = file.getName();
                    int suffixIndex = name.indexOf(".");
                    if (suffixIndex > 0) {
                        name = name.substring(0, suffixIndex);
                    }
                    FaceEntity faceEntity = null;
                    faceEntity = faceRepository.registerJpeg(context, bytes, name);
                    success[0]++;
                    if (faceEntity == null) {
                        failed[0]++;
                    } else {
                        increaseFaceCount();
                    }
                    FaceEntity finalFaceEntity = faceEntity;
                    return observer -> observer.onNext(finalFaceEntity == null);
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable = d;
                    }

                    @Override
                    public void onNext(Boolean res) {
                        int succeedSize = success[0];
                        int failedSize = failed[0];
                        if (total == succeedSize + failedSize) {
                            callback.onFinish(success[0], failed[0], total, null);
                        } else {
                            callback.onProcess(success[0], failed[0], total);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        callback.onFinish(success[0], failed[0], total, e.getMessage());
                        disposable.dispose();
                    }

                    @Override
                    public void onComplete() {

                    }
                });

    }

    private void increaseFaceCount() {
        synchronized (this) {
            Log.i(TAG, "increaseFaceCount: " + faceCount);
            getTotalFaceCount().postValue(++faceCount);
        }
    }

    private void decreaseFaceCount() {
        synchronized (this) {
            Log.i(TAG, "decreaseFaceCount: " + faceCount);
            getTotalFaceCount().postValue(--faceCount);
        }
    }

    /**
     * 停止注册
     *
     * @return 是否停止成功
     */
    public boolean stopRegisterIfDoing() {
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
            disposable = null;
            return true;
        }
        return false;
    }

    public void init() {
        if (faceRepository == null) {
            FaceServer instance = FaceServer.getInstance();
            instance.init(ArcFaceApplication.getApplication().getApplicationContext(), new FaceServer.OnInitFinishedCallback() {
                @Override
                public void onFinished(int faceCount) {
                    initFinished.postValue(true);
                }
            });
            faceRepository = new FaceRepository(PAGE_SIZE, faceDao, instance);
        }
    }

    public void release() {
        stopRegisterIfDoing();
        FaceServer.getInstance().release();
    }

}
