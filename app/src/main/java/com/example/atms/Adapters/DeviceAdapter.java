package com.example.atms.Adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.example.atms.Models.DeserializedModels.Device;
import com.example.atms.R;

import org.w3c.dom.Text;

import java.util.List;

public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>{

    private List<Device> devices;
    private Context context;

    public DeviceAdapter(List<Device> devices, Context context) {
        this.devices = devices;
        this.context = context;
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder{

        TextView deviceName, temp, hum, oxy, lastSeen;
        Switch aSwitch;

        public DeviceViewHolder(View view) {
            super(view);
            deviceName = view.findViewById(R.id.device_key);
            temp = view.findViewById(R.id.temp);
            hum = view.findViewById(R.id.hum);
            oxy = view.findViewById(R.id.oxy);
            lastSeen = view.findViewById(R.id.last_seen);
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
        holder.deviceName.setText(device.getDeviceName());
    }

    @Override
    public int getItemCount() {
        return devices.size();
    }

}
