package com.honeyrock.podslider2;

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;

import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import java.lang.ref.WeakReference;

public class PodSlider extends View {

    public enum DrawableSize {
        FIT_POD_CIRCLE, FIT_MEDIUM_CIRCLE, FIT_LARGE_CIRCLE
    }

    public static final int ANIMATION_DURATION = 600;
    private int numberOfPods;
    private Paint mainPaint;
    private Pod[] pods;
    private OnPodClickListener mPodClickListener;
    private int currentlySelectedPod = -1;
    private boolean firstDraw = true;
    private ViewPager mViewPager;
    private boolean isViewMeasured = false;
    private int mainSliderColor;
    private int podColor;
    private int selectedPodColor;
    private int mediumCircleColor;
    private int largeCircleColor;
    private Paint mediumCirclePaint;
    private Paint largeCirclePaint;

    private float touchStartX;
    private float touchStartY;

    float largeCircleRadius;
    float mediumCircleRadius;

    private float largeCircleCurrentCenterX;

    private float mediumCircleCurrentCenterX;

    private ValueAnimator largeCircleAnimator = null;
    private ValueAnimator mediumCircleAnimator = null;
    private Interpolator largeCircleInterpolator = null;
    private Interpolator mediumCircleInterpolator = null;

    private boolean usePageTitle = true;

    @SuppressWarnings("FieldCanBeLocal")
    private float podRadius;

