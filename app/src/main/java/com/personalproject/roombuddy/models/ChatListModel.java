package com.personalproject.roombuddy.models;

public class ChatListModel {


    String name, message, imageUrl, participantID;

    public ChatListModel(String name, String message, String imageUrl, String participantID) {

        this.name = name;
        this.message = message;
        this.imageUrl = imageUrl;
        this.participantID = participantID;
    }

    public String getName() {
        return name;
    }


    public String getMessage() {
        return message;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public String getParticipantID() {
        return participantID;
    }
}
