package com.zuck.swipe.hitblockrefresh.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

public class HitBlockView extends View {

    public static final int STATUS_GAME_PREPAR = 0;

    public static final int STATUS_GAME_PLAYING = 1;

    public static final int STATUS_GAME_OVER = 2;

    /**
     * 矩形块竖向排列的数目
     */
    private static final int BLOCK_VERTICAL_NUM = 5;

    /**
     * 矩形块横向排列的数目
     */
    private static final int BLOCK_HORIZONTAL_NUM = 3;

    /**
     * 小球默认其实弹射角度
     */
    private static final int DEFAULT_ANGLE = 30;

    /**
     * 分割线默认宽度大小
     */
    private static final float DIVIDING_LINE_SIZE = 1.f;

    /**
     * 小球移动速度
     */
    private static final float SPEED = 6.f;

    /**
     * 矩形块的高度
     */
    private static final float BLOCK_HEIGHT = 40.f;

    /**
     * 矩形块的宽度
     */
    private static final float BLOCK_WIDTH = 13.f;

    /**
     * 小球半径
     */
    private static final float BALL_RADIUS = 8.f;
    
    private Paint mPaint, blockPaint;

    private TextPaint textPaint;

    private float blockLeft;

    private float racketLeft, racketTop, racketHeight;

    private float cx, cy, preY;

    private int angle = DEFAULT_ANGLE;

    private int screenWidth;

    private List<Point> pointList = new ArrayList<>();

    private boolean isleft = true;

    private int gameStatus = STATUS_GAME_PREPAR;

    public HitBlockView(Context context) {
        this(context, null);
    }

