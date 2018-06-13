package com.example.atms;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;

import com.example.atms.Models.EventBusModels.DeviceResponse;

import org.greenrobot.eventbus.EventBus;

public class DeviceOptionsBottomFragment extends BottomSheetDialogFragment implements View.OnClickListener , View.OnTouchListener{

    LinearLayout root , year , constraints;
    DeviceResponse deviceResponse;
    private static final String TAG = "MTAG";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.device_options,container,false);
        root = view.findViewById(R.id.device_root);
        year = view.findViewById(R.id.this_year);
        constraints = view.findViewById(R.id.constraintss);
        year.setOnTouchListener(this);
        constraints.setOnTouchListener(this);
        year.setOnClickListener(this);
        constraints.setOnClickListener(this);
        deviceResponse = getArguments().getParcelable("device-response");
        return view;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        if(view.getId() != R.id.device_root){
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                view.setBackgroundColor(getResources().getColor(R.color.pressed));
            }
        }

        return false;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.this_year){
            Log.d(TAG, "onClick: " + "Yearly Average");
            Intent intent = new Intent(getActivity(),YearlyDataActivity.class);
            intent.putExtra("dev-mac",deviceResponse.getMac());
            intent.putExtra("dev-id",deviceResponse.getId());
            startActivity(intent);
            this.dismiss();
        }else if(view.getId() == R.id.constraintss){
            Log.d(TAG, "onClick: " + "Constraints");
            EventBus.getDefault().post(new DeviceResponse(deviceResponse.getMac(),deviceResponse.getId(),"con-dialogue"));
            this.dismiss();
        }
    }
}
















