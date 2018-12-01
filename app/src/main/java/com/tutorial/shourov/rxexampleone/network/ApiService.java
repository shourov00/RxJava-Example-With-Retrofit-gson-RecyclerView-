package com.tutorial.shourov.rxexampleone.network;

import com.tutorial.shourov.rxexampleone.network.model.Note;
import com.tutorial.shourov.rxexampleone.network.model.User;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Single;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

/**
 * Created by Shourov on 01,December,2018
 * Single: one single item can handle
 * Completable: multiple item can handle
 */
public interface ApiService {
    //register new notes
    @FormUrlEncoded
    @POST("notes/user/register")
    Single<User> register(@Field("device_id")  String deviceId);

    //create note
    @FormUrlEncoded
    @POST("notes/new")
    Single<Note> createNote(@Field("note") String note);

    //Fetch all notes
    @GET("notes/all")
    Single<List<Note>> fetchAllNotes();

    //Update Single Note
    @FormUrlEncoded
    @PUT("notes/{id}")
    Completable updateNote(@Path("id") int noteId, @Field("note") String note);

    //Delete Note
    @DELETE("notes/{id}")
    Completable deleteNote(@Path("id") int noteId);
}
