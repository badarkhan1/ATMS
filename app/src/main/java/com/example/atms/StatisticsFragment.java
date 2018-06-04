package com.example.atms;


import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.atms.Models.DeserializedModels.StatisticalData;
import com.example.atms.NetworkClient.AtmsClient;
import com.example.atms.NetworkRemoteSingleton.Remote;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class StatisticsFragment extends Fragment {

    TextView ownedDevices;
    TextView totalReadings;
    TextView readingsToday;
    TextView maxTempToday;
    TextView sensorDevices;
    TextView activeDevices;
    TextView maxHumToday;
    TextView actuatorDevices;
    NestedScrollView scrollView;
    ProgressBar statPro;
    SwipeRefreshLayout refreshLayout;
    Retrofit retrofit = Remote.getRetrofit();
    AtmsClient atmsClient = retrofit.create(AtmsClient.class);
    private static final String TAG = "MTAG";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_statistics, container, false);
        ownedDevices = (TextView) view.findViewById(R.id.owned_devices);
        totalReadings = (TextView) view.findViewById(R.id.total_readings);
        readingsToday = (TextView) view.findViewById(R.id.readings_today);
        maxTempToday = (TextView) view.findViewById(R.id.max_temp_today);
        sensorDevices = (TextView) view.findViewById(R.id.sensor_devices);
        activeDevices = (TextView) view.findViewById(R.id.active_devices);
        maxHumToday = (TextView) view.findViewById(R.id.max_hum_today);
        actuatorDevices = (TextView) view.findViewById(R.id.act_devices);
        scrollView = (NestedScrollView) view.findViewById(R.id.scrollView);
        statPro = (ProgressBar) view.findViewById(R.id.stat_pro);
        scrollView.setVisibility(View.GONE);
        refreshLayout = view.findViewById(R.id.stat_refresh);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });

        return view;
    }

    private void refreshContent() {
        Call<StatisticalData> call = atmsClient.getStatisticalData(getContext().getSharedPreferences("login", Context.MODE_PRIVATE).getInt("user_id",0));
        call.enqueue(new Callback<StatisticalData>() {
            @Override
            public void onResponse(@Nullable Call<StatisticalData> call, Response<StatisticalData> response) {
                if(response.isSuccessful() && response.body() !=null){
                    Log.d(TAG, "onResponse: " + "statistics response successfull");
                    String maxTemp , maxHum;
                    maxTemp = response.body().getMaxTempToday() == null ? "waiting..." : String.valueOf(response.body().getMaxTempToday());
                    maxHum = response.body().getMaxHumToday() == null ? "waiting..." : String.valueOf(response.body().getMaxHumToday());

                    ownedDevices.setText(String.valueOf(response.body().getDevicesOwned()));
                    totalReadings.setText(String.valueOf(response.body().getTotalReadings()));
                    readingsToday.setText(String.valueOf(response.body().getTotalReadingsToday()));
                    maxTempToday.setText(maxTemp);
                    sensorDevices.setText(String.valueOf(response.body().getTotalSensorDevices()));
                    activeDevices.setText(String.valueOf(response.body().getTotalActiveDevices()));
                    maxHumToday.setText(maxHum);
                    actuatorDevices.setText(String.valueOf(response.body().getTotalActuatorDevices()));
                    statPro.setVisibility(View.GONE);
                    scrollView.setVisibility(View.VISIBLE);
                }else{
                    Log.d(TAG, "onResponse: " + "statistics response unsuccessfull");
                }
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<StatisticalData> call, Throwable t) {
                Log.d(TAG, "onFailure: " + "statistics response failed");
                refreshLayout.setRefreshing(false);
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
        ((DrawerActivity)getActivity()).setNavigationItemChecked(0);
    }
}








