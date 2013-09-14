package com.clarity.glassviewer;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class FullscreenActivity extends Activity {

    private GestureDetector mGestureDetector;
    private GlassGestureListener mGlassGestureListener;

    private Bitmap[] mSlideBitmaps;
    private ImageView mImageView;
    private ImageView mThumbnailView;
    private int mNumImagesLoaded = 0;
    private int mCurrentSlide = 0;
    private boolean mDisplayPreview = true;

    private boolean imagesLoaded() {
        return mNumImagesLoaded == mSlideBitmaps.length;
    }

    private void startPresentation() {

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mImageView = (ImageView) findViewById(R.id.imageView);
        mThumbnailView = (ImageView) findViewById(R.id.previewThumbnail);
        mGlassGestureListener = new GlassGestureListener();
        mSlideBitmaps = new Bitmap[2];

        mGestureDetector = new GestureDetector(this, mGlassGestureListener);

        String[] urls = {
                "http://yoshi.2yr.net/pics/yoshis-story-yoshi.png",
                "http://pad3.whstatic.com/images/thumb/0/07/MarioNintendoImage.png/350px-MarioNintendoImage.png",
                "http://images3.wikia.nocookie.net/__cb20120116195460/fantendo/images/2/20/SM3DL2_Luigi.png",
                "http://images2.wikia.nocookie.net/__cb20130525205357/jadensadventures/images/c/c4/7674397_display.png",
                "http://images.wikia.com/mariofanon/images/c/c9/Toad.png",
                "http://images.wikia.com/fantendo/images/archive/9/95/20090714164416!200px-Koopa.png",
        };

        mSlideBitmaps = new Bitmap[urls.length];
        for (int i = 0; i < urls.length; i++) {
            new DownloadImageTask(i).execute(urls[i]);
        }

        new StartPresentationTask().execute();


    }

    private void renderSlide() {
        // Relies on mCurrentSlide and mDisplayPreview
        if (!mDisplayPreview || mCurrentSlide + 1 == mSlideBitmaps.length) {
            mThumbnailView.setVisibility(View.GONE);
        }
        else if (mCurrentSlide + 1 < mSlideBitmaps.length) {
            mThumbnailView.setVisibility(View.VISIBLE);
            mThumbnailView.setImageBitmap(mSlideBitmaps[mCurrentSlide+1]);
        }

        mImageView.setVisibility(View.VISIBLE);
        mImageView.setImageBitmap(mSlideBitmaps[mCurrentSlide]);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
           Log.i("Keycodes down", "keyCode: " + keyCode);
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.i("Keycodes up", "keyCode: " + keyCode);
        return false;
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }


    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return true;
    }

    private class StartPresentationTask extends AsyncTask<Void, Void, Void> {

        public StartPresentationTask() {
            super();
        }

        @Override
        protected Void doInBackground(Void... voids) {

            String initURL = "http://clarity-uho.appspot.com/api/glass";

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(initURL);

            try {
//                Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("action", "init"));
                nameValuePairs.add(new BasicNameValuePair("id", "5733953138851840"));
                try {
                    UrlEncodedFormEntity uefe = new UrlEncodedFormEntity(nameValuePairs);
                    httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                    HttpResponse response = httpclient.execute(httppost);
                    Log.i("RESPONSE", "sigh... " + response.toString());

                    BufferedReader reader = new BufferedReader(new InputStreamReader(
                            response.getEntity().getContent(), "UTF-8"));
                    String json = reader.readLine();
                    // Instantiate a JSON object from the request response
                    JSONArray jsonArray = new JSONArray(json);

                } catch(Exception e) {
                    Log.d("Exception", e.toString());
                }

                // Execute HTTP Post Request

            } catch (Exception e) {
                // TODO Auto-generated catch block
            }

            return null;
        }
    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

        private int mIndex;

        public DownloadImageTask(int bitmapIndex) {
            mIndex = bitmapIndex;
        }

        protected Bitmap doInBackground(String... urls) {
            Log.d("Async", "findme: doInBackground on " + mIndex);
            String urldisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }
            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            mSlideBitmaps[mIndex] = result;
            mNumImagesLoaded++;

            Log.d("Async", "findme: onPostExecute: " + mIndex);

            if (imagesLoaded()) {
                Log.i("Async", "findme: Finished loading all images.");
                mCurrentSlide = 0;
                renderSlide();
            }
        }
    }

    private class GlassGestureListener extends GestureDetector.SimpleOnGestureListener {
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
                            if (imagesLoaded() && mCurrentSlide + 1 < mSlideBitmaps.length) {
                                mCurrentSlide++;
                                renderSlide();
                            }

                        } else {
                            Log.d("Event", "findme: On Fling Backward");
                            if (imagesLoaded() && mCurrentSlide > 0) {
                                mCurrentSlide--;
                                renderSlide();
                            }
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
            mDisplayPreview = !mDisplayPreview;
            renderSlide();
            return true;
        }

    }
}
