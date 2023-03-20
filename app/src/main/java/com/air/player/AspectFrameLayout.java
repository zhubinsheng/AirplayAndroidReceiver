package com.air.player;

import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.widget.FrameLayout;

/**
 * Created by Administrator on 2017/3/12 0012.
 */

public class AspectFrameLayout extends SurfaceView {

    private float mTargetAspect = -1.0f;

    public AspectFrameLayout(Context context) {
        super(context);
    }

    public AspectFrameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AspectFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setAspectRatio(float aspectRatio) {
        if (aspectRatio < 0) {
            throw new IllegalArgumentException("aspect ratio < 0");
        }
        if (mTargetAspect != aspectRatio) {
            mTargetAspect = aspectRatio;
            requestLayout();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mTargetAspect > 0) {
            int originWidth = MeasureSpec.getSize(widthMeasureSpec);
            int originHeight = MeasureSpec.getSize(heightMeasureSpec);

            int hPadding = getPaddingLeft() + getPaddingRight();
            int vPadding = getPaddingTop() + getPaddingBottom();

            originWidth -= hPadding;
            originHeight -= vPadding;

            float viewAspectRatio = originWidth * 1.0f / originHeight;
            float aspectDiff = mTargetAspect / viewAspectRatio - 1;
            if (Math.abs(aspectDiff) < 0.01) {

            } else {
                if (aspectDiff > 0) {
                    //width / height too large
                    originHeight = (int) (originWidth / mTargetAspect);
                } else {
                    originWidth = (int) (originHeight * mTargetAspect);
                }
                originHeight += vPadding;
                originWidth += hPadding;
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(originWidth, MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(originHeight, MeasureSpec.EXACTLY);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}