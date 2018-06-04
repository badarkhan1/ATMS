package com.example.atms.Models.EventBusModels;


import java.util.Date;
import java.util.List;

public class AnalyticsResponse {

    public List<Date> getDates() {
        return dates;
    }

    public void setDates(List<Date> dates) {
        this.dates = dates;
    }

    public List<Double> getTemps() {
        return temps;
    }

    public void setTemps(List<Double> temps) {
        this.temps = temps;
    }

    public List<Double> getHums() {
        return hums;
    }

    public void setHums(List<Double> hums) {
        this.hums = hums;
    }

    public AnalyticsResponse(List<Date> dates, List<Double> temps, List<Double> hums) {

        this.dates = dates;
        this.temps = temps;
        this.hums = hums;
    }

    List<Date> dates;
    List<Double> temps;
    List<Double> hums;

}
