package com.arcsoft.arcfacedemo.data;

import android.content.Context;
import android.util.Log;

import com.arcsoft.arcfacedemo.facedb.dao.FaceDao;
import com.arcsoft.arcfacedemo.facedb.entity.FaceEntity;
import com.arcsoft.arcfacedemo.faceserver.FaceServer;
import com.arcsoft.arcfacedemo.faceserver.RegisterFailedException;

import java.io.File;
import java.util.List;

public class FaceRepository {
    private FaceDao faceDao;
    private int currentIndex = 0;
    private int pageSize;
    private static final String TAG = "FaceRepository";
    private FaceServer faceServer;

    public FaceRepository(int pageSize, FaceDao faceDao, FaceServer faceServer) {
        this.pageSize = pageSize;
        this.faceDao = faceDao;
        this.faceServer = faceServer;
    }

    public List<FaceEntity> loadMore() {
        List<FaceEntity> faceEntities = faceDao.getFaces(currentIndex, pageSize);
        currentIndex += faceEntities.size();
        return faceEntities;
    }

    public List<FaceEntity> reload() {
        currentIndex = 0;
        return loadMore();
    }

    public int clearAll() {
        // 由于涉及到文件删除操作，所以使用faceServer
        int faceCount = faceServer.clearAllFaces();
        currentIndex = 0;
        return faceCount;
    }

    public int delete(FaceEntity faceEntity) {
        int index = faceDao.deleteFace(faceEntity);
        boolean delete = new File(faceEntity.getImagePath()).delete();
        if (!delete) {
            Log.w(TAG, "deleteFace: failed to delete headImageFile '" + faceEntity.getImagePath() + "'");
        }
        return index;
    }


    public FaceEntity registerJpeg(Context context, byte[] bytes, String name) throws RegisterFailedException {
        return faceServer.registerJpeg(context, bytes, name);
    }

    public FaceEntity registerBgr24(Context context, byte[] bgr24Data, int width, int height, String name) {
        return faceServer.registerBgr24(context, bgr24Data, width, height, name);
    }

    public int getTotalFaceCount() {
        return faceDao.getFaceCount();
    }
}
