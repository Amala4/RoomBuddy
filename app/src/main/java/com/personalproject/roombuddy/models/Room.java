package com.personalproject.roombuddy.models;

public class Room {

    private String imageUrl;
    private String roomCampus;
    private String roommateGender;
    private String roomState;
    private String currentPage;
    private String postNumber;
    private String userID;
    private boolean isLoading = false;

    public Room(String imageUrl, String roomState,
                String roomCampus, String roommateGender,
                String postNumber, String userID, String currentPage) {
        this.imageUrl = imageUrl;
        this.roomState = roomState;
        this.roomCampus = roomCampus;
        this.roommateGender = roommateGender;
        this.postNumber = postNumber;
        this.userID = userID;
        this.currentPage = currentPage;
    }

    public Room() {
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getPostNumber() {
        return postNumber;
    }

    public String getRoomCampus() {
        return roomCampus;
    }

    public String getCurrentPage() {
        return currentPage;
    }

    public String getRoomState() { return roomState; }

    public String getRoommateGender() {
        return roommateGender;
    }

    public String getUserID() {
        return userID;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public void setLoading(boolean loading) {
        isLoading = loading;
    }
}