    public HitBlockView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HitBlockView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        screenWidth = getScreenWidth(context);
        initTools();
    }


    /**
     * 获取屏幕宽度
     *
     * @param context context
     * @return 手机屏幕宽度
     */
    public int getScreenWidth(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    private void initTools() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(DIVIDING_LINE_SIZE);

        blockPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        blockPaint.setStyle(Paint.Style.FILL);

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setStyle(Paint.Style.STROKE);
        textPaint.setStrokeCap(Paint.Cap.ROUND);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = (int) Math.ceil(BLOCK_VERTICAL_NUM * BLOCK_HEIGHT + (BLOCK_VERTICAL_NUM - 1) * DIVIDING_LINE_SIZE + DIVIDING_LINE_SIZE * 2);
        setMeasuredDimension(screenWidth, height);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        blockLeft = BLOCK_HORIZONTAL_NUM * BLOCK_WIDTH + (BLOCK_HORIZONTAL_NUM - 1) * DIVIDING_LINE_SIZE;

        racketLeft = blockLeft * 14;

        racketHeight = (int) (BLOCK_HEIGHT * 1.6f);

        cx = racketLeft - 2 * BALL_RADIUS;
        cy = (int) (getHeight() * .5f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawBoundary(canvas);

        drawColorBlock(canvas);

        drawRacket(canvas);

        makeBallPath(canvas);

//        if (gameStatus == STATUS_GAME_PREPAR) {
//            drawCurtain(canvas);
//        }

    }

//    private void drawCurtain(Canvas canvas) {
//        drawTopCurtain(canvas);
//        drawBottomCurtain(canvas);
//    }
//
//    private void drawTopCurtain(Canvas canvas) {
//        canvas.saveLayer(0, DIVIDING_LINE_SIZE, getWidth(), getHeight() * .5f, null, Canvas.ALL_SAVE_FLAG);
//        canvas.drawColor(Color.WHITE);
//
//        textPaint.setTextSize(35);
//        canvas.drawText("Pull to Break Out", 50, 50, textPaint);
//        canvas.restore();
//    }
//
//    private void drawBottomCurtain(Canvas canvas) {
//        canvas.saveLayer(0, getHeight() * .5f, getWidth(), getHeight() - DIVIDING_LINE_SIZE, null, Canvas.ALL_SAVE_FLAG);
//        canvas.drawColor(Color.YELLOW);
//
//        textPaint.setTextSize(35);
//        canvas.drawText("Pull to Break Out", 300, 180, textPaint);
//        canvas.restore();
//    }

    /**
     * 绘制分割线
     * @param canvas 默认画布
     */
    private void drawBoundary(Canvas canvas) {
        mPaint.setColor(Color.parseColor("#606060"));
        canvas.drawLine(0, 0, screenWidth, 0, mPaint);
        canvas.drawLine(0, getHeight(), screenWidth, getHeight(), mPaint);
    }

    /**
     * 绘制挡板
     * @param canvas 默认画布
     */
    private void drawRacket(Canvas canvas) {
        mPaint.setColor(Color.parseColor("#A5A5A5"));
        canvas.drawRect(racketLeft, racketTop, racketLeft + BLOCK_WIDTH, racketTop + racketHeight, mPaint);
    }

    /**
     * 绘制并处理小球运动的轨迹
     * @param canvas 默认画布
     */
    private void makeBallPath(Canvas canvas) {
        mPaint.setColor(Color.BLACK);

        if (cx <= blockLeft * 2 + BALL_RADIUS) { // 小球进入到色块区域
            if (checkTouchBlock(cx, cy)) { // 反弹回来
                isleft = false;
            }
        }  else if (cx  <= blockLeft + BALL_RADIUS ) { // 小球穿过色块区域

        }

        if (cx + BALL_RADIUS >= racketLeft && cx - BALL_RADIUS < racketLeft + BLOCK_WIDTH) { //小球在挡板区域范围内
            if (checkTouchRacket(cy)) {
                isleft = true;
            }
        } else if (cx - BALL_RADIUS > racketLeft + BLOCK_HEIGHT) { // 小球超出挡板区域
            promptGameOver(canvas);
        }

        if (cy <= BALL_RADIUS + DIVIDING_LINE_SIZE) { // 小球撞到上边界
            angle = 180 - DEFAULT_ANGLE;
        } else if (cy >= getHeight() - BALL_RADIUS - DIVIDING_LINE_SIZE) { // 小球撞到下边界
            angle = 180 + DEFAULT_ANGLE;
        }


        if (isleft) {
            cx -= SPEED;
        } else {
            cx += SPEED;
        }
        cy -= (float) Math.tan(Math.toRadians(angle)) * SPEED;

        canvas.drawCircle(cx, cy, BALL_RADIUS, mPaint);

        postInvalidate();

    }

    private void promptGameOver(Canvas canvas) {

    }

    /**
     * 检查小球是否撞击到挡板
     * @param y 小球当前坐标Y值
     * @return 小球位于挡板Y值区域范围内：true，反之：false
     */
    private boolean checkTouchRacket(float y) {
        boolean flag = false;
        float diffVal = y - racketTop;
        if (diffVal >= 0 && diffVal <= racketHeight) { // 小球位于挡板Y值区域范围内
            flag = true;
        }
        return flag;
    }

    /**
     * 检查小球是否撞击到矩形块
     * @param x 小球坐标X值
     * @param y 小球坐标Y值
     * @return 撞击到：true，反之：false
     */
    private boolean checkTouchBlock(float x, float y) {
        int columnX = (int) ((x - blockLeft - BALL_RADIUS - SPEED ) / BLOCK_WIDTH);
        columnX = columnX == BLOCK_HORIZONTAL_NUM ? columnX - 1 : columnX;
        int rowY = (int) (y / BLOCK_HEIGHT);
        rowY = rowY == BLOCK_VERTICAL_NUM ? rowY - 1 : rowY;
        Point p = new Point();
        p.set(columnX, rowY);

        boolean flag = false;
        for (Point point : pointList) {
            if (point.equals(p.x, p.y)) {
                flag = true;
                break;
            }
        }

        if (!flag) {
            pointList.add(p);
        }
        return !flag;
    }

    /**
     * 绘制矩形色块
     * @param canvas 默认画布
     */
    private void drawColorBlock(Canvas canvas) {
        float left, top;
        int column, row, colorCode;
        for (int i = 0; i < BLOCK_HORIZONTAL_NUM * BLOCK_VERTICAL_NUM; i++) {
            column = i % BLOCK_HORIZONTAL_NUM;
            row = i % BLOCK_VERTICAL_NUM;

            boolean flag = false;
            for (Point point : pointList) {
                if (point.equals(column, row)) {
                    flag = true;
                    break;
                }
            }
            if (flag) {
                continue;
            }

            colorCode = (255 / BLOCK_HORIZONTAL_NUM) * column;
            blockPaint.setColor(Color.rgb(colorCode, colorCode, colorCode));

            left = blockLeft + column * (BLOCK_WIDTH + DIVIDING_LINE_SIZE);
            top = DIVIDING_LINE_SIZE + row * (BLOCK_HEIGHT + DIVIDING_LINE_SIZE);
            canvas.drawRect(left, top, left + BLOCK_WIDTH, top + BLOCK_HEIGHT, blockPaint);
        }
    }

    /**
     * 控制挡板上下移动
     * @param distance 挡板移动的距离，正数：下移，负数：上移
     */
    public void moveRacket(float distance) {
        float maxDistance = (getHeight() -  2 * DIVIDING_LINE_SIZE - racketHeight);

        if (distance > maxDistance) {
            distance = maxDistance;
        }

        racketTop += distance;
        postInvalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                preY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float currY = event.getY();
                float diffY = currY - preY;
                preY = currY;
                moveRacket(diffY);
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return true;
    }
}
