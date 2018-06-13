package com.example.atms.Models.EventBusModels;


import java.util.Date;
import java.util.List;

public class YearlyResponse {

    private List<Date> dates;
    private List<Double> data;
    private String selected;

    public YearlyResponse(List<Date> dates, List<Double> data, String selected) {
        this.dates = dates;
        this.data = data;
        this.selected = selected;
    }

    public List<Date> getDates() {
        return dates;
    }

    public void setDates(List<Date> dates) {
        this.dates = dates;
    }

    public List<Double> getData() {
        return data;
    }

    public void setData(List<Double> data) {
        this.data = data;
    }

    public String getSelected() {
        return selected;
    }

    public void setSelected(String selected) {
        this.selected = selected;
    }
}
