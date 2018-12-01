package com.tutorial.shourov.rxexampleone.network;

import android.content.Context;
import android.text.TextUtils;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.tutorial.shourov.rxexampleone.app.Const;
import com.tutorial.shourov.rxexampleone.utils.PreUtils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Shourov on 01,December,2018
 */
public class ApiClient {
    private static OkHttpClient sOkHttpClient;
    private static Retrofit sRetrofit = null;
    private static int REQUEST_TIMEOUT = 60;

    public static Retrofit getClient(Context context) {

        if (sOkHttpClient == null)
            initOkHttp(context);

        //add retrofit
        if (sRetrofit == null) {
            sRetrofit = new Retrofit.Builder()
                    .baseUrl(Const.BASE_URL)
                    .client(sOkHttpClient)
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return sRetrofit;
    }

    public static void initOkHttp(Context context) {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .connectTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(REQUEST_TIMEOUT, TimeUnit.SECONDS);

        //An OkHttp interceptor which logs request and response information
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        httpClient.addInterceptor(loggingInterceptor);

        httpClient.addInterceptor(chain -> {
            Request originals = chain.request();
            Request.Builder builder = originals.newBuilder()
                    .addHeader("Accept", "application/json")
                    .addHeader("Content-Type", "application/json");

            // Adding Authorization token (API Key)
            // Requests will be denied without API key
            if (!TextUtils.isEmpty(PreUtils.getApiKey(context))) {
                builder.addHeader("Authorization", PreUtils.getApiKey(context));
            }

            Request request = builder.build();
            return chain.proceed(request);
        });

        sOkHttpClient = httpClient.build();
    }
}
