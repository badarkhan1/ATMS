package com.example.atms.NetworkClient;


import com.example.atms.Models.DeserializedModels.User;
import com.example.atms.Models.Login;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AtmsClient {

    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("login")
    Call<User> login(@Body Login login);

    @Headers({
            "Accept: application/json",
            "Content-Type: application/json"
    })
    @POST("logout")
    Call<Void> logout(@Header("Authorization") String token);
}
