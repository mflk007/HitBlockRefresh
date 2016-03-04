package com.zuck.swipe.hitblockrefresh.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.concurrent.Executors;

/**
 * Created by ZhaoFan on 2016/3/2.
 */
public class HitBlockRefreshView extends LinearLayout implements View.OnTouchListener {

    private static final String tag = "HitBlockRefreshView";

    /**
     * 下拉状态
     */
    public static final int STATUS_PULL_TO_REFRESH = 0;

    /**
     * 释放准备刷新状态
     */
    public static final int STATUS_RELEASE_TO_REFRESH = 1;

    /**
     * 正在刷新状态
     */
    public static final int STATUS_REFRESHING = 2;

    /**
     * 释放后，又按住玩游戏状态
     */
    public static final int STATUS_AGAIN_DOWN = 3;

    /**
     * 刷新完成状态
     */
    public static final int STATUS_REFRESH_FINISHED = 4;

    /**
     * 下拉刷新的回调接口
     */
    private PullToRefreshListener mListener;

    /**
     * 下拉头的View
     */
    private HitBlockHeader header;

    /**
     * 需要去下拉刷新的ListView
     */
    private ListView listView;

    /**
     * 下拉头的布局参数
     */
    private MarginLayoutParams headerLayoutParams;

    /**
     * 下拉头的高度
     */
    private int hideHeaderHeight;

    /**
     * 当前处理什么状态
     */
    private int currentStatus = STATUS_REFRESH_FINISHED;;

    /**
     * 手指按下时的屏幕纵坐标
     */
    private float preDownY;

    /**
     * 在被判定为滚动之前用户手指可以移动的最大值。
     */
    private int touchSlop;

    /**
     * 是否已加载过一次layout，这里onLayout中的初始化只需加载一次
     */
    private boolean loadOnce;

    /**
     * 当前是否可以下拉，只有ListView滚动到头的时候才允许下拉
     */
    private boolean ableToPull;

    private static final float STICK_RATIO = .65f;

    private int tempHeaderTopMargin;

    /**
     * 下拉刷新控件的构造函数，会在运行时动态添加一个下拉头的布局。
     *
     * @param context
     * @param attrs
     */
    public HitBlockRefreshView(Context context, AttributeSet attrs) {
        super(context, attrs);
        header = new HitBlockHeader(context);
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        setOrientation(VERTICAL);
        addView(header, 0);
    }

