package com.example.groupchatapp.Models;

public class Message
{
    private String from, message, type, time, date, name, id, senderName;

    public Message()
    {

    }

    public Message(String id, String from, String message, String type, String time, String date, String name, String senderName) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.id =id;
        this.time = time;
        this.date = date;
        this.name = name;
        this.senderName = senderName;
    }

    public String getId() {
        return id;
    }

    public String getFrom() {
        return from;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public String getTime() {
        return time;
    }

    public String getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public String getSenderName() {
        return senderName;
    }

}
