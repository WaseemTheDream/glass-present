package com.clarity.glassviewer;

import android.graphics.Bitmap;

/**
 * Created by bonnie on 9/14/13.
 */

public class Slide {

    private String speaker_notes;
    private String page_id;
    private String img_url;
    private Bitmap bitmap;

    public Slide(String speaker_notes, String page_id, String img_url) {
        this.speaker_notes = speaker_notes;
        this.page_id = page_id;
        this.img_url = img_url;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
    }

    public String getSpeaker_notes() {
        return speaker_notes;
    }

    public void setSpeaker_notes(String speaker_notes) {
        this.speaker_notes = speaker_notes;
    }

    public String getPage_id() {
        return page_id;
    }

    public void setPage_id(String page_id) {
        this.page_id = page_id;
    }

    public String getImg_url() {
        return img_url;
    }

    public void setImg_url(String img_url) {
        this.img_url = img_url;
    }

}