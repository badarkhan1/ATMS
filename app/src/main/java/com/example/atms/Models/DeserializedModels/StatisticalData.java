package com.example.atms.Models.DeserializedModels;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class StatisticalData {

    @SerializedName("devices_owned")
    @Expose
    private Integer devicesOwned;
    @SerializedName("total_readings")
    @Expose
    private Integer totalReadings;
    @SerializedName("total_readings_today")
    @Expose
    private Integer totalReadingsToday;
    @SerializedName("max_temp_today")
    @Expose
    private Integer maxTempToday;
    @SerializedName("total_sensor_devices")
    @Expose
    private Integer totalSensorDevices;
    @SerializedName("total_active_devices")
    @Expose
    private Integer totalActiveDevices;
    @SerializedName("max_hum_today")
    @Expose
    private Integer maxHumToday;
    @SerializedName("total_actuator_devices")
    @Expose
    private Integer totalActuatorDevices;

    public Integer getDevicesOwned() {
        return devicesOwned;
    }

    public void setDevicesOwned(Integer devicesOwned) {
        this.devicesOwned = devicesOwned;
    }

    public Integer getTotalReadings() {
        return totalReadings;
    }

    public void setTotalReadings(Integer totalReadings) {
        this.totalReadings = totalReadings;
    }

    public Integer getTotalReadingsToday() {
        return totalReadingsToday;
    }

    public void setTotalReadingsToday(Integer totalReadingsToday) {
        this.totalReadingsToday = totalReadingsToday;
    }

    public Integer getMaxTempToday() {
        return maxTempToday;
    }

    public void setMaxTempToday(Integer maxTempToday) {
        this.maxTempToday = maxTempToday;
    }

    public Integer getTotalSensorDevices() {
        return totalSensorDevices;
    }

    public void setTotalSensorDevices(Integer totalSensorDevices) {
        this.totalSensorDevices = totalSensorDevices;
    }

    public Integer getTotalActiveDevices() {
        return totalActiveDevices;
    }

    public void setTotalActiveDevices(Integer totalActiveDevices) {
        this.totalActiveDevices = totalActiveDevices;
    }

    public Integer getMaxHumToday() {
        return maxHumToday;
    }

    public void setMaxHumToday(Integer maxHumToday) {
        this.maxHumToday = maxHumToday;
    }

    public Integer getTotalActuatorDevices() {
        return totalActuatorDevices;
    }

    public void setTotalActuatorDevices(Integer totalActuatorDevices) {
        this.totalActuatorDevices = totalActuatorDevices;
    }

}