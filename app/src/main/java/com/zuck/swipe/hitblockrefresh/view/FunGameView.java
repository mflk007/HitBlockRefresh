package com.zuck.swipe.hitblockrefresh.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by ZhaoFan on 2016/3/9.
 */
abstract class FunGameView extends View{

    static final int STATUS_GAME_PREPAR = 0;

    static final int STATUS_GAME_PLAY = 1;

    static final int STATUS_GAME_FINISHED = 2;

    static final int STATUS_GAME_OVER = 3;

    static final String TEXT_GAME_OVER = "Game Over";

    static final String TEXT_LOADING = "Loading...";

    static final String TEXT_LOADING_FINISHED = "Loading Finished";

    /**
     * 分割线默认宽度大小
     */
    static final float DIVIDING_LINE_SIZE = 1.f;

    static final float VIEW_HEIGHT_RATIO = .161f;

    int screenWidth, screenHeight;

    protected Paint mPaint;

    protected TextPaint textPaint;

    public FunGameView(Context context) {
        this(context, null);
    }

    public FunGameView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FunGameView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        initBaseTools();
        initBaseConfigParams(context);
        initConcreteView();
    }

    protected void initBaseTools() {
        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.parseColor("#C1C2C2"));

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(1.f);
    }

    protected void initBaseConfigParams(Context context) {
        screenWidth = getScreenMetrics(context).widthPixels;
        screenHeight = getScreenMetrics(context).heightPixels;
    }

    protected abstract void initConcreteView();

    protected abstract void drawGame(Canvas canvas);

    @Override
    protected void onDraw(Canvas canvas) {
        drawBoundary(canvas);
        drawGame(canvas);

    }

    private void drawBoundary(Canvas canvas) {
        mPaint.setColor(Color.parseColor("#606060"));
        canvas.drawLine(0, 0, screenWidth, 0, mPaint);
        canvas.drawLine(0, getMeasuredHeight(), screenWidth, getMeasuredHeight(), mPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(screenWidth, (int) (screenHeight * VIEW_HEIGHT_RATIO));
    }

    /**
     * 获取屏幕尺寸
     *
     * @param context context
     * @return 手机屏幕尺寸
     */
    private DisplayMetrics getScreenMetrics(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(dm);
        return dm;
    }
}
