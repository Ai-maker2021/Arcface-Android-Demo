package com.arcsoft.arcfacedemo.util.camera.glsurface;

import android.opengl.GLES20;
import android.util.Log;

import java.nio.IntBuffer;

public class GLUtil {
    private static final String TAG = "GLUtil";


    /**
     * 显示的顶点
     */
    static final float[] SQUARE_VERTICES = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            -1.0f, 1.0f,
            1.0f, 1.0f
    };
    /**
     * 原数据显示
     * 0,1***********1,1
     * *             *
     * *             *
     * *             *
     * *             *
     * *             *
     * 0,0***********1,0
     */
    static final float[] COORD_VERTICES = {
            0.0f, 1.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 0.0f
    };

    /**
     * 逆时针旋转90度显示
     * 1,1***********1,0
     * *             *
     * *             *
     * *             *
     * *             *
     * *             *
     * 0,1***********0,0
     */
    static final float[] ROTATE_90_COORD_VERTICES = {
            1.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            0.0f, 0.0f
    };

    /**
     * 逆时针旋转180度显示
     * 1,0***********0,0
     * *             *
     * *             *
     * *             *
     * *             *
     * *             *
     * 1，1***********0,1
     */
    static final float[] ROTATE_180_COORD_VERTICES = {
            1.0f, 0.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f
    };

    /**
     * 逆时针旋转270度显示
     * 0,0***********0,1
     * *             *
     * *             *
     * *             *
     * *             *
     * *             *
     * 1,0***********1,1
     */
    static final float[] ROTATE_270_COORD_VERTICES = {
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 1.0f
    };

    /**
     * 镜像显示
     * 1,1***********0,1
     * *             *
     * *             *
     * *             *
     * *             *
     * *             *
     * 1,0***********0,0
     */
    static final float[] MIRROR_COORD_VERTICES = {
            1.0f, 1.0f,
            0.0f, 1.0f,
            1.0f, 0.0f,
            0.0f, 0.0f
    };

    /**
     * 镜像并逆时针旋转90度显示
     * 0,1***********0,0
     * *             *
     * *             *
     * *             *
     * *             *
     * *             *
     * 1,1***********1,0
     */
    static final float[] ROTATE_90_MIRROR_COORD_VERTICES = {
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f
    };
    /**
     * 镜像并逆时针旋转180度显示
     * 0,0***********1,0
     * *             *
     * *             *
     * *             *
     * *             *
     * *             *
     * 0,1***********1,1
     */
    static final float[] ROTATE_180_MIRROR_COORD_VERTICES = {
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f
    };
    /**
     * 镜像并逆时针旋转270度显示
     * 1,0***********1,1
     * *             *
     * *             *
     * *             *
     * *             *
     * *             *
     * 0,0***********0,1
     */
    static final float[] ROTATE_270_MIRROR_COORD_VERTICES = {
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 0.0f,
            0.0f, 1.0f
    };

    /**
     * 创建OpenGL Program，并链接
     *
     * @param fragmentShaderCode 片段着色器代码
     * @param vertexShaderCode   顶点着色器代码
     * @return OpenGL Program
     */
    static int createShaderProgram(String fragmentShaderCode, String vertexShaderCode) {
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);
        if (vertexShader == 0 || fragmentShader == 0) {
            return 0;
        }
        int mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(mProgram, vertexShader);
        GLES20.glAttachShader(mProgram, fragmentShader);
        GLES20.glLinkProgram(mProgram);

        IntBuffer linked = IntBuffer.allocate(1);
        GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linked);
        if (linked.get(0) == 0) {
            return 0;
        }
        return mProgram;
    }

    /**
     * 加载着色器
     *
     * @param shaderType 着色器类型，可以是片段着色器{@link GLES20#GL_FRAGMENT_SHADER}或顶点着色器{@link GLES20#GL_VERTEX_SHADER}
     * @param source     着色器代码
     * @return 着色器对象的引用，0代表失败
     */
    static int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader == 0) {
            Log.e(TAG, "loadShader: failed to create shader");
            checkGlErrorIfOccur("create shader " + shaderType);
            return 0;
        }
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader " + shaderType + ":" + GLES20.glGetShaderInfoLog(shader));
            GLES20.glDeleteShader(shader);
            shader = 0;
            checkGlErrorIfOccur("glGetShaderiv " + shaderType);
        }
        return shader;
    }

    /**
     * 检查是否出现GLES错误
     */
    private static void checkGlErrorIfOccur(String op) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            String errorMsg = String.format("error 0x%h occurred: %s", error, op);
            Log.e(TAG, errorMsg);
            throw new RuntimeException(errorMsg);
        }
    }

    /**
     * 根据是否镜像和旋转角度选择合适的顶点坐标
     *
     * @param isMirror     是否镜像
     * @param rotateDegree 旋转角度
     * @return 顶点坐标
     */
    static float[] getCoordVerticesByPreviewParams(boolean isMirror, int rotateDegree) {
        float[] coordVertice = GLUtil.COORD_VERTICES;
        if (isMirror) {
            switch (rotateDegree) {
                case 0:
                    coordVertice = GLUtil.MIRROR_COORD_VERTICES;
                    break;
                case 90:
                    coordVertice = GLUtil.ROTATE_90_MIRROR_COORD_VERTICES;
                    break;
                case 180:
                    coordVertice = GLUtil.ROTATE_180_MIRROR_COORD_VERTICES;
                    break;
                case 270:
                    coordVertice = GLUtil.ROTATE_270_MIRROR_COORD_VERTICES;
                    break;
                default:
                    break;
            }
        } else {
            switch (rotateDegree) {
                case 0:
                    coordVertice = GLUtil.COORD_VERTICES;
                    break;
                case 90:
                    coordVertice = GLUtil.ROTATE_90_COORD_VERTICES;
                    break;
                case 180:
                    coordVertice = GLUtil.ROTATE_180_COORD_VERTICES;
                    break;
                case 270:
                    coordVertice = GLUtil.ROTATE_270_COORD_VERTICES;
                    break;
                default:
                    break;
            }
        }
        return coordVertice.clone();
    }
}
