package com.example.atms;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class DeviceFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_device, container, false);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ((DrawerActivity)getActivity()).setNavigationItemChecked(2);
    }
}
