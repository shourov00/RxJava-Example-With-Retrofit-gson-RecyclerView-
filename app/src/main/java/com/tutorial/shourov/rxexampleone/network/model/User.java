package com.tutorial.shourov.rxexampleone.network.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Shourov on 01,December,2018
 * User response once the device is registered. For now this model will have apiKey only.
 */
public class User extends BaseRespose {

    @SerializedName("api_key")
    private String apikey;

    public String getApikey() {
        return apikey;
    }
}
