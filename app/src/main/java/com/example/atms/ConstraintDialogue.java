package com.example.atms;


import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.interfaces.OnRangeSeekbarFinalValueListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalRangeSeekbar;
import com.example.atms.Models.EventBusModels.Constraint;
import com.example.atms.Models.EventBusModels.ConstraintResponse;
import com.example.atms.Models.EventBusModels.DeviceResponse;
import com.example.atms.NetworkRemoteSingleton.Remote;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.util.regex.*;

public class ConstraintDialogue extends DialogFragment implements View.OnClickListener{

    CrystalRangeSeekbar temp, hum, oxy;
    TextView temp_min , temp_max , hum_min , hum_max , oxy_min , oxy_max;
    ProgressBar progressBar;
    OkHttpClient client;
    Request request;
    private static final String TAG = "MTAG";
    private String response;
    JSONObject jsonResponse;
    JSONObject con;
    int min_temp , max_temp , min_hum , max_hum , min_oxy , max_oxy;
    Button btnDismiss , btnApply;
    String mac;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setStyle(DialogFragment.STYLE_NO_TITLE,android.R.style.Theme_Holo_Dialog_NoActionBar);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.constraint_dialogue,container,false);

        // Retrieve all texts
        temp_min = view.findViewById(R.id.min_temp);
        temp_max = view.findViewById(R.id.max_temp);

        hum_min = view.findViewById(R.id.min_hum);
        hum_max = view.findViewById(R.id.max_hum);

        oxy_min = view.findViewById(R.id.min_oxy);
        oxy_max = view.findViewById(R.id.max_oxy);

        // Retrieve all seek bars
        temp = view.findViewById(R.id.range_seekbar);
        hum  = view.findViewById(R.id.hum_seekbar);
        oxy  = view.findViewById(R.id.oxy_seekbar);

        // Set the color definitions of all three seek bars
        temp.setBarColor(Color.GRAY);
        temp.setBarHighlightColor(getResources().getColor(R.color.temp_color));

        hum.setBarColor(Color.GRAY);
        hum.setBarHighlightColor(getResources().getColor(R.color.hum_color));

        oxy.setBarColor(Color.GRAY);
        oxy.setBarHighlightColor(getResources().getColor(R.color.oxy_color));

        // Retrieve the progress bar
        progressBar = view.findViewById(R.id.con_pro);

        // Retrieve the dialogue buttons
        btnDismiss = view.findViewById(R.id.btn_dismiss);
        btnApply   = view.findViewById(R.id.btn_apply);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
//        if(response != null){
//            // set the temperature bar
//            temp.setMinValue(min_temp);
//            temp.setMaxValue(max_temp);
//            temp_min.setText(String.valueOf(min_temp));
//            temp_max.setText(String.valueOf(max_temp));
//
//            // set the humidity bar
//            hum.setMinValue(min_hum);
//            hum.setMaxValue(max_hum);
//            hum_min.setText(String.valueOf(min_hum));
//            hum_max.setText(String.valueOf(max_hum));
//
//            // set the temperature bar
//            oxy.setMinValue(min_oxy);
//            oxy.setMaxValue(max_oxy);
//            oxy_min.setText(String.valueOf(min_oxy));
//            oxy_max.setText(String.valueOf(max_oxy));
//        }

        response = getArguments().getString("response","");
        if(!response.equals("")){
            try {
                jsonResponse = new JSONObject(response);
                con = jsonResponse.getJSONObject("con");
                min_temp = con.getInt("min_temp");
                max_temp = con.getInt("max_temp");
                min_hum = con.getInt("min_hum");
                max_hum = con.getInt("max_hum");
                min_oxy = con.getInt("min_oxy");
                max_oxy = con.getInt("max_oxy");
                mac = con.getString("device_key");
                temp.setMinStartValue(min_temp).apply();
                temp.setMaxStartValue(max_temp).apply();
                hum.setMinStartValue(min_hum).apply();
                hum.setMaxStartValue(max_hum).apply();
                oxy.setMinStartValue(min_oxy).apply();
                oxy.setMaxStartValue(max_oxy).apply();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if(!getArguments().getString("mac","").equals("")) {
            mac = getArguments().getString("mac");
            temp.setMinValue(0);
            temp.setMaxValue(100);
            hum.setMinValue(0);
            hum.setMaxValue(100);
            oxy.setMinValue(0);
            oxy.setMaxValue(100);
        }

        setListenerToTemperatureBar();
        setListenerToHumisityBar();
        setListenerToOxygenBar();
        btnDismiss.setOnClickListener(this);
        btnApply.setOnClickListener(this);
    }

    private void setListenerToOxygenBar() {
        oxy.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {
                oxy_min.setText(String.valueOf(minValue));
                oxy_max.setText(String.valueOf(maxValue));
            }
        });

        oxy.setOnRangeSeekbarFinalValueListener(new OnRangeSeekbarFinalValueListener() {
            @Override
            public void finalValue(Number minValue, Number maxValue) {
                min_oxy = minValue.intValue();
                max_oxy = maxValue.intValue();
            }
        });
    }

    private void setListenerToHumisityBar() {
        hum.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {
                hum_min.setText(String.valueOf(minValue));
                hum_max.setText(String.valueOf(maxValue));
            }
        });

        hum.setOnRangeSeekbarFinalValueListener(new OnRangeSeekbarFinalValueListener() {
            @Override
            public void finalValue(Number minValue, Number maxValue) {
                min_hum = minValue.intValue();
                max_hum = maxValue.intValue();
            }
        });
    }

    private void setListenerToTemperatureBar() {
        temp.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
            @Override
            public void valueChanged(Number minValue, Number maxValue) {
                temp_min.setText(String.valueOf(minValue));
                temp_max.setText(String.valueOf(maxValue));
            }
        });

        temp.setOnRangeSeekbarFinalValueListener(new OnRangeSeekbarFinalValueListener() {
            @Override
            public void finalValue(Number minValue, Number maxValue) {
                min_temp = minValue.intValue();
                max_temp = maxValue.intValue();
            }
        });
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.btn_dismiss){
            dismiss();
        }else if (view.getId() == R.id.btn_apply){
            EventBus.getDefault().post(new ConstraintResponse("apply",response,new Constraint(min_temp,max_temp,min_hum,max_hum,min_oxy,max_oxy,mac)));
        }
    }
}














