package com.example.atms.NetworkClient;


import android.support.annotation.Nullable;

import com.example.atms.Models.DeserializedModels.AccountUpdateResponse;
import com.example.atms.Models.DeserializedModels.Device;
import com.example.atms.Models.DeserializedModels.StatisticalData;
import com.example.atms.Models.DeserializedModels.User;
import com.example.atms.Models.Login;

import java.util.ArrayList;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

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

    @GET("devices/{user_id}")
    Call<ArrayList<Device>> getUserDevices(@Path("user_id") int user_id , @Header("Authorization") String token);

    @GET("statistics/{user_id}")
    Call<StatisticalData> getStatisticalData(@Path("user_id") int user_id);

    @POST("mode/{device_id}/{mode}")
    Call<Void> setDeviceMode(@Path("device_id") int device_id, @Path("mode") int mode);

    @Multipart
    @POST("update/account/{user_id}")
    Call<AccountUpdateResponse> updateUserAccountInfo(@Path("user_id") int user_id, @Header("Authorization") String apiToken,
                                                      @Nullable @Part MultipartBody.Part image,
                                                      @Part("name") RequestBody name,
                                                      @Part("email") RequestBody email,
                                                      @Part("contact") RequestBody contact,
                                                      @Part("address") RequestBody address);
}














