package com.example.atms.NetworkRemoteSingleton;

import android.app.DownloadManager;
import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Remote {

    private static final String BASE_URL;
    private static Retrofit retrofit;
    public static final String IP = "http://192.168.10.10:8080/atms/";
    private static final String STORAGE_URL;
    public static final String USER_IMAGES_URL;
    private static final String TAG = "MTAG";

    static {
        BASE_URL = IP + "public/api/";
        STORAGE_URL = IP + "storage/app/";
        USER_IMAGES_URL = "public/uploads/users/";
        retrofit = null;
    }

    public static Retrofit getRetrofit(){
        if(retrofit == null){
            retrofit = new Retrofit.Builder().baseUrl(BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
        }
        return retrofit;
    }

}
