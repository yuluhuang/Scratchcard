package com.android.ylh.scratchcard;

import android.app.Notification;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

import java.lang.reflect.Type;

/**
 * User: 余芦煌(504367857@qq.com)
 * Date: 2014-12-30
 * Time: 11:07
 * FIXME
 */
public class Scratchcard extends View {


    //遮盖层
    private Paint mOutterPaint;
    private Path mPath;
    private Canvas mCanvas;
    private Bitmap mBitmap;

    private int mLastX;
    private int mLastY;

    private Bitmap mOutterBitmap;
    //----------------------------------

    // private Bitmap bitmap;
    private String mText;
    private Paint mBackPaint;
    /*
    * 记录刮奖信息文本的宽和高
    * */
    private Rect mTextBound;
    private int mTextSize;
    private int mTextColor;

    //判断遮盖层区域是否到达消失的值
    private boolean mComplete = false;

    public interface OnScratchcardListener {
        void complete();
    }

    private OnScratchcardListener mListener;

    public void setOnScratchcardListener(OnScratchcardListener mListener) {
        this.mListener = mListener;
    }

    public Scratchcard(Context context) {
        this(context, null);
    }


    public Scratchcard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Scratchcard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        TypedArray ta = null;
        try {
            ta = context.getTheme().obtainStyledAttributes(attrs,
                    R.styleable.Scratchcard, defStyleAttr, 0);

            int n = ta.getIndexCount();

            for (int i = 0; i < n; i++) {

                int attr = ta.getIndex(i);
                switch (attr) {
                    case R.styleable.Scratchcard_text:
                        mText = ta.getString(attr);
                        break;
                    case R.styleable.Scratchcard_textSize:
                        mTextSize = (int) ta.getDimension(attr, TypedValue.applyDimension(
                                TypedValue.COMPLEX_UNIT_SP, 22, getResources().getDisplayMetrics()
                        ));
                        break;
                    case R.styleable.Scratchcard_textColor:
                        mTextColor = ta.getColor(attr, 0x000000);
                        break;
                }
            }
        } finally {
            if (ta != null)
                ta.recycle();
        }
    }

    public void setText(String mText) {
        this.mText = mText;
        //当前画笔绘制文本的宽和高
        mBackPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        //初始化我们的bitmap
        mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        mCanvas = new Canvas(mBitmap);
        setupOutPaint();
        setupBackPaint();

        //mCanvas.drawColor(Color.parseColor("#101010"));
        mCanvas.drawRoundRect(new RectF(0, 0, width, height), 30, 30, mOutterPaint);//圆角

        mCanvas.drawBitmap(mOutterBitmap, null, new RectF(0, 0, width, height), null);

    }

    /*
    * 设置绘制获奖信息的画笔属性
    * */
    private void setupBackPaint() {
        mBackPaint.setColor(mTextColor);
        mBackPaint.setStyle(Paint.Style.FILL);
        mBackPaint.setTextSize(mTextSize);
        //当前画笔绘制文本的宽和高
        mBackPaint.getTextBounds(mText, 0, mText.length(), mTextBound);
    }

    /**
     * 设置绘制path画笔的一些属性
     */
    private void setupOutPaint() {
        //
        mOutterPaint.setColor(Color.parseColor("#c0c0c0"));
        mOutterPaint.setAntiAlias(true);
        mOutterPaint.setDither(true);
        mOutterPaint.setStrokeCap(Paint.Cap.ROUND);
        mOutterPaint.setStrokeJoin(Paint.Join.ROUND);
        mOutterPaint.setStyle(Paint.Style.FILL);
        mOutterPaint.setStrokeWidth(20);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();

        int x = (int) event.getX();
        int y = (int) event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mLastX = x;
                mLastY = y;
                mPath.moveTo(mLastX, mLastY);

            case MotionEvent.ACTION_MOVE:
                int dx = Math.abs(x - mLastX);
                int dy = Math.abs(y - mLastY);

                if (dx > 3 || dy > 3) {
                    mPath.lineTo(x, y);
                }
                mLastX = x;
                mLastY = y;
                break;

            case MotionEvent.ACTION_UP:
                if (!mComplete) {
                    new Thread(mRunnable).start();
                }

                break;


        }
        if (!mComplete){
            invalidate();
        }

        return true;
    }


    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            int w = getWidth();
            int h = getHeight();
            float wipeArea = 0;
            float totalArea = w * h;

            Bitmap bitmap = mBitmap;//
            int[] mPixels = new int[w * h];
            //获得bitmap上所有像素信息
            bitmap.getPixels(mPixels, 0, w, 0, 0, w, h);
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    int index = i + j * w;
                    if (mPixels[index] == 0) {
                        wipeArea++;
                    }
                }
            }
            if (wipeArea > 0 && totalArea > 0) {
                int percent = (int) (wipeArea * 100 / totalArea);
                Log.e("TAG", percent + "");
                if (percent > 50) {
                    mComplete = true;
                    postInvalidate();

                }
            }
        }
    };

    @Override
    protected void onDraw(Canvas canvas) {
        //canvas.drawBitmap(bitmap, 0, 0, null);//背后
        canvas.drawText(mText, getWidth() / 2 - mTextBound.width() / 2, getHeight() / 2 + mTextBound.height() / 2, mBackPaint);


        if (!mComplete) {
            drawPath();
            canvas.drawBitmap(mBitmap, 0, 0, null);
        } else {
            if (mListener != null) {
                mListener.complete();
            }
        }
    }

    private void drawPath() {
        mOutterPaint.setStyle(Paint.Style.STROKE);

        mOutterPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        mCanvas.drawPath(mPath, mOutterPaint);
    }


    /**
     * 进行一些初始化操作
     */
    private void init() {
        mOutterPaint = new Paint();
        mPath = new Path();

        //bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);

        mOutterBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
        mText = "谢谢惠顾";
        mTextBound = new Rect();
        mBackPaint = new Paint();
        mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 22, getResources().getDisplayMetrics());
    }
}
