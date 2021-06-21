package com.arcsoft.arcfacedemo.util.debug;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Rect;

import com.arcsoft.arcfacedemo.ArcFaceApplication;
import com.arcsoft.arcfacedemo.facedb.entity.FaceEntity;
import com.arcsoft.arcfacedemo.util.ConfigUtil;
import com.arcsoft.arcfacedemo.util.FileUtil;
import com.arcsoft.arcfacedemo.util.debug.model.BasicInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.VersionInfo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.concurrent.Executor;

public class DebugInfoDumper {
    /**
     * 存储检测不到人脸的时长
     */
    public static final int DEFAULT_DUMP_FACE_TRACK_DATA_DURATION = 15;

    /**
     * 数据存储目录
     */
    private static String LOG_ROOT_DIR;
    /**
     * crash日志存放目录
     */
    public static String CRASH_LOG_DIR;

    /**
     * 当前的数据存储目录，每次打开debug界面后确定，按时间存储
     */
    private String currentDumpDir;

    /**
     * 每次打开时存储一些基础信息
     */
    public static String basicInfoFilePath;

    static {
        LOG_ROOT_DIR = ArcFaceApplication.getApplication().getExternalFilesDir("debugDump").toString();
        CRASH_LOG_DIR = ArcFaceApplication.getApplication().getExternalFilesDir("crashLog").toString();
    }

    private static volatile DebugInfoDumper instance = null;

    private DebugInfoDumper() {
    }

    public static DebugInfoDumper getInstance() {
        if (instance == null) {
            synchronized (DebugInfoDumper.class) {
                if (instance == null) {
                    instance = new DebugInfoDumper();
                }
            }
        }
        return instance;
    }

    public String getCurrentDumpDir() {
        return currentDumpDir;
    }

    public static String getBasicInfoFilePath() {
        return basicInfoFilePath;
    }

    public void init() {
        currentDumpDir = LOG_ROOT_DIR + File.separator + new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(System.currentTimeMillis());
        basicInfoFilePath = currentDumpDir + File.separator + "basicInfo.txt";
    }

    public static final int ERROR_TYPE_FACE_TRACK = 1;
    public static final int ERROR_TYPE_FACE_LIVENESS = 2;
    public static final int ERROR_TYPE_FEATURE_EXTRACT = 3;
    public static final int ERROR_TYPE_FEATURE_COMPARE = 4;

    public static String getDirNameByErrorType(int errType) {
        switch (errType) {
            case ERROR_TYPE_FACE_TRACK:
                return "ftError";
            case ERROR_TYPE_FACE_LIVENESS:
                return "flError";
            case ERROR_TYPE_FEATURE_EXTRACT:
                return "extractError";
            case ERROR_TYPE_FEATURE_COMPARE:
                return "compareError";
            default:
                return "unknown";
        }
    }

    /**
     * 检测不到的人脸的图像，命名为：
     * {width}x{height}_{timestamp}_{ERROR_TYPE}_{ftCode}.NV21
     */
    private static final String NAME_FORMAT_FT_ERROR_FILE = "%dx%d_%d_%d_%d.NV21";
    /**
     * 活体检测结果，命名为：
     * {width}x{height}_{timestamp}_{ERROR_TYPE}_{faceId}_{livenessCode}_{rect(left-top-right-bottom)}_{orient}_{livenessType(rgb/ir)}_{liveness}_{livenessCost}.NV21
     */
    private static final String NAME_FORMAT_FL_ERROR_FILE = "%dx%d_%d_%d_%d_%d_(%d,%d,%d,%d)_%d_%d_%d_%d.NV21";

    /**
     * 特征提取失败的图像，命名为：
     * {width}x{height}_{timestamp}_{ERROR_TYPE}_{faceId}_{extractCode}_{rect(left-top-right-bottom)}_{orient}_{fqCost}_{extractCost}.NV21
     */
    private static final String NAME_FORMAT_EXTRACT_ERROR_FILE = "%dx%d_%d_%d_%d_%d_(%d,%d,%d,%d)_%d_%d_%d.NV21";

    /**
     * 识别未通过的图像，命名为：
     * {width}x{height}_{timestamp}_{ERROR_TYPE}_{faceId}_{compareCode}_{rect(left-top-right-bottom)}_{orient}_{similar}_{feature1-fileName}_{feature2-fileName}.NV21
     */
    private static final String NAME_FORMAT_COMPARE_ERROR_FILE = "%dx%d_%d_%d_%d_%d_(%d,%d,%d,%d)_%d_%.6f_%s_%s.NV21";


