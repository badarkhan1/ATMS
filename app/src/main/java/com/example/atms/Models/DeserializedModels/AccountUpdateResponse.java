package com.example.atms.Models.DeserializedModels;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class AccountUpdateResponse {

    @SerializedName("image")
    @Expose
    private String image;

    public AccountUpdateResponse(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
