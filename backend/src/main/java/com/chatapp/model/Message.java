package com.chatapp.model;

import java.time.LocalDateTime;

public class Message {
    private int id;
    private String senderEmail;
    private String senderType; // "admin" or "member"
    private String receiverEmail;
    private String receiverType; // "admin" or "member"
    private String message;
    private LocalDateTime timestamp;
    private boolean isRead;

    public Message() {}

    public Message(String senderEmail, String senderType, String receiverEmail, 
                   String receiverType, String message) {
        this.senderEmail = senderEmail;
        this.senderType = senderType;
        this.receiverEmail = receiverEmail;
        this.receiverType = receiverType;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.isRead = false;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public String getSenderType() {
        return senderType;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }

    public String getReceiverType() {
        return receiverType;
    }

    public void setReceiverType(String receiverType) {
        this.receiverType = receiverType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", senderEmail='" + senderEmail + '\'' +
                ", senderType='" + senderType + '\'' +
                ", receiverEmail='" + receiverEmail + '\'' +
                ", receiverType='" + receiverType + '\'' +
                ", message='" + message + '\'' +
                ", timestamp=" + timestamp +
                ", isRead=" + isRead +
                '}';
    }
}
