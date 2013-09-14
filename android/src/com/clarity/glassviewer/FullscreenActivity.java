package com.clarity.glassviewer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

public class FullscreenActivity extends Activity {
	private static final String USER_AGENT = "Mozilla/5.0";
	  
    private GestureDetector mGestureDetector;
    private GlassGestureListener mGlassGestureListener;
    private Chronometer mChronometer;
    private Slide[] mSlides;
    private ImageView mImageView;
    private TextView mTextView;
    private ImageView mThumbnailView;
    private int mNumImagesLoaded = 0;
    private int mCurrentSlide = 0;
    private boolean mDisplayPreview = true;
    private boolean mDisplayNotes = false;
    private String mPresenterID = "ab60dc96-dcb8-4e53-afed-fd23c63b4476";
    private String mPresentationID = "5733953138851840";
    private TextView mSlideNumberView;
    private View mSlideNumberWrapper;

    private boolean imagesLoaded() {
        return mNumImagesLoaded == mSlides.length;
    }

    private void goToSlide(int slideNo) {
        new SlideChangeTask(mSlides[slideNo].getPage_id()).execute();
        mCurrentSlide = slideNo;
        renderSlide();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getActionBar().hide();
        
        String extra = getIntent().getStringExtra("qr");
        int pipeIndex = extra.indexOf('|');
        mPresentationID = extra.substring(0, pipeIndex);
        mPresenterID = extra.substring(pipeIndex + 1);
        Log.d("FULLSCREEN", "findme: mPresentationID = " + mPresentationID);
        Log.d("FULLSCREEN", "findme: mPresenterID = " + mPresenterID);
        
        setContentView(R.layout.activity_fullscreen);

        mImageView = (ImageView) findViewById(R.id.imageView);
        mThumbnailView = (ImageView) findViewById(R.id.previewThumbnail);
        mChronometer = (Chronometer) findViewById(R.id.timer);
        mChronometer.setVisibility(View.INVISIBLE);
        mTextView = (TextView) findViewById(R.id.textView);
        mSlideNumberView = (TextView) findViewById(R.id.slideNumber);
        mSlideNumberWrapper = findViewById(R.id.slideNumWrapper);


        mGlassGestureListener = new GlassGestureListener();

        mGestureDetector = new GestureDetector(this, mGlassGestureListener);

        new StartPresentationTask().execute();

        Log.d("FullscreenActivity", "onCreate complete");
    }


    private void renderSlide() {

    	mSlideNumberWrapper.setVisibility(View.VISIBLE);
    	
        mSlideNumberView.setText((mCurrentSlide + 1) + " / " + mSlides.length);


        if (!mDisplayPreview || mCurrentSlide + 1 == mSlides.length) {
            mThumbnailView.setVisibility(View.GONE);
        }
        else if (mCurrentSlide + 1 < mSlides.length) {
            mThumbnailView.setVisibility(View.VISIBLE);
            mThumbnailView.setImageBitmap(mSlides[mCurrentSlide+1].getBitmap());
        }

        if (mDisplayNotes) {
            mTextView.setVisibility(View.VISIBLE);
            mImageView.setVisibility(View.GONE);
            mTextView.setText(mSlides[mCurrentSlide].getSpeaker_notes());
            mThumbnailView.setVisibility(View.GONE);
        }
        else {
            mTextView.setVisibility(View.GONE);
            mImageView.setVisibility(View.VISIBLE);
            mImageView.setImageBitmap(mSlides[mCurrentSlide].getBitmap());
        }

    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        Log.d("FullscreenActivity", "generic! generic! generic!");
        return true;
    }

    private HttpResponse httpPost(String initURL, List<NameValuePair> nameValuePairs) {
        HttpClient httpclient = new DefaultHttpClient();
        try {
                HttpPost httppost = new HttpPost(initURL);
                UrlEncodedFormEntity uefe = new UrlEncodedFormEntity(nameValuePairs);
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);
                return response;

        } catch(Exception e) {
                Log.d("Exception", e.toString());
        }
        return null;
    }

    private HttpResponse httpGet(String initURL) {
        HttpClient httpclient = new DefaultHttpClient();
        try {

            HttpGet httpGet = new HttpGet(initURL);
            HttpResponse response = httpclient.execute(httpGet);
            return response;

        } catch(Exception e) {
                Log.d("Exception", e.toString());
        }
        return null;
    } 

    private class SlideChangeTask extends AsyncTask<Void, Void, Void> {

        private String mPageID;

        public SlideChangeTask(String pageid) {
            mPageID = pageid;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String initURL = "http://clarity-uho.appspot.com/api/controller";
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
            nameValuePairs.add(new BasicNameValuePair("page_id", mPageID));
            nameValuePairs.add(new BasicNameValuePair("presenter_id", mPresenterID));
            nameValuePairs.add(new BasicNameValuePair("presentation_id", mPresentationID));
            httpPost(initURL, nameValuePairs);
            return null;
        }
    }

    private class StartPresentationTask extends AsyncTask<Void, Void, Void> {

        public StartPresentationTask() {
            super();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            
            for (int i = 0; i < mSlides.length; i++) {
            	new DownloadImageTask(i).execute(mSlides[i].getImg_url());
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            String initURL = "http://clarity-uho.appspot.com/api/controller/" +
                    mPresentationID + "?presenter_id=" + mPresenterID;
            HttpResponse response = httpGet(initURL);
 
         BufferedReader reader;
         String json;
    		try {
    			reader = new BufferedReader(new InputStreamReader(
    			         response.getEntity().getContent(), "UTF-8"));
    			json = reader.readLine();
    			Log.d("GSON", json);
    	         
    			Gson gson = new Gson();
    	        JsonParser parser = new JsonParser();
    	        JsonObject jsonObject = parser.parse(json).getAsJsonObject();
    	        JsonArray slides = jsonObject.getAsJsonArray("slides");
    	        mSlides = new Slide[slides.size()];
    	        for (int i = 0; i < slides.size(); i++) {
    	            mSlides[i] = gson.fromJson(slides.get(i), Slide.class);
    	            Log.d("SLIDES", "Slide = " + mSlides[i]);
    	        }
    		} catch (UnsupportedEncodingException e) {
    			e.printStackTrace();
    		} catch (IllegalStateException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
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
            mSlides[mIndex].setBitmap(result);
            mNumImagesLoaded++;

            if (imagesLoaded()) {
                Log.i("Async", "findme: Finished loading all images.");
                goToSlide(0);
                mChronometer.setVisibility(View.VISIBLE);
                mChronometer.start();
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
                            if (imagesLoaded() && mCurrentSlide + 1 < mSlides.length) {
                                goToSlide(mCurrentSlide + 1);
                            }

                        } else {
                            Log.d("Event", "findme: On Fling Backward");
                            if (imagesLoaded() && mCurrentSlide > 0) {
                                goToSlide(mCurrentSlide - 1);
                            }
                        }
                    }
                } else {
                    if (Math.abs(totalYTraveled) > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
                        if(totalYTraveled > 0) {
                            Log.d("Event", "findme: On Fling Down");
                            //mDisplayNotes = false;
                            //renderSlide();
                        } else {
                            Log.d("Event", "findme: On Fling Up");
                            mDisplayNotes = !mDisplayNotes;
                            renderSlide();
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
