package com.example.android.booklisting;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * This class is from a stack overflow post, which handle horizontal swipe gestures.
 * The link is as below.
 * https://stackoverflow.com/questions/4139288/android-how-to-handle-right-to-left-swipe-gestures
 */
public class OnSwipeTouchListener implements OnTouchListener {

    private final GestureDetector mGestureDetector;

    public OnSwipeTouchListener(Context context) {
        mGestureDetector = new GestureDetector(context, new GestureListener());
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        v.performClick();
        return mGestureDetector.onTouchEvent(event);
    }

    private final class GestureListener extends SimpleOnGestureListener {

        private static final int SWIPE_THRESHOLD = 10;
        private static final int SWIPE_VELOCITY_THRESHOLD = 10;

        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            boolean result = false;
            try {
                float diffY = e2.getY() - e1.getY();
                float diffX = e2.getX() - e1.getX();
                if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD &&
                            Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                        if (diffX > 0) {
                            onSwipeRight();
                        } else {
                            onSwipeLeft();
                        }
                        result = true;
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return result;
        }
    }

    public void onSwipeRight() {
    }

    public void onSwipeLeft() {
    }
}
