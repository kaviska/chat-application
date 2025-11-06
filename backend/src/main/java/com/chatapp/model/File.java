package com.chatapp.model;

import java.sql.Timestamp;

public class File {
    private int id;
    private String filename;
    private byte[] fileData;
    private String fileType;
    private String sender;
    private String receiver;
    private Timestamp timestamp;

    public File() {
    }

    public File(String filename, byte[] fileData, String fileType, String sender, String receiver) {
        this.filename = filename;
        this.fileData = fileData;
        this.fileType = fileType;
        this.sender = sender;
        this.receiver = receiver;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public byte[] getFileData() {
        return fileData;
    }

    public void setFileData(byte[] fileData) {
        this.fileData = fileData;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
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

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}