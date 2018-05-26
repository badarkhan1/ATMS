package com.example.atms.Models.EventBusModels;

public class LoginSuccess {

    private int loginResponse;
    private String user;

    public int getLoginResponse() {
        return loginResponse;
    }

    public void setLoginResponse(int loginResponse) {
        this.loginResponse = loginResponse;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public LoginSuccess(int loginResponse, String user) {
        this.loginResponse = loginResponse;
        this.user = user;
    }
}
