package com.example.atms.Authorization;


public class Token {

    private static String token;

    static {
        token = null;
    }

    public static void setToken(String api_token){
        token = api_token;
    }

    public static String getToken(){
        return token;
    }
}