    /**
     * 进行一些关键性的初始化操作，比如：将下拉头向上偏移进行隐藏，给ListView注册touch事件。
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed && !loadOnce) {
            hideHeaderHeight = -header.getHeight();
            headerLayoutParams = (MarginLayoutParams) header.getLayoutParams();
            headerLayoutParams.topMargin = hideHeaderHeight;
            listView = (ListView) getChildAt(1);
            listView.setOnTouchListener(this);
            loadOnce = true;
        }
    }

    /**
     * 当ListView被触摸时调用，其中处理了各种下拉刷新的具体逻辑。
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        setIsAbleToPull(event);
        if (!ableToPull) return false;
        if (currentStatus == STATUS_AGAIN_DOWN) {
            return handleAgainDownAction(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                preDownY = event.getRawY();
                if (currentStatus == STATUS_REFRESHING) { //表示释放后处于刷新状态时候，又按住了
                    currentStatus = STATUS_AGAIN_DOWN;
                    headerLayoutParams.topMargin = 0;
                    header.setLayoutParams(headerLayoutParams);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                float currY = event.getRawY();
                float distance = currY - preDownY;
                float offsetY = distance * STICK_RATIO;
                if (distance <= 0 && headerLayoutParams.topMargin <= hideHeaderHeight) {
                    return false;
                }
                if (distance < touchSlop) {
                    return false;
                }

                if (headerLayoutParams.topMargin > 0 ) { // 头部全部被下拉出来的时候状态转换为释放刷新
                    currentStatus = STATUS_RELEASE_TO_REFRESH;
                }

                if (headerLayoutParams.topMargin > 0) {
                    currentStatus = STATUS_RELEASE_TO_REFRESH;
                } else {
                    currentStatus = STATUS_PULL_TO_REFRESH;
                }

                // 通过偏移下拉头的topMargin值，来实现下拉效果
                headerLayoutParams.topMargin = (int) (offsetY + hideHeaderHeight);
                header.setLayoutParams(headerLayoutParams);
                break;
            case MotionEvent.ACTION_UP:
                if (currentStatus == STATUS_PULL_TO_REFRESH) {
                    rollbackHeader();
                }
                if (currentStatus == STATUS_RELEASE_TO_REFRESH) {
                    refreshingRollBack2Header();
                }
                break;
        }
        if (currentStatus == STATUS_PULL_TO_REFRESH || currentStatus == STATUS_RELEASE_TO_REFRESH) {
            // 当前正处于下拉或释放状态，要让ListView失去焦点，否则被点击的那一项会一直处于选中状态
            listView.setPressed(false);
            listView.setFocusable(false);
            listView.setFocusableInTouchMode(false);
            // 当前正处于下拉或释放状态，通过返回true屏蔽掉ListView的滚动事件
            return true;
        }
        return false;
    }

    /**
     * 处理手指第二次按住屏幕玩游戏的事件
     * @param event
     * @return
     */
    private boolean handleAgainDownAction(MotionEvent event) {
        Log.d(tag, "&&&&&&&&&&&&&&&&& : " + event.getAction());
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                float currY = event.getRawY();
                float distance = currY - preDownY;
                float offsetY = distance * STICK_RATIO;
                header.moveRacket(offsetY);
                headerLayoutParams.topMargin = (int) (offsetY);
                header.setLayoutParams(headerLayoutParams);
                break;
            case MotionEvent.ACTION_UP:
                rollbackHeader();
                break;
        }
        listView.setPressed(false);
        listView.setFocusable(false);
        listView.setFocusableInTouchMode(false);
        return true;
    }

    /**
     * 根据当前ListView的滚动状态来设定 {@link #ableToPull}
     * 的值，每次都需要在onTouch中第一个执行，这样可以判断出当前应该是滚动ListView，还是应该进行下拉。
     *
     * @param event
     */
    private void setIsAbleToPull(MotionEvent event) {
        View firstChild = listView.getChildAt(0);
        if (firstChild != null) {
            int firstVisiblePos = listView.getFirstVisiblePosition();
            if (firstVisiblePos == 0 && firstChild.getTop() == 0) {
                // 如果首个元素的上边缘，距离父布局值为0，就说明ListView滚动到了最顶部，此时应该允许下拉刷新
                if (!ableToPull) {
                    preDownY = event.getRawY();
                }
                ableToPull = true;
            } else { // 反之
                if (headerLayoutParams.topMargin != hideHeaderHeight) {
                    headerLayoutParams.topMargin = hideHeaderHeight;
                    header.setLayoutParams(headerLayoutParams);
                }
                ableToPull = false;
            }
        } else {
            // 如果ListView中没有元素，也应该允许下拉刷新
            ableToPull = true;
        }
    }

    /**
     * 给下拉刷新控件注册一个监听器。
     *
     * @param listener
     *            监听器的实现。
     * @param id
     *            为了防止不同界面的下拉刷新在上次更新时间上互相有冲突， 请不同界面在注册下拉刷新监听器时一定要传入不同的id。
     */
    public void setOnRefreshListener(PullToRefreshListener listener, int id) {
        mListener = listener;
    }

    /**
     * 当所有的刷新逻辑完成后，记录调用一下，否则你的ListView将一直处于正在刷新状态。
     */
    public void finishRefreshing() {
        if (currentStatus != STATUS_AGAIN_DOWN) {
            rollbackHeader();
        }
    }

    /**
     * 回滚到头部刷新控件的高度，并触发后台刷新任务
     */
    private void refreshingRollBack2Header() {
        ValueAnimator rbToHeaderAnimator = ValueAnimator.ofInt(headerLayoutParams.topMargin, 0);
        long duration = (long) (headerLayoutParams.topMargin * 1.1f) >=0 ? (long) (headerLayoutParams.topMargin * 1.1f) : 0;
        rbToHeaderAnimator.setDuration(duration);
        rbToHeaderAnimator.setInterpolator(new AccelerateInterpolator());
        rbToHeaderAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int marginValue = Integer.parseInt(animation.getAnimatedValue().toString());
                headerLayoutParams.topMargin = marginValue;
                header.setLayoutParams(headerLayoutParams);
            }
        });
        rbToHeaderAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                currentStatus = STATUS_REFRESHING;
                header.postStart();
                Executors.newSingleThreadExecutor().submit(new Runnable() {
                    @Override
                    public void run() {
                        if (mListener != null) {
                            mListener.onRefresh();
                        }
                    }
                });
            }
        });
        rbToHeaderAnimator.start();
    }

    /**
     * 回滚下拉刷新头部控件
     */
    private void rollbackHeader() {
        tempHeaderTopMargin = headerLayoutParams.topMargin;
        ValueAnimator rbAnimator = ValueAnimator.ofInt(0, header.getHeight() + tempHeaderTopMargin);
        rbAnimator.setDuration(300);
        rbAnimator.setInterpolator(new AccelerateInterpolator());
        rbAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int marginValue = Integer.parseInt(animation.getAnimatedValue().toString());
                headerLayoutParams.topMargin = -marginValue + tempHeaderTopMargin;
                header.setLayoutParams(headerLayoutParams);
            }
        });
        rbAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (currentStatus == STATUS_PULL_TO_REFRESH || currentStatus == STATUS_REFRESH_FINISHED) {
                    currentStatus = STATUS_REFRESH_FINISHED;
                    return ;
                }
                currentStatus = STATUS_REFRESH_FINISHED;
                header.postEnd();
            }
        });
        rbAnimator.start();
    }

    /**
     * 下拉刷新的监听器，使用下拉刷新的地方应该注册此监听器来获取刷新回调。
     */
    public interface PullToRefreshListener {

        /**
         * 刷新时会去回调此方法，在方法内编写具体的刷新逻辑。注意此方法是在子线程中调用的， 你可以不必另开线程来进行耗时操作。
         */
        void onRefresh();

    }

}
