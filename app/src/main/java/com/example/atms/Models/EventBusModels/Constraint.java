package com.example.atms.Models.EventBusModels;


public class Constraint {
    private int minTemp , maxTemp , minHum , maxHum , minOxy , maxOxy;
    private String device_key;

    public int getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(int minTemp) {
        this.minTemp = minTemp;
    }

    public int getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(int maxTemp) {
        this.maxTemp = maxTemp;
    }

    public int getMinHum() {
        return minHum;
    }

    public void setMinHum(int minHum) {
        this.minHum = minHum;
    }

    public int getMaxHum() {
        return maxHum;
    }

    public void setMaxHum(int maxHum) {
        this.maxHum = maxHum;
    }

    public int getMinOxy() {
        return minOxy;
    }

    public void setMinOxy(int minOxy) {
        this.minOxy = minOxy;
    }

    public int getMaxOxy() {
        return maxOxy;
    }

    public void setMaxOxy(int maxOxy) {
        this.maxOxy = maxOxy;
    }

    public String getDevice_key() {
        return device_key;
    }

    public void setDevice_key(String device_key) {
        this.device_key = device_key;
    }

    public Constraint(int minTemp, int maxTemp, int minHum, int maxHum, int minOxy, int maxOxy, String device_key) {
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.minHum = minHum;
        this.maxHum = maxHum;
        this.minOxy = minOxy;
        this.maxOxy = maxOxy;
        this.device_key = device_key;
    }
}
