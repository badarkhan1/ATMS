package com.example.atms.Adapters;


import android.content.Context;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.atms.Models.DeserializedModels.Device;
import com.example.atms.Models.EventBusModels.DeviceResponse;
import com.example.atms.NetworkClient.AtmsClient;
import com.example.atms.NetworkRemoteSingleton.Remote;
import com.example.atms.R;


import org.greenrobot.eventbus.EventBus;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>{

    private List<Device> devices;
    private Context context;
    private Retrofit retrofit;
    private AtmsClient atmsClient;
    private static final String TAG = "MTAG";
    private SimpleDateFormat sdf;
    private long time = 0;
    private CharSequence ago;

    public DeviceAdapter(List<Device> devices, Context context) {
        this.devices = devices;
        this.context = context;
        retrofit = Remote.getRetrofit();
        atmsClient = retrofit.create(AtmsClient.class);
        sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        int device_id;
        TextView deviceName, temp, hum, oxy, lastSeen,device_mac,total_readings,live;
        Switch aSwitch;
        ImageView device_menu;

        public DeviceViewHolder(View view) {
            super(view);
            deviceName = view.findViewById(R.id.device_key);
            device_mac = view.findViewById(R.id.device_mac);
            total_readings = view.findViewById(R.id.total_readings);
            temp = view.findViewById(R.id.temp);
            hum = view.findViewById(R.id.hum);
            oxy = view.findViewById(R.id.oxy);
            aSwitch = view.findViewById(R.id.device_switch);
            lastSeen = view.findViewById(R.id.last_seen);
            live = view.findViewById(R.id.live);
            device_menu = view.findViewById(R.id.card_menu);
            aSwitch.setOnClickListener(this);
            device_menu.setOnClickListener(this);
            live.setOnClickListener(this);
        }

        @Override
        public void onClick(final View view) {
            if(view.getId() == R.id.card_menu){
                EventBus.getDefault().post(new DeviceResponse(this.device_mac.getText().toString(),this.device_id,"device-options"));
            }else if (view.getId() == R.id.device_switch){
                aSwitch.setEnabled(false);
                Call<Void> call = atmsClient.setDeviceMode(device_id,!aSwitch.isChecked() ? 0 : 1);
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {

                        if(response.isSuccessful()){
                            Log.d(TAG, "onResponse: " + "Device mode set successfull");
                            String actionPerformed;
                            if (!aSwitch.isChecked()){
                                aSwitch.setChecked(false);
                                live.setEnabled(false);
                                actionPerformed = "Device has been turned off";
                            }else{
                                aSwitch.setChecked(true);
                                live.setEnabled(true);
                                actionPerformed = "Device has been turned on";
                            }
                            aSwitch.setEnabled(true);
                            Snackbar.make(view,actionPerformed,Snackbar.LENGTH_LONG).show();
                        }else{
                            Log.d(TAG, "onResponse: " + "Device mode set unsuccessfull");
                            aSwitch.setEnabled(true);
                            Snackbar.make(view,"Oops! seems like there was a problem",Snackbar.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.d(TAG, "onResponse: " + "Device mode set failed");
                        aSwitch.setEnabled(true);
                        Snackbar.make(view,"Oops! Looks like something went wrong",Snackbar.LENGTH_LONG).show();
                    }
                });
            }else if(view.getId() == R.id.live){
                if(aSwitch.isChecked()){
                    EventBus.getDefault().post(new DeviceResponse(device_mac.getText().toString(),device_id,"update-shared-preference"));
                }
            }
        }
    }

    @Override
    public DeviceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_device_layout,parent,false);
        return new DeviceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DeviceViewHolder holder, int position) {
        Device device = devices.get(position);
        holder.device_id = device.getId();
        holder.total_readings.setText(String.valueOf(device.getReadingsCount()));
        holder.device_mac.setText(device.getDeviceKey());
        holder.deviceName.setText(device.getDeviceName());
        holder.temp.setText(device.getReadings().size() != 0 ? String.valueOf(device.getReadings().get(0).getTemp())  : "0.00");
        holder.hum.setText(device.getReadings().size() != 0 ? String.valueOf(device.getReadings().get(0).getHum())    : "0.00");
        holder.oxy.setText(device.getReadings().size() != 0 ? String.valueOf(device.getReadings().get(0).getOxygen()) : "0.00");
        holder.aSwitch.setChecked(device.getMode() == 1);
        if (holder.aSwitch.isChecked()){
            holder.live.setEnabled(true);
        }else{
            holder.live.setEnabled(false);
        }
        if(!device.getReadings().isEmpty()){
            try {
                time = sdf.parse(device.getReadings().get(0).getCreatedAt()).getTime();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            long now = System.currentTimeMillis();
            ago = DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
        }else{
            ago = ".....";
        }
        holder.lastSeen.setText(ago);
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

}
