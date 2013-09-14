package com.clarity.glassviewer;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

public class GlassGestureListener extends GestureDetector.SimpleOnGestureListener {
    private static final int SWIPE_MIN_DISTANCE = 100;
    private static final int SWIPE_THRESHOLD_VELOCITY = 1000;

    @Override
    public boolean onFling(MotionEvent start, MotionEvent finish, float velocityX, float velocityY) {
        try {
            float totalXTraveled = finish.getX() - start.getX();
            float totalYTraveled = finish.getY() - start.getY();
            if (Math.abs(totalXTraveled) > Math.abs(totalYTraveled)) {
                if (Math.abs(totalXTraveled) > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    if (totalXTraveled > 10) {
                        Log.d("Event", "findme: On Fling Forward");

                    } else {
                        Log.d("Event", "findme: On Fling Backward");

                    }
                }
            } else {
                if (Math.abs(totalYTraveled) > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                    if(totalYTraveled > 0) {
                        Log.d("Event", "findme: On Fling Down");
                    } else {
                        Log.d("Event", "findme: On Fling Up");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Log.d("Event", "findme: On Single Tap");
        return true;
    }
}
