package com.example.atms.Models.EventBusModels;


import android.os.Parcel;
import android.os.Parcelable;

public class DeviceResponse implements Parcelable{

    private String mac;
    private int id;
    private String action;

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public DeviceResponse(String mac, int id, String action) {
        this.mac = mac;
        this.id = id;
        this.action = action;
    }

    public DeviceResponse(Parcel parcel){
        this.mac = parcel.readString();
        this.id = parcel.readInt();
        this.action = parcel.readString();
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public static final Parcelable.Creator CREATOR  = new Parcelable.Creator<DeviceResponse>(){

        @Override
        public DeviceResponse createFromParcel(Parcel parcel) {
            return new DeviceResponse(parcel);
        }

        @Override
        public DeviceResponse[] newArray(int i) {
            return new DeviceResponse[i];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.mac);
        parcel.writeInt(this.id);
        parcel.writeString(this.action);
    }
}
