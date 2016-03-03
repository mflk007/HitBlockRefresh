package com.zuck.swipe.hitblockrefresh.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Hitomis on 2016/3/1.
 */
public class HitBlockHeader extends LinearLayout {

    private HitBlockView hitBlockView;

    private RelativeLayout curtainReLayout, maskReLayout;

    private FrameLayout containerLayout;

    private TextView topMaskView, bottomMaskView;

    private int halfHitBlockHeight;

    private boolean isStart = false;


    public HitBlockHeader(Context context) {
        this(context, null);
    }

    public HitBlockHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HitBlockHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context mContext) {

        containerLayout = new FrameLayout(mContext);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        addView(containerLayout, lp);

        hitBlockView = new HitBlockView(mContext);
        hitBlockView.postStatus(HitBlockView.STATUS_GAME_PREPAR);
        containerLayout.addView(hitBlockView);

        LayoutParams maskLp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        maskLp.topMargin = (int) HitBlockView.DIVIDING_LINE_SIZE;
        maskLp.bottomMargin = (int) HitBlockView.DIVIDING_LINE_SIZE;

        curtainReLayout = new RelativeLayout(mContext);
        maskReLayout = new RelativeLayout(mContext);
        maskReLayout.setBackgroundColor(Color.parseColor("#5A5A5A"));

        topMaskView = new TextView(mContext);
        topMaskView.setTextColor(Color.BLACK);
        topMaskView.setBackgroundColor(Color.WHITE);
        topMaskView.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        topMaskView.setTextSize(20);
        topMaskView.setText("Pull to Break Out!");

        bottomMaskView = new TextView(mContext);
        bottomMaskView.setTextColor(Color.BLACK);
        bottomMaskView.setBackgroundColor(Color.WHITE);
        bottomMaskView.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        bottomMaskView.setTextSize(18);
        bottomMaskView.setText("Scrooll to move handle");

        containerLayout.addView(maskReLayout, maskLp);
        containerLayout.addView(curtainReLayout, maskLp);

        hitBlockView.getViewTreeObserver().addOnGlobalLayoutListener(new MeasureListener());

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);

        ViewGroup.LayoutParams layoutParams = getLayoutParams();
        layoutParams.height = 0;
        setLayoutParams(layoutParams);
        setGravity(Gravity.BOTTOM);
    }

    private class MeasureListener implements ViewTreeObserver.OnGlobalLayoutListener {

        @Override
        public void onGlobalLayout() {
            halfHitBlockHeight = (int) ((hitBlockView.getHeight() - 2 * HitBlockView.DIVIDING_LINE_SIZE) * .5f);
            RelativeLayout.LayoutParams topRelayLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, halfHitBlockHeight);
            RelativeLayout.LayoutParams bottomRelayLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, halfHitBlockHeight);
            topRelayLayoutParams.topMargin = (int) HitBlockView.DIVIDING_LINE_SIZE;
            bottomRelayLayoutParams.topMargin = halfHitBlockHeight + topRelayLayoutParams.topMargin;

            curtainReLayout.removeAllViews();

            curtainReLayout.addView(topMaskView, 0, topRelayLayoutParams);
            curtainReLayout.addView(bottomMaskView, 1, bottomRelayLayoutParams);

            getViewTreeObserver().removeGlobalOnLayoutListener(this);
        }
    }


    public void updateStatus() {
        ObjectAnimator topMaskAnimator = ObjectAnimator.ofFloat(topMaskView, "translationY", topMaskView.getTranslationY(), -halfHitBlockHeight);
        ObjectAnimator bottomMaskAnimator = ObjectAnimator.ofFloat(bottomMaskView, "translationY", bottomMaskView.getTranslationY(), halfHitBlockHeight);
        ObjectAnimator maskShadowAnimator = ObjectAnimator.ofFloat(maskReLayout, "alpha", maskReLayout.getAlpha(), 0);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(topMaskAnimator).with(bottomMaskAnimator).with(maskShadowAnimator);
        animatorSet.setDuration(800);

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                containerLayout.removeView(maskReLayout);
                containerLayout.removeView(curtainReLayout);
                hitBlockView.postStatus(HitBlockView.STATUS_GAME_PLAY);
            }
        });
        animatorSet.start();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//        int width = 0, height = 0;
//
//        int count = getChildCount();
//        for (int i = 0; i < count; i++) {
//            View childView = getChildAt(i);
//            measureChild(childView, widthMeasureSpec, heightMeasureSpec);
//            if (childView instanceof HitBlockView) {
//                width = childView.getMeasuredWidth();
//                height = childView.getMeasuredHeight();
//            }
//        }
//
//        if (heightMode == MeasureSpec.UNSPECIFIED || heightMode == MeasureSpec.EXACTLY) {
//            widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
//            heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
//        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void updateVisiableHeight(int height) {
        ViewGroup.LayoutParams mLayoutLayoutParams = (ViewGroup.LayoutParams) getLayoutParams();
        mLayoutLayoutParams.height = height;
        setLayoutParams(mLayoutLayoutParams);

        if (height >= hitBlockView.getHeight() && !isStart) {
            isStart = true;
            updateStatus();
        }
    }

    public int getVisiableHeight() {
        return getHeight();
    }
}
