package com.arcsoft.arcfacedemo.util.camera.glsurface;

import android.content.Context;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class CameraGLSurfaceView extends GLSurfaceView {
    private static final String TAG = "CameraGLSurfaceView";


    YUVRenderer yuvRenderer;
    NV21Drawer nv21Drawer;

    public CameraGLSurfaceView(Context context) {
        this(context, null);
    }

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEGLContextClientVersion(2);
        // 设置Renderer到GLSurfaceView
        yuvRenderer = new YUVRenderer();
        nv21Drawer = new NV21Drawer();
        setRenderer(yuvRenderer);
        // 只有在绘制数据改变时才绘制view
        setRenderMode(RENDERMODE_WHEN_DIRTY);
    }

    /**
     * 设置不同的片段着色器代码以达到不同的预览效果
     *
     * @param fragmentShaderCode 片段着色器代码
     */
    public void setFragmentShaderCode(String fragmentShaderCode) {
        nv21Drawer.setFragmentShaderCode(fragmentShaderCode);
    }

    public void init(boolean isMirror, int rotateDegree, int frameWidth, int frameHeight) {
        nv21Drawer.init(isMirror, rotateDegree, frameWidth, frameHeight);

        queueEvent(() -> yuvRenderer.initRenderer());
    }

    public class YUVRenderer implements Renderer {
        private void initRenderer() {
            boolean createSuccess = nv21Drawer.createGLProgram();
            if (!createSuccess) {
                Log.e(TAG, "initRenderer createGLProgram failed!");
            }
        }

        @Override
        public void onSurfaceCreated(GL10 unused, EGLConfig config) {
            Log.i(TAG, "initRenderer onSurfaceCreated: ");
            initRenderer();
        }


        @Override
        public void onDrawFrame(GL10 gl) {
            nv21Drawer.render();
        }

        @Override
        public void onSurfaceChanged(GL10 unused, int width, int height) {
            Log.i(TAG, "onSurfaceChanged: ");
            GLES20.glViewport(0, 0, width, height);
        }
    }

    /**
     * 传入NV21刷新帧
     *
     * @param data NV21数据
     */
    public void renderNV21(byte[] data) {
        nv21Drawer.updateNV21(data);
        requestRender();
    }


    /**
     * 传入NV21刷新帧，并同时绘制人脸框
     *
     * @param data     NV21数据
     * @param faceRect 人脸框
     */
    public void renderNV21WithFaceRect(byte[] data, Rect faceRect, int strokeWidth) {
        nv21Drawer.updateNV21(data, faceRect, strokeWidth);
        requestRender();
    }
}
