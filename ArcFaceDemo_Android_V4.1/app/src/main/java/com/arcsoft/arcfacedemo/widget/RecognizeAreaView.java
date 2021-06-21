package com.arcsoft.arcfacedemo.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.arcsoft.arcfacedemo.R;

/**
 * 控制可识别区域的控件，中间的镂空区域为可识别区域。
 * <p>
 * 结合{@link FaceRectView}和{@link com.arcsoft.arcfacedemo.util.FaceRectTransformer}使用，可判断人脸是否显示在镂空区域
 * <p>
 * 注意：需要保证人脸框绘制正确，识别区域的控制才有效。
 * <p>
 * 实际使用中建议不要实现onTouch
 */
public class RecognizeAreaView extends View implements View.OnTouchListener {
    /**
     * 限制的识别区域
     */
    private RectF limitArea;

    /**
     * 不可识别区域的颜色
     */
    private int shadowColor;

    /**
     * 触摸点到当前识别区域的4个顶点距离的平方
     * 0：左上角
     * 1：右上角
     * 2：左下角
     * 3：右下角
     */
    private double[] distanceSquares = new double[4];

    /**
     * 识别区域发生变更的回调
     */
    public interface OnRecognizeAreaChangedListener {
        /**
         * 当识别区域发生变更时执行
         *
         * @param recognizeArea 新的识别区域（相对于View，而非图像数据）
         */
        void onRecognizeAreaChanged(Rect recognizeArea);
    }

    OnRecognizeAreaChangedListener onRecognizeAreaChangedListener;

    /**
     * 设置识别区域发生变更的回调
     *
     * @param onRecognizeAreaChangedListener 识别区域发生变更的回调
     */
    public void setOnRecognizeAreaChangedListener(OnRecognizeAreaChangedListener onRecognizeAreaChangedListener) {
        this.onRecognizeAreaChangedListener = onRecognizeAreaChangedListener;
    }

    public RecognizeAreaView(Context context) {
        this(context, null);
    }

    public RecognizeAreaView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        shadowColor = ContextCompat.getColor(context, R.color.color_black_shadow);
        setOnTouchListener(this);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        limitArea = new RectF(0, 0, width, height);
        if (onRecognizeAreaChangedListener != null) {
            onRecognizeAreaChangedListener.onRecognizeAreaChanged(
                    new Rect(((int) limitArea.left), ((int) limitArea.top),
                            ((int) limitArea.right), ((int) limitArea.bottom))
            );
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (limitArea == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            canvas.clipOutRect(limitArea);
        } else {
            canvas.clipRect(limitArea, Region.Op.DIFFERENCE);
        }
        canvas.drawColor(shadowColor);
    }

    /**
     * 根据最近的触摸点，刷新识别区域
     *
     * @param x 触摸点的横坐标
     * @param y 触摸点的纵坐标
     */
    private void updateRecognizeArea(float x, float y) {
        /*
          0：左上角
          1：右上角
          2：左下角
          3：右下角
         */
        distanceSquares[0] = getDistanceSquare(x, y, limitArea.left, limitArea.top);
        distanceSquares[1] = getDistanceSquare(x, y, limitArea.right, limitArea.top);
        distanceSquares[2] = getDistanceSquare(x, y, limitArea.left, limitArea.bottom);
        distanceSquares[3] = getDistanceSquare(x, y, limitArea.right, limitArea.bottom);

        int closestIndex = 0;
        double closestDistance = distanceSquares[0];
        for (int i = 1; i < distanceSquares.length; i++) {
            double distance = distanceSquares[i];
            if (closestDistance > distance) {
                closestDistance = distance;
                closestIndex = i;
            }
        }
        switch (closestIndex) {
            case 0:
                limitArea.left = x;
                limitArea.top = y;
                break;
            case 1:
                limitArea.right = x;
                limitArea.top = y;
                break;
            case 2:
                limitArea.left = x;
                limitArea.bottom = y;
                break;
            case 3:
                limitArea.right = x;
                limitArea.bottom = y;
                break;
            default:
                break;
        }
    }

    /**
     * 获取两点距离的平方（由于只是为了大小比较，所以没必要开根号，减少运算）
     *
     * @param x1 第一个点的横坐标
     * @param y1 第一个点的纵坐标
     * @param x2 第二个点的横坐标
     * @param y2 第二个点的纵坐标
     * @return 距离的平方
     */
    private double getDistanceSquare(float x1, float y1, float x2, float y2) {
        float deltaHorizontal = x1 - x2;
        float deltaVertical = y1 - y2;
        return deltaHorizontal * deltaHorizontal + deltaVertical * deltaVertical;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int pointerCount = event.getPointerCount();
        for (int i = 0; i < pointerCount; i++) {
            updateRecognizeArea(event.getX(i), event.getY(i));
        }
        if (onRecognizeAreaChangedListener != null) {
            onRecognizeAreaChangedListener.onRecognizeAreaChanged(
                    new Rect(((int) limitArea.left), ((int) limitArea.top),
                            ((int) limitArea.right), ((int) limitArea.bottom))
            );
        }
        invalidate();
        return true;
    }
}
