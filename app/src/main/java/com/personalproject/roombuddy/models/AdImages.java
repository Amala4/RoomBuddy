package com.personalproject.roombuddy.models;

public class AdImages {

    private String adimageUrl;
    private String adwebUrl;

    public AdImages(String adimageUrl, String adwebUrl) {
        this.adimageUrl = adimageUrl;
        this.adwebUrl = adwebUrl;

    }

    public AdImages() {
    }

    public String getAdimageUrl() {
        return adimageUrl;
    }

    public String getAdwebUrl() {
        return adwebUrl;
    }

}


