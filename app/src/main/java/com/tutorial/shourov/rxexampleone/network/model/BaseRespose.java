package com.tutorial.shourov.rxexampleone.network.model;

/**
 * Created by Shourov on 01,December,2018
 *  As every response will have a error node,
 *  we define the error node in BaseResponse class and extend this class in other models.
 */
public class BaseRespose {
    private String error;

    public String getError() {
        return error;
    }
}
