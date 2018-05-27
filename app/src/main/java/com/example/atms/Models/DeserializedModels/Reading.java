package com.example.atms.Models.DeserializedModels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

class Reading {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("device_key")
    @Expose
    private String deviceKey;
    @SerializedName("temp")
    @Expose
    private Double temp;
    @SerializedName("hum")
    @Expose
    private Double hum;
    @SerializedName("oxygen")
    @Expose
    private Double oxygen;
    @SerializedName("created_at")
    @Expose
    private String createdAt;
    @SerializedName("updated_at")
    @Expose
    private String updatedAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDeviceKey() {
        return deviceKey;
    }

    public void setDeviceKey(String deviceKey) {
        this.deviceKey = deviceKey;
    }

    public Double getTemp() {
        return temp;
    }

    public void setTemp(Double temp) {
        this.temp = temp;
    }

    public Double getHum() {
        return hum;
    }

    public void setHum(Double hum) {
        this.hum = hum;
    }

    public Double getOxygen() {
        return oxygen;
    }

    public void setOxygen(Double oxygen) {
        this.oxygen = oxygen;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

}
