package com.zuck.swipe.hitblockrefresh.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class BattleCityView extends View {

    private static final String tag = "BattleCityView";

    private static int TANK_ROW_NUM = 3;

    /**
     * 分割线默认宽度大小
     */
    static final float DIVIDING_LINE_SIZE = 1.f;

    static final float VIEW_HEIGHT_RATIO = .161f;

    static final float TANK_BARREL_RATIO = 1/3.f;

    static final float BULLET_NUM_RATIO = .4f;

    static final int TANK_EXTRA_SPACING = 10;

    private Paint mPaint;

    private TextPaint textPaint;

    private SparseArray<Queue<RectF>> eTankSparseArray;

    private Queue<Point> mBulletList;

    private Random random;

    private float selfTankTop, heightSize;

    private float bulletRadius;

    private int screenWidth, screenHeight;

    private int tankSize, barrelSize, tankSpaceSize, bulletSpaceSize;

    private int enemySpeed = 3, bulletSpeed = 8;

    private int offsetETankX, offsetMBulletX;

    private boolean isAdditional, isLaunch;

    public BattleCityView(Context context) {
        this(context, null);
    }

    public BattleCityView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BattleCityView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initTools();
        initConfigParams(context);
    }

    private void initTools() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(DIVIDING_LINE_SIZE);

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

        random = new Random();
    }

    private void initConfigParams(Context context) {
        screenWidth = getScreenMetrics(context).widthPixels;
        screenHeight = getScreenMetrics(context).heightPixels;

        selfTankTop = DIVIDING_LINE_SIZE;

        heightSize = screenHeight * VIEW_HEIGHT_RATIO;
        tankSize = (int) (Math.floor((heightSize - (TANK_ROW_NUM + 1) * DIVIDING_LINE_SIZE) / TANK_ROW_NUM + .5f));
        barrelSize = (int) Math.floor(tankSize * TANK_BARREL_RATIO + .5f);
        tankSpaceSize = tankSize + barrelSize + TANK_EXTRA_SPACING;
        bulletSpaceSize = (int) (screenWidth * BULLET_NUM_RATIO);

        bulletRadius = (barrelSize - 2 * DIVIDING_LINE_SIZE) * .5f;

        eTankSparseArray = new SparseArray<>();
        int option = apperanceOption();
        for (int i = 0; i < TANK_ROW_NUM; i++) {
            Queue<RectF> rectFQueue = new LinkedList<>();
            if (i == option) {
                rectFQueue.offer(generateEnemyTank(i));
            }
            eTankSparseArray.put(i, rectFQueue);
        }

        mBulletList = new LinkedList<>();
    }

    private RectF generateEnemyTank(int index) {
        float left = - (tankSize + barrelSize);
        float top = index * (tankSize + DIVIDING_LINE_SIZE) + DIVIDING_LINE_SIZE ;
        return new RectF(left, top, left + tankSize, top + tankSize);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(screenWidth, (int) heightSize);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        drawBoundary(canvas);

        drawEnemyTank(canvas);

        drawSelfTank(canvas);

        makeBulletPath(canvas);
    }

    private Point usedBullet;

    private void makeBulletPath(Canvas canvas) {
        offsetMBulletX += bulletSpeed;
        if (offsetMBulletX / bulletSpaceSize == 1) {
            isLaunch = true;
            offsetMBulletX = 0;
        } else {
            isLaunch = false;
        }

        if (isLaunch) {
            Point bulletPoint = new Point();
            bulletPoint.x = screenWidth - tankSize - barrelSize;
            bulletPoint.y = (int) (selfTankTop + tankSize * .5f);
            mBulletList.offer(bulletPoint);
        }

        boolean isOversetp = false;
        for (Point point : mBulletList) {
            if (checkWipeOutETank(point)) {
                usedBullet = point;
                continue;
            }
            if (point.x + bulletRadius <= 0) {
                isOversetp = true;
            }
            drawBullet(canvas, point);
        }

        if (isOversetp) {
            mBulletList.poll();
        }

        mBulletList.remove(usedBullet);
        usedBullet = null;
    }

    private boolean checkWipeOutETank(Point point) {
        boolean beHit = false;
        int index = point.y / (getMeasuredHeight() / TANK_ROW_NUM);
        Log.d(tag, "****************** : " + "<" + String.valueOf(index) + ">");
        index = index == TANK_ROW_NUM ? TANK_ROW_NUM - 1 : index;
        index = index == -1 ? 0 : index;
        Queue<RectF> rectFQueue = eTankSparseArray.get(index);
        for (RectF rectF : rectFQueue) {
            if (rectF.contains(point.x, point.y)) { // 击中
                beHit = true;
                break;
            }
        }
        if (beHit) {
            rectFQueue.poll();
        }
        return beHit;
    }

    private void drawBullet(Canvas canvas, Point point) {
        point.x -= bulletSpeed;
        canvas.drawCircle(point.x, point.y, bulletRadius, mPaint);
    }

    private void drawSelfTank(Canvas canvas) {
        canvas.drawRect(screenWidth - tankSize, selfTankTop, screenWidth, selfTankTop + tankSize, mPaint);
        canvas.drawRect(screenWidth - tankSize - barrelSize,
                selfTankTop + (tankSize - barrelSize) * .5f,
                screenWidth - tankSize,
                selfTankTop + (tankSize - barrelSize) * .5f + barrelSize,
                mPaint);
    }

    private void drawBoundary(Canvas canvas) {
        mPaint.setColor(Color.parseColor("#606060"));
        canvas.drawLine(0, 0, screenWidth, 0, mPaint);
        canvas.drawLine(0, getMeasuredHeight(), screenWidth, getMeasuredHeight(), mPaint);
    }

    private void drawEnemyTank(Canvas canvas) {
        mPaint.setColor(Color.parseColor("#909090"));
        offsetETankX += enemySpeed;
        if (offsetETankX / tankSpaceSize == 1) {
            isAdditional = true;
            offsetETankX = 0;
        } else {
            isAdditional = false;
        }

        boolean isOversetp = false;
        int option = apperanceOption();

        for (int i = 0; i < TANK_ROW_NUM; i++) {
            Queue<RectF> rectFQueue = eTankSparseArray.get(i);

            if (isAdditional && i == option) {
                rectFQueue.offer(generateEnemyTank(i));
            }

            for (RectF rectF : rectFQueue) {
                if (rectF.left >= screenWidth) {
                    isOversetp = true;
                    continue;
                }
                drawTank(canvas, rectF);
            }
            if (isOversetp) {
                rectFQueue.poll();
                isOversetp = false;
            }
        }
        invalidate();
    }

    private void drawTank(Canvas canvas, RectF rectF) {
        rectF.set(rectF.left + enemySpeed, rectF.top, rectF.right + enemySpeed, rectF.bottom);
        canvas.drawRect(rectF, mPaint);
        float barrelTop = rectF.top + (tankSize - barrelSize) * .5f;
        canvas.drawRect(rectF.right, barrelTop, rectF.right + barrelSize, barrelTop + barrelSize, mPaint);

    }

    private int apperanceOption() {
        return random.nextInt(TANK_ROW_NUM);
    }

    public void moveController(float distance) {
        float maxDistance = (getMeasuredHeight() -  2 * DIVIDING_LINE_SIZE - tankSize);

        if (distance > maxDistance) {
            distance = maxDistance;
        }

        selfTankTop += distance;
        postInvalidate();
    }

    private float preY;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                preY = event.getRawY();
                break;
            case MotionEvent.ACTION_MOVE:
                float distance = event.getRawY() - preY;
                preY = event.getRawY();
                moveController(distance);
                break;
        }
        return true;
    }

    /**
     * 获取屏幕宽度
     *
     * @param context context
     * @return 手机屏幕宽度
     */
    public DisplayMetrics getScreenMetrics(Context context) {
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(dm);
        return dm;
    }

}
