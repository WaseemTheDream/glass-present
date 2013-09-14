package com.clarity.glassviewer;

import com.clarity.glassviewer.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MotionEvent;

public class MainActivity extends Activity {

	private GestureDetector mGestureDetector;
	private GlassGestureListener mGlassGestureListener;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mGlassGestureListener = new GlassGestureListener();
		mGestureDetector = new GestureDetector(this, mGlassGestureListener);
		
		Log.d("MainActivity", "mGestureDector = " + mGestureDetector.toString());
		Log.d("MainActivity", "mGlassGestureListener = " + mGlassGestureListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
    	Log.d("Event", "onGenericMotionEvent");
        mGestureDetector.onTouchEvent(event);
        return true;
    }
}
