package com.tutorial.shourov.rxexampleone.network.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Shourov on 01,December,2018
 * Defines the note object with id, note and timestamp fields
 */
public class Note extends BaseRespose {
    @SerializedName("id")
    private int mId;

    @SerializedName("note")
    private String mNote;

    @SerializedName("timestamp")
    private String mTimeStamp;

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getNote() {
        return mNote;
    }

    public void setNote(String note) {
        mNote = note;
    }

    public String getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        mTimeStamp = timeStamp;
    }
}
