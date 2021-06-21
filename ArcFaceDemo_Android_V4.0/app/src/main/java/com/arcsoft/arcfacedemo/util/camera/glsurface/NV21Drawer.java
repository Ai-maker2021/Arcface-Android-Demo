package com.arcsoft.arcfacedemo.util.camera.glsurface;

import android.graphics.Color;
import android.graphics.Rect;
import android.opengl.GLES20;
import android.util.Log;

import com.arcsoft.arcfacedemo.util.ImageUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

/**
 * 用于绘制NV21数据的封装类
 */
public class NV21Drawer {
    private static final String TAG = "NV21Drawer";


    // SQUARE_VERTICES每2个值作为一个顶点
    private static final int COUNT_PER_SQUARE_VERTICE = 2;
    // COORD_VERTICES每2个值作为一个顶点
    private static final int COUNT_PER_COORD_VERTICES = 2;
    // 一个FLOAT占4个字节，用于分配内存时的计算
    private static final int FLOAT_SIZE_BYTES = 4;

    /**
     * 片段着色器，正常效果
     */
    public static final String FRAG_SHADER_NORMAL =
            "precision mediump float;\n" +
                    "    varying vec2 tc;\n" +
                    "    uniform sampler2D ySampler;\n" +
                    "    uniform sampler2D vuSampler;\n" +
                    "    const mat3 yuvToRgbMat = mat3(1.0, 1.0, 1.0, 0, -0.344, 1.77, 1.403, -0.714,0);\n" +
                    "    void main()\n" +
                    "    {\n" +
                    "        vec3 yuv;\n" +
                    "        yuv.x = texture2D(ySampler, tc).r;\n" +
                    "        vec4 vuVec = texture2D(vuSampler, tc);\n" +
                    "        yuv.y = vuVec.a - 0.5;\n" +
                    "        yuv.z = vuVec.r - 0.5;\n" +
                    "        gl_FragColor = vec4(yuvToRgbMat * yuv, 1.0);\n" +
                    "    }";
    /**
     * 片段着色器，灰度效果。R = G = B = Y
     */
    public static final String FRAG_SHADER_GRAY =
            "precision mediump float;\n" +
                    "    varying vec2 tc;\n" +
                    "    uniform sampler2D ySampler;\n" +
                    "    void main()\n" +
                    "    {\n" +
                    "        vec3 yuv;\n" +
                    "        yuv.xyz = texture2D(ySampler, tc).rrr;\n" +
                    "        gl_FragColor = vec4(yuv, 1.0);\n" +
                    "    }";

    /**
     * 顶点着色器
     */
    private static final String VERTEX_SHADER =
            "    attribute vec4 attr_position;\n" +
                    "    attribute vec2 attr_tc;\n" +
                    "    varying vec2 tc;\n" +
                    "    void main() {\n" +
                    "        gl_Position = attr_position;\n" +
                    "        tc = attr_tc;\n" +
                    "    }";

    // 源视频帧宽/高
    private int frameWidth, frameHeight;
    // 是否镜像
    private boolean isMirror;
    // 是否旋转
    private int rotateDegree = 0;

    // 用于画框并显示的NV21
    private byte[] nv21WithRect;

    private ByteBuffer yBuf = null, vuBuf = null;

    // 纹理id
    private int[] yTexture = new int[1];
    private int[] vuTexture = new int[1];

    private String fragmentShaderCode = FRAG_SHADER_NORMAL;

    private FloatBuffer squareVertices = null;
    private FloatBuffer coordVertices = null;

    private int programHandle = 0;

    // gl_attr
    private int glPosition;
    private int textureCoord;

    /**
     * 设置不同的片段着色器代码以达到不同的预览效果
     *
     * @param fragmentShaderCode 片段着色器代码
     */
    public void setFragmentShaderCode(String fragmentShaderCode) {
        this.fragmentShaderCode = fragmentShaderCode;
    }


    public void init(boolean isMirror, int rotateDegree, int frameWidth, int frameHeight) {
        if (this.frameWidth == frameWidth
                && this.frameHeight == frameHeight
                && this.rotateDegree == rotateDegree
                && this.isMirror == isMirror) {
            return;
        }
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.rotateDegree = rotateDegree;
        this.isMirror = isMirror;

        int yFrameSize = this.frameHeight * this.frameWidth;
        int vuFrameSize = yFrameSize / 2;
        yBuf = ByteBuffer.allocateDirect(yFrameSize);
        vuBuf = ByteBuffer.allocateDirect(vuFrameSize);

        // TODO：这段代码可删除
        // 这里的作用是为VU数据预先填上0x80，避免打开时的瞬间全是绿色
        byte[] vu = new byte[vuFrameSize];
        Arrays.fill(vu, (byte) 0x80);
        vuBuf.put(vu);
        vuBuf.position(0);

        // 顶点坐标
        squareVertices = ByteBuffer
                .allocateDirect(GLUtil.SQUARE_VERTICES.length * FLOAT_SIZE_BYTES)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        squareVertices.put(GLUtil.SQUARE_VERTICES).position(0);

        // 纹理坐标
        float[] coordVertice = GLUtil.getCoordVerticesByPreviewParams(isMirror, rotateDegree);
        // 显示多块数据
//        for (int i = 0; i < coordVertice.length; i++) {
//            coordVertice[i] *= 2;
//        }
        coordVertices = ByteBuffer.allocateDirect(coordVertice.length * FLOAT_SIZE_BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        coordVertices.put(coordVertice).position(0);

    }

    private void createTexture(int width, int height, int format, int[] textureId) {

        GLES20.glGenTextures(1, textureId, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId[0]);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, format, width, height, 0, format, GLES20.GL_UNSIGNED_BYTE, null);
    }

