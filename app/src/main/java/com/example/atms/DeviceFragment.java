package com.example.atms;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.example.atms.Adapters.DeviceAdapter;
import com.example.atms.Models.DeserializedModels.Device;
import com.example.atms.NetworkClient.AtmsClient;
import com.example.atms.NetworkRemoteSingleton.Remote;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class DeviceFragment extends Fragment {

    RecyclerView recyclerView;
    ProgressBar progressBar;
    DeviceAdapter adapter;
    Retrofit retrofit;
    AtmsClient atmsClient;
    private static final String TAG = "MTAG";
    SwipeRefreshLayout refreshLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        retrofit = Remote.getRetrofit();
        atmsClient = retrofit.create(AtmsClient.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_device, container, false);
        recyclerView = view.findViewById(R.id.device_recycler);
        progressBar = view.findViewById(R.id.device_pro);
        refreshLayout = view.findViewById(R.id.refresh_layout);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });
        return view;
    }

    private void refreshContent() {
        Call<ArrayList<Device>> call = atmsClient.getUserDevices(getContext().getSharedPreferences("login",Context.MODE_PRIVATE).getInt("user_id",0),
                "Bearer " + getContext().getSharedPreferences("login",Context.MODE_PRIVATE).getString("token",""));
        call.enqueue(new Callback<ArrayList<Device>>() {
            @Override
            public void onResponse(Call<ArrayList<Device>> call, Response<ArrayList<Device>> response) {
                if(response.isSuccessful()){
                    Log.d(TAG, "onResponse: " + "Device response successfull");
                    adapter = new DeviceAdapter(response.body(),getContext());
                    LinearLayoutManager manager = new LinearLayoutManager(getContext());
                    recyclerView.setLayoutManager(manager);
                    recyclerView.setAdapter(adapter);
                    recyclerView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }else{
                    Log.d(TAG, "onResponse: " + "Device response unsuccessfull");
                }
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<ArrayList<Device>> call, Throwable t) {
                Log.d(TAG, "onFailure: " + "Device response failed");
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        refreshContent();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((DrawerActivity)getActivity()).setNavigationItemChecked(2);
    }

}











