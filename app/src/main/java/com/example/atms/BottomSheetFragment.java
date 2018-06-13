package com.example.atms;


import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.atms.Services.AuthenticationService;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import static android.content.Context.MODE_PRIVATE;

public class BottomSheetFragment extends BottomSheetDialogFragment implements View.OnClickListener, View.OnTouchListener{

    LinearLayout layout,root;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.bottom_sheet_menu,container,false);

        layout = view.findViewById(R.id.logout);
        root = view.findViewById(R.id.root_layout);
        layout.setOnTouchListener(this);
        layout.setOnClickListener(this);
        return view;
    }

    public void attemptLogout(){
//        getActivity().getSharedPreferences("device",MODE_PRIVATE).edit().putString("device-mac","60:01:94:37:D1:34").apply();
//        getActivity().getSharedPreferences("device",MODE_PRIVATE).edit().putString("device-id","1").apply();
//        getActivity().getSharedPreferences("login",MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(getActivity(),AuthenticationService.class);
        intent.setAction("com.example.atms.ACTION_LOGOUT");
        getActivity().startService(intent);
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.logout){
            attemptLogout();
            this.dismiss();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        if(view.getId() == R.id.logout){
            if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                root.setBackgroundColor(getResources().getColor(R.color.pressed));
            }
        }

        return false;
    }
}













