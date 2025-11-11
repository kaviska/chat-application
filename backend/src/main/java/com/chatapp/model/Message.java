package com.chatapp.model;

import java.time.LocalDateTime;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class Message {
    private int id;
    private String senderEmail;
    private String senderType; // "admin" or "member"
    private String receiverEmail;
    private String receiverType; // "admin" or "member"
    private String message;
    private LocalDateTime timestamp;
    private boolean isRead;
    // Compatibility / helper fields used by older server code
    private String type;
    private String username; // alias for sender's username

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

    // Compatibility getters/setters used by older code
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return this.message;
    }

    public void setContent(String content) {
        this.message = content;
    }

    public String getSender() {
        return this.senderEmail;
    }

    public void setSender(String sender) {
        this.senderEmail = sender;
    }

    public String getReceiver() {
        return this.receiverEmail;
    }

    public void setReceiver(String receiver) {
        this.receiverEmail = receiver;
    }

    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // timestamp compatibility: accept epoch millis
    public void setTimestamp(long epochMillis) {
        try {
            this.timestamp = LocalDateTime.ofEpochSecond(epochMillis / 1000, (int)(epochMillis % 1000) * 1000000, java.time.ZoneOffset.systemDefault().getRules().getOffset(java.time.Instant.now()));
        } catch (Exception e) {
            // fallback: set to now
            this.timestamp = LocalDateTime.now();
        }
    }

    // JSON helpers
    public String toJson() {
        return new Gson().toJson(this);
    }

    public static Message fromJson(String json) throws JsonSyntaxException {
        return new Gson().fromJson(json, Message.class);
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
