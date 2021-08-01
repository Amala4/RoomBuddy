package com.personalproject.roombuddy.models;

public class Messages {

    String message, userID, messageOwnerID, messageTime;

    public Messages(String messageOwnerID, String userID, String message, String messageTime) {

        this.message = message;
        this.userID = userID;
        this.messageOwnerID = messageOwnerID;
        this.messageTime = messageTime;
    }


    public String getMessage() {
        return message;
    }


    public String getUserID() {
        return userID;
    }


    public String getMessageOwnerID() {
        return messageOwnerID;
    }

    public String getMessageTime() {
        return messageTime;
    }



}
