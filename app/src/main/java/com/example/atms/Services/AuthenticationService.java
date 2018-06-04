package com.example.atms.Services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.atms.Authorization.Token;
import com.example.atms.Models.DeserializedModels.Data;
import com.example.atms.Models.DeserializedModels.User;
import com.example.atms.Models.Login;
import com.example.atms.NetworkClient.AtmsClient;
import com.example.atms.NetworkRemoteSingleton.Remote;

import org.greenrobot.eventbus.EventBus;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AuthenticationService extends Service {

    private Retrofit retrofit;
    private AtmsClient atmsClient;
    private SharedPreferences sharedPreferences;
    private static final String TAG = "MTAG";
    public static final int LOGIN_FAILED = 0;
    public static final int LOGIN_SUCCESS = 1;
    public static final int LOGOUT_FAILED = 2;
    public static final int LOGOUT_SUCCESS = 3;

    public AuthenticationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        retrofit = Remote.getRetrofit();
        atmsClient = retrofit.create(AtmsClient.class);
        sharedPreferences = getApplicationContext().getSharedPreferences("login",MODE_PRIVATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent.getAction().equals("com.example.atms.ACTION_LOGIN")){
            login(intent.getExtras().getString("email"),intent.getExtras().getString("password"));
        }else if(intent.getAction().equals("com.example.atms.ACTION_LOGOUT")){
            Log.d(TAG, "onStartCommand: " + sharedPreferences.getString("token",""));
            logout(sharedPreferences.getString("token",""));
        }

        return START_NOT_STICKY;
    }

    private void login(String email, String password) {
        Call<User> call = atmsClient.login(new Login(email,password));
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, @Nullable Response<User> response) {
                if(response.isSuccessful()){
                    Log.d(TAG, "onResponse: Response Successfull");
                    saveUserDataInSharedPreferences(response.body().getData());
                    EventBus.getDefault().post(LOGIN_SUCCESS);
                }else{
                    Log.d(TAG, "onResponse: Response Unsuccessful");
                    EventBus.getDefault().post(LOGIN_FAILED);
                }
                stopSelf();
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.d(TAG, "onResponse: Response Failure");
                EventBus.getDefault().post(LOGIN_FAILED);
                stopSelf();
            }
        });
    }

    public void logout(String token){
        Call<Void> call = atmsClient.logout("Bearer " + token);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()){
                    Log.d(TAG, "onResponse: Logout Success");
                    EventBus.getDefault().post(LOGOUT_SUCCESS);
                    sharedPreferences.edit().clear().apply();
                }else{
                    Log.d(TAG, "onResponse: Logout Unsuccessfull");
                    EventBus.getDefault().post(LOGOUT_FAILED);
                }
                stopSelf();
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d(TAG, "onFailure: Logout Failure");
                EventBus.getDefault().post(LOGOUT_FAILED);
                stopSelf();
            }
        });
    }

    private void saveUserDataInSharedPreferences(Data data) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("user_id",data.getId());
        editor.putString("token",data.getApiToken());
        editor.putString("name",data.getName());
        editor.putString("email",data.getEmail());
        editor.putString("image",data.getImage());
        editor.putString("contact",data.getContact());
        editor.putString("address",data.getAddress());
        editor.apply();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: Authentication Service Destroyed");
    }
}














