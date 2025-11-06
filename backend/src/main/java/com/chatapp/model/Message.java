package com.chatapp.model;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Message {

    private String type; // message, private_message, login, register, file, typing, user_list,
                         // user_joined...
    private String sender;
    private String receiver;
    private Object content; // Can be String or JsonObject
    private long timestamp;
    private String username;

    public Message() {
        this.timestamp = System.currentTimeMillis();
    }

    public Message(String type, String sender, String content) {
        this.type = type;
        this.sender = sender;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    public Message(String type, String sender, String receiver, String content) {
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    // ======== Getters and Setters =========

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public Object getContent() {
        return content;
    }

    public String getContentAsString() {
        if (content instanceof String) {
            return (String) content;
        }
        return content.toString();
    }

    public JsonObject getContentAsJson() {
        if (content instanceof JsonObject) {
            return (JsonObject) content;
        }
        return null;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // No longer need file-specific getters and setters as they're part of content

    // ✅ JSON Convert
    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static Message fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Message.class);
    }

    @Override
    public String toString() {
        return "Message{" +
                "type='" + type + '\'' +
                ", sender='" + sender + '\'' +
                ", receiver='" + receiver + '\'' +
                ", content=" + content +
                ", timestamp=" + timestamp +
                ", username='" + username + '\'' +
                '}';
    }
}