    public static String getFaceTrackErrorFileName(int width, int height, int ftCode) {
        return String.format(Locale.ENGLISH, NAME_FORMAT_FT_ERROR_FILE, width, height, System.currentTimeMillis(), ERROR_TYPE_FACE_TRACK, ftCode);
    }

    public static String getFaceLivenessFileName(int width, int height, int trackId, int livenessCode, FaceInfo faceInfo, int livenessType, int liveness, long livenessCost) {
        Rect rect = faceInfo.getRect();
        return String.format(Locale.ENGLISH, NAME_FORMAT_FL_ERROR_FILE, width, height, System.currentTimeMillis(),
                ERROR_TYPE_FACE_LIVENESS, trackId, livenessCode, rect.left, rect.top, rect.right, rect.bottom,
                faceInfo.getOrient(), livenessType, liveness, livenessCost);
    }

    public static String getExtractFailedFileName(int width, int height, int trackId, int extractCode, FaceInfo faceInfo, long extractCost, long fqCost) {
        Rect rect = faceInfo.getRect();
        return String.format(Locale.ENGLISH, NAME_FORMAT_EXTRACT_ERROR_FILE, width, height, System.currentTimeMillis(),
                ERROR_TYPE_FEATURE_EXTRACT, trackId, extractCode, rect.left, rect.top, rect.right, rect.bottom,
                faceInfo.getOrient(), fqCost, extractCost);
    }

    public static String getCompareFailedFileName(int width, int height, int trackId, int compareCode, FaceInfo faceInfo, float similar, String recognizeFeatureFileName, String registerFeatureFileName) {
        Rect rect = faceInfo.getRect();
        return String.format(Locale.ENGLISH, NAME_FORMAT_COMPARE_ERROR_FILE, width, height, System.currentTimeMillis(),
                ERROR_TYPE_FEATURE_COMPARE, trackId, compareCode, rect.left, rect.top, rect.right, rect.bottom,
                faceInfo.getOrient(), similar, recognizeFeatureFileName, registerFeatureFileName);
    }

    public static void saveNormalData(int errorType, String dir, String fileName, byte[] data, Executor executor) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                File parentFile = new File(dir + File.separator + getDirNameByErrorType(errorType));
                if (!parentFile.exists() && !parentFile.mkdirs()) {
                    return;
                }
                File file = new File(parentFile, fileName);
                FileUtil.saveDataToFile(data, file);
            }
        });
    }

    public static void savePerformanceData(String dir, String fileName, String performanceInfo, Executor executor) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                File parentFile = new File(dir);
                if (!parentFile.exists() && !parentFile.mkdirs()) {
                    return;
                }
                FileUtil.saveDataToFile((performanceInfo + "\r\n").getBytes(), new File(parentFile, fileName), true);
            }
        });
    }

    public static void saveCompareFailedData(String dir, String fileName, byte[] nv21, String recognizeFeatureName, String registerFeatureName, byte[] recognizeFaceFeature, FaceEntity faceEntity, Executor executor) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                File parentFile = new File(dir + File.separator + getDirNameByErrorType(DebugInfoDumper.ERROR_TYPE_FEATURE_COMPARE) + File.separator + System.currentTimeMillis());
                if (!parentFile.exists() && !parentFile.mkdirs()) {
                    return;
                }
                // 识别的特征
                FileUtil.saveDataToFile(recognizeFaceFeature, new File(parentFile, recognizeFeatureName));
                // 注册的特征
                FileUtil.saveDataToFile(faceEntity.getFeatureData(), new File(parentFile, registerFeatureName));
                // nv21
                FileUtil.saveDataToFile(nv21, new File(parentFile, fileName));
                // jpg
                byte[] jpeg = FileUtil.fileToData(new File(faceEntity.getImagePath()));
                if (jpeg != null) {
                    FileUtil.saveDataToFile(jpeg, new File(parentFile, registerFeatureName + ".jpg"));
                }
            }
        });
    }

    public static String getBasicInfo() {
        Context context = ArcFaceApplication.getApplication().getApplicationContext();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        String memory = "unknown";
        if (activityManager != null) {
            ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
            activityManager.getMemoryInfo(memoryInfo);
            memory = memoryInfo.totalMem / 1024 / 1024 + "MB";
        }
        VersionInfo versionInfo = new VersionInfo();
        FaceEngine.getVersion(versionInfo);
        BasicInfo basicInfo = new BasicInfo(DeviceInfoUtil.getCpuModel(), memory, ConfigUtil.getAppId(context), ConfigUtil.getSdkKey(context), ConfigUtil.getActiveKey(context), versionInfo.getVersion());
        return basicInfo.getBasicInfoString();
    }

}
