package com.example.atms.Models.EventBusModels;


public class ConstraintResponse {

    private String status;
    private String response;
    private Constraint constraint;

    public Constraint getConstraint() {
        return constraint;
    }

    public void setConstraint(Constraint constraint) {
        this.constraint = constraint;
    }

    public ConstraintResponse(String status, String response, Constraint constraint) {
        this.status = status;
        this.response = response;
        this.constraint = constraint;
    }

    public ConstraintResponse(String status, String response) {
        this.status = status;
        this.response = response;
    }



    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