    /**
     * 创建OpenGL Program并关联shader代码中的变量
     */
    public boolean createGLProgram() {
        if (squareVertices == null || coordVertices == null) {
            return false;
        }
        programHandle = GLUtil.createShaderProgram(fragmentShaderCode, VERTEX_SHADER);
        if (programHandle != 0) {

            GLES20.glUseProgram(programHandle);

            glPosition = GLES20.glGetAttribLocation(programHandle, "attr_position");
            textureCoord = GLES20.glGetAttribLocation(programHandle, "attr_tc");

            GLES20.glEnableVertexAttribArray(glPosition);
            GLES20.glEnableVertexAttribArray(textureCoord);

            squareVertices.position(0);
            GLES20.glVertexAttribPointer(glPosition, COUNT_PER_SQUARE_VERTICE, GLES20.GL_FLOAT, false, 8, squareVertices);
            coordVertices.position(0);
            GLES20.glVertexAttribPointer(textureCoord, COUNT_PER_COORD_VERTICES, GLES20.GL_FLOAT, false, 8, coordVertices);


            int ySampler = GLES20.glGetUniformLocation(programHandle, "ySampler");
            int vuSampler = GLES20.glGetUniformLocation(programHandle, "vuSampler");

            GLES20.glUniform1i(ySampler, 0);
            GLES20.glUniform1i(vuSampler, 1);


            //启用纹理
            GLES20.glEnable(GLES20.GL_TEXTURE_2D);
            //创建纹理
            createTexture(frameWidth, frameHeight, GLES20.GL_LUMINANCE, yTexture);
            createTexture(frameWidth / 2, frameHeight / 2, GLES20.GL_LUMINANCE_ALPHA, vuTexture);

            return true;
        } else {
            return false;
        }
    }

    boolean prepareDraw() {
        if (programHandle != 0) {
            GLES20.glUseProgram(programHandle);

            GLES20.glEnableVertexAttribArray(glPosition);
            GLES20.glEnableVertexAttribArray(textureCoord);

            squareVertices.position(0);
            GLES20.glVertexAttribPointer(glPosition, COUNT_PER_SQUARE_VERTICE, GLES20.GL_FLOAT, false, 8, squareVertices);
            coordVertices.position(0);
            GLES20.glVertexAttribPointer(textureCoord, COUNT_PER_COORD_VERTICES, GLES20.GL_FLOAT, false, 8, coordVertices);

            return true;
        } else {
            Log.e(TAG, "program not created!");
            return false;
        }
    }

    synchronized boolean render() {
        if (vuBuf != null && programHandle != 0) {

            // y
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, yTexture[0]);
            GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D,
                    0,
                    0,
                    0,
                    frameWidth,
                    frameHeight,
                    GLES20.GL_LUMINANCE,
                    GLES20.GL_UNSIGNED_BYTE,
                    yBuf);

            // vu
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, vuTexture[0]);
            GLES20.glTexSubImage2D(GLES20.GL_TEXTURE_2D,
                    0,
                    0,
                    0,
                    frameWidth / 2,
                    frameHeight / 2,
                    GLES20.GL_LUMINANCE_ALPHA,
                    GLES20.GL_UNSIGNED_BYTE,
                    vuBuf);

            // 在数据绑定完成后进行绘制
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            return true;
        }
        return false;
    }

    boolean updateNV21(byte[] data) {
        if (vuBuf == null) {
            return false;
        }
        int ySize = frameWidth * frameHeight;
        int vuSize = ySize / 2;
        synchronized (this) {
            yBuf.put(data, 0, ySize).position(0);
            vuBuf.put(data, ySize, vuSize).position(0);
        }
        return true;
    }

    boolean updateNV21(byte[] data, Rect faceRect, int strokeWidth) {
        if (vuBuf == null) {
            return false;
        }
        // 避免重复创建，频繁GC
        if (nv21WithRect == null || nv21WithRect.length != data.length) {
            nv21WithRect = new byte[data.length];
        }
        System.arraycopy(data, 0, nv21WithRect, 0, nv21WithRect.length);

        ImageUtil.drawRectOnNv21(nv21WithRect, frameWidth, frameHeight, Color.YELLOW, strokeWidth, faceRect);
        int ySize = frameWidth * frameHeight;
        int vuSize = ySize / 2;

        synchronized (this) {
            yBuf.put(nv21WithRect, 0, ySize).position(0);
            vuBuf.put(nv21WithRect, ySize, vuSize).position(0);
        }
        return true;
    }
}