    private ValueAnimator.AnimatorUpdateListener largeCircleAnimatorListener =
            new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    updateLargeCircleCenterX(animation);
                }
            };
    private ValueAnimator.AnimatorUpdateListener mediumCircleAnimatorListener =
            new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    updateMediumCircleCenterX(animation);
                }
            };

    private Rect clipBounds;

    public PodSlider(Context context) {
        super(context);
    }

    public PodSlider(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PodSlider(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(21)
    public PodSlider(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public void setPodClickListener(OnPodClickListener listener) {
        this.mPodClickListener = listener;
    }

    private void init(Context c, AttributeSet attrs) {
        TypedArray a = c.getTheme().obtainStyledAttributes(attrs, R.styleable.PodSlider, 0, 0);
        try {
            int mainSliderColor = a.getColor(R.styleable.PodSlider_mainSliderColor, 0);
            int numberOfPods = a.getInt(R.styleable.PodSlider_numberOfPods, 1);
            int podColor = a.getColor(R.styleable.PodSlider_podColor, 0);
            int selectedPodColor = a.getColor(R.styleable.PodSlider_selectedPodColor, Color.WHITE);
            mediumCircleColor = a.getColor(R.styleable.PodSlider_mediumCircleColor, podColor);
            largeCircleColor = a.getColor(R.styleable.PodSlider_largeCircleColor, mainSliderColor);
            init(numberOfPods, podColor, mainSliderColor, selectedPodColor);
        } finally {
            a.recycle();
        }
    }

    @Override
    public void setBackgroundColor(int color) {
        mainSliderColor = color;
        this.mainPaint.setColor(color);
        invalidate();
    }

    public void setNumberOfPods(int numberOfPods) {
        this.numberOfPods = numberOfPods;
        pods = new Pod[numberOfPods];
        for (int i = 0; i < numberOfPods; i++) {
            pods[i] = new Pod(mainSliderColor, podColor, selectedPodColor, this, i);
        }
        if (currentlySelectedPod != -1 && currentlySelectedPod < PodSlider.this.numberOfPods) {
            setCurrentlySelectedPod(currentlySelectedPod);
        } else {
            setCurrentlySelectedPod(0);
        }
        if (!isViewMeasured) {
            return;
        }
        requestLayout();
    }

    public void setPodTexts(String[] texts) {
        if (texts.length < pods.length) {
            throw new IllegalStateException("The length of the texts array must be same " +
                    "as the number of pods.");
        }
        for (int i = 0; i < pods.length; i++) {
            pods[i].setCenterText(texts[i]);
        }
        invalidate();
    }

    public void setPodDrawables(Drawable[] drawables, DrawableSize size) {
        if (drawables.length < pods.length)
            throw new IllegalStateException("The length of the drawables array must be same " +
                    "as the number of pods.");
        for (int i = 0; i < pods.length; i++) {
            pods[i].setCenterDrawable(drawables[i], size);
        }
        invalidate();
    }

    private void init(final int numberOfPods, int podColor,
                      int mainSliderColor, int selectedPodColor) {
        this.mainSliderColor = mainSliderColor;
        this.podColor = podColor;
        this.selectedPodColor = selectedPodColor;
        clipBounds = new Rect();

        mainPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mainPaint.setColor(mainSliderColor);
        //mainPaint.setShadowLayer(5.5f, 6.0f, 6.0f, Color.BLACK);
        mainPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        mediumCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mediumCirclePaint.setColor(mediumCircleColor);
        //mediumCirclePaint.setShadowLayer(5.5f, 6.0f, 6.0f, Color.BLACK);
        mediumCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        largeCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        largeCirclePaint.setColor(largeCircleColor);
        //largeCirclePaint.setShadowLayer(5.5f, 6.0f, 6.0f, Color.BLACK);
        largeCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        setNumberOfPods(numberOfPods);
        currentlySelectedPod = 0;
        largeCircleInterpolator = new EaseOutElasticInterpolator(1.0f, 1.9f);
        mediumCircleInterpolator = new EaseOutElasticInterpolator(1.0f, 1.3f);
    }

    /**
     * Set the currently selected pod AND also animate the view.
     * <p/>
     * Please remember to use this only after
     * the view has been rendered (i.e donot use this in onCreate()/onCreateView()
     * this will cause the large and medium circle to be drawn at (0, height/2)
     *
     * @param currentlySelectedPod the position of pod you want to be selected.
     */
    public void setCurrentlySelectedPodAndAnimate(int currentlySelectedPod) {
        setCurrentlySelectedPod(currentlySelectedPod);
        update(pods[currentlySelectedPod].getCenterX());
    }

    /**
     * @param interpolator a custom interpolator to interpolate time for large circle animation
     */
    @SuppressWarnings("unused")
    public void setLargeCircleInterpolator(Interpolator interpolator) {
        largeCircleInterpolator = interpolator;
    }

    /**
     * @param interpolator a custom interpolator to interpolate time for medium circle animation.
     */
    @SuppressWarnings("unused")
    public void setMediumCircleInterpolator(Interpolator interpolator) {
        this.mediumCircleInterpolator = interpolator;
    }

    public void setCurrentlySelectedPod(int currentlySelectedPod) {
        this.currentlySelectedPod = currentlySelectedPod;
        for (int i = 0; i < numberOfPods; i++) {
            pods[i].setSelected(false);
        }
        pods[currentlySelectedPod].setSelected(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction() & MotionEvent.ACTION_MASK;
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                touchStartX = event.getX();
                touchStartY = event.getY();
                return true;
            case MotionEvent.ACTION_UP:
                float endX = event.getX();
                float endY = event.getY();
                if (isAClick(touchStartX, endX, touchStartY, endY)) {
                    onClick(endX, endY);
                }
                return true;
            default:
                return false;
        }
    }

    private void onClick(float x, float y) {
        for (int i = 0; i < pods.length; i++) {
            Pod pod = pods[i];
            if (pod.doesCoOrdinatesLieInSelectRange(x, y)) {
                setCurrentlySelectedPodAndAnimate(i);
                // propagate click.
                if (mPodClickListener != null)
                    this.mPodClickListener.onPodClick(i);
                if (mViewPager != null)
                    mViewPager.setCurrentItem(i);
                return;
            }
        }
    }

    private void moveMediumCircle(float toX) {
        if (mediumCircleAnimator != null && mediumCircleAnimator.isRunning())
            mediumCircleAnimator.cancel();
        mediumCircleAnimator = ValueAnimator.ofFloat(mediumCircleCurrentCenterX, toX);
        mediumCircleAnimator.setDuration(ANIMATION_DURATION);
        mediumCircleAnimator.setInterpolator(mediumCircleInterpolator);
        mediumCircleAnimator.addUpdateListener(mediumCircleAnimatorListener);
        mediumCircleAnimator.start();
    }

    private void moveLargeCircle(float toX) {
        if (largeCircleAnimator != null && largeCircleAnimator.isRunning())
            largeCircleAnimator.cancel();
        largeCircleAnimator = ValueAnimator.ofFloat(largeCircleCurrentCenterX, toX);
        largeCircleAnimator.setDuration(ANIMATION_DURATION);
        largeCircleAnimator.setInterpolator(largeCircleInterpolator);
        largeCircleAnimator.addUpdateListener(largeCircleAnimatorListener);
        largeCircleAnimator.start();
    }

    private void updateLargeCircleCenterX(ValueAnimator animator) {
        largeCircleCurrentCenterX = (float) animator.getAnimatedValue();
        invalidateOnlyRectIfPossible();
    }

    private void invalidateOnlyRectIfPossible() {
        if (clipBounds != null && clipBounds.left != 0 &&
                clipBounds.top != 0 && clipBounds.right != 0 && clipBounds.bottom != 0)
            invalidate(clipBounds);
        else
            invalidate();
    }

    private void updateMediumCircleCenterX(ValueAnimator animator) {
        mediumCircleCurrentCenterX = (float) animator.getAnimatedValue();
        invalidateOnlyRectIfPossible();
    }

    private void update(float toX) {
        pods[currentlySelectedPod].animatePod();
        moveLargeCircle(toX);
        moveMediumCircle(toX);
    }

    public void usePageTitles(boolean usePageTitle) {
        this.usePageTitle = usePageTitle;
    }

    public void setUpWithViewPager(ViewPager pager) {
        mViewPager = pager;
        final PagerAdapter adapter = pager.getAdapter();
        if (adapter == null) {
            throw new IllegalArgumentException("ViewPager does not have a PagerAdapter set");
        }
        setNumberOfPods(adapter.getCount());
        if (usePageTitle) {
            for (int i = 0; i < pods.length; i++) {
                String pageTitle = adapter.getPageTitle(i) != null ?
                        adapter.getPageTitle(i).toString() : null;
                if (pageTitle != null)
                    pods[i].setCenterText(pageTitle);
            }
        }
        pager.addOnPageChangeListener(new PodSliderOnPageChangeListener(this));
        if (adapter.getCount() > 0) {
            final int curItem = pager.getCurrentItem();
            if (currentlySelectedPod != curItem) {
                currentlySelectedPod = curItem;
            }
        }
    }
//
//    public Pod getPod(int position) {
//        if (pods == null) {
//            throw new IllegalStateException("You need to init the pods first by calling " +
//                    "setNumberOfPods() or setUpWithViewPager");
//        }
//        if (position >= pods.length || position <= 0) {
//            throw new IndexOutOfBoundsException("Number of pods is " + pods.length
//                    + " passed position is " + position);
//        }
//        return pods[position];
//    }

    private boolean isAClick(float startX, float endX, float startY, float endY) {
        float differenceX = Math.abs(startX - endX);
        float differenceY = Math.abs(startY - endY);
        return !(differenceX > 5f || differenceY > 5f);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.getClipBounds(clipBounds);
        // make large circle diameter equal to the height of the canvas.
        largeCircleRadius = Math.min((getHeight() - getPaddingTop() - getPaddingBottom()) / 2,
                (getWidth() - getPaddingRight() - getPaddingLeft()) / 2);
        mediumCircleRadius = largeCircleRadius / 1.5f;
        podRadius = largeCircleRadius * 2 / 7;
        float rectangleRight = clipBounds.right - getPaddingRight() - (largeCircleRadius / 5);
        float rectangleLeft = clipBounds.left + getPaddingLeft() + (largeCircleRadius / 5);
        float rectangleTop = getPaddingTop() + clipBounds.top + (largeCircleRadius / 5);
        float rectangleBottom = clipBounds.bottom - getPaddingBottom() - (largeCircleRadius / 5);

        drawRoundedRect(canvas, rectangleLeft, rectangleTop, rectangleRight, rectangleBottom);
        float podCenterY = rectangleTop + ((rectangleBottom - rectangleTop) / 2);
        if (numberOfPods == 1) {
            // draw one at the center and be done.
            float centerX = rectangleLeft + ((rectangleRight - rectangleLeft) / 2);
            largeCircleCurrentCenterX = centerX;
            mediumCircleCurrentCenterX = centerX;
            Pod pod = pods[0];
            pod.setCenter(centerX, podCenterY);
            pod.setPodRadius(podRadius);
            canvas.drawCircle(largeCircleCurrentCenterX, podCenterY, largeCircleRadius, largeCirclePaint);
            canvas.drawCircle(mediumCircleCurrentCenterX, podCenterY, mediumCircleRadius, mediumCirclePaint);
            pod.drawPod(canvas);
            return;
        }
        // else you start calculation.
        float startX = rectangleLeft + (rectangleBottom - rectangleTop) / 2;
        float gapBetweenPodCenters = calculateGapBetweenPodCenters(rectangleLeft, rectangleRight,
                rectangleTop, rectangleBottom);
        if (firstDraw) {
            firstDraw = false;
            largeCircleCurrentCenterX = startX + currentlySelectedPod * gapBetweenPodCenters;
            mediumCircleCurrentCenterX = startX + currentlySelectedPod * gapBetweenPodCenters;
        }
        canvas.drawCircle(largeCircleCurrentCenterX, podCenterY, largeCircleRadius, largeCirclePaint);
        canvas.drawCircle(mediumCircleCurrentCenterX, podCenterY, mediumCircleRadius, mediumCirclePaint);
        for (int i = 0, n = numberOfPods; i < n; i++) {
            float podCenterX = startX + i * gapBetweenPodCenters;
            Pod pod = pods[i];
            pod.setPodRadius(podRadius);
            pod.setCenter(podCenterX, podCenterY);
            pod.drawPod(canvas);
        }
    }

    private float calculateGapBetweenPodCenters(float left, float right, float top, float bottom) {
        // The center of leftmost circle is at getHeight() / 2, getHeight() / 2 (by design)
        // The center of rightmost circle is at getWidth() - getHeight /2, getHeight / 2 (by design)
        // So the distance between these 2 points is the difference is their x-axis co ordinates
        // as both have the same y-axis coordinate.
        // Which is nothing but (getWidth() - getHeight / 2) - getHeight / 2
        // which equal to getWidth() - 2 * getHeight / 2
        // which is equal to getWidth() - getHeight()
        float distanceBetweenTheCentersOfPodsAtTheEnd = (right - left) - (bottom - top);
        // Now to determine the distance between the center of each pod
        // I divide the distanceBetweenTheCentersOfPodsAtTheEnd by number of Pods -1
        // because distance between one pod starts at the 0th position.
        return distanceBetweenTheCentersOfPodsAtTheEnd / (numberOfPods - 1);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;
        int desiredHeight = (int) (getContext().getResources().getDisplayMetrics().density * 30);

        //Measure Height
        if (heightMode == MeasureSpec.EXACTLY) {
            //Must be this size
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            height = Math.min(desiredHeight, heightSize);
        } else {
            //Be whatever you want
            height = desiredHeight;
        }
        int desiredWidth = getDesiredWidth(height);

        //Measure Width
        if (widthMode == MeasureSpec.EXACTLY) {
            //Must be this size
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            //Can't be bigger than...
            width = Math.min(desiredWidth, widthSize);
        } else {
            //Be whatever you want
            width = desiredWidth;
        }
        setMeasuredDimension(width, height);
        isViewMeasured = true;
    }

    private int getDesiredWidth(int height) {
        return height * numberOfPods;
    }

    private void drawRoundedRect(Canvas canvas, float left, float top, float right, float bottom) {
        float radius = Math.min((bottom - top) / 2, (right - left) / 2);
        canvas.drawCircle(left + radius, top + radius, radius, mainPaint);
        canvas.drawCircle(right - radius, top + radius, radius, mainPaint);
        canvas.drawRect(left + radius, top, right - radius, bottom, mainPaint);
    }

    public static class PodSliderOnPageChangeListener implements ViewPager.OnPageChangeListener {
        private final WeakReference<PodSlider> mPodSliderRef;
//        private int mScrollState;
//        private int mPendingSelection = -1;

        public PodSliderOnPageChangeListener(PodSlider podSlider) {
            mPodSliderRef = new WeakReference<>(podSlider);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
//            mScrollState = state;
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
//            final PodSlider podSlider = mPodSliderRef.get();
//            if (podSlider == null) {
//                return;
//            }
            /*float currentX = podSlider.largeAndSmallCircleCurrentCenterX;
            float differenceX;
            if (position == podSlider.currentlySelectedPod) {
                differenceX = podSlider.pods[position + 1].getCenterX() - currentX;
                podSlider.largeAndSmallCircleCurrentCenterX += differenceX * positionOffset;
            } else {
                differenceX = podSlider.pods[position - 1].getCenterX() - currentX;
                podSlider.largeAndSmallCircleCurrentCenterX -= differenceX * positionOffset;
            }
            podSlider.invalidate();*/

            /*if (mPendingSelection == -1 ||
                    podSlider.pods[podSlider.currentlySelectedPod].getCenterX() !=
                            podSlider.pods[mPendingSelection].getCenterX()) {
                float currentX = podSlider.largeAndSmallCircleCurrentCenterX;
                float differenceX;
                if (positionOffset > 0 || mPendingSelection == -1)
                    differenceX = podSlider.pods[position + 1].getCenterX() - currentX;
                else
                    differenceX = podSlider.pods[mPendingSelection].getCenterX() - currentX;
                podSlider.largeAndSmallCircleCurrentCenterX += differenceX * positionOffset;
                podSlider.invalidate();
            }
            if (mScrollState == ViewPager.SCROLL_STATE_IDLE) {
                if (mPendingSelection != -1) {
                    podSlider.currentlySelectedPod = mPendingSelection;
                    podSlider.pods[mPendingSelection].animatePod();
                    mPendingSelection = -1;
                }
            }*/
            /*if (mPendingSelection != -1 && mScrollState == ViewPager.SCROLL_STATE_IDLE) {
                podSlider.currentlySelectedPod = mPendingSelection;
                podSlider.pods[mPendingSelection].animatePod();
            }*/
        }

        @Override
        public void onPageSelected(int position) {
            PodSlider podSlider = mPodSliderRef.get();
            if (podSlider != null) {
                podSlider.setCurrentlySelectedPodAndAnimate(position);
            }
        }
    }

}
