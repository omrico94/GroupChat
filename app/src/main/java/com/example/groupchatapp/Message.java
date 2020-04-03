package com.example.groupchatapp;

public class Message
{
    private String from, message, type, time, date, name, mid;

    public Message()
    {

    }

    public Message(String id, String from, String message, String type, String time, String date, String name) {
        this.from = from;
        this.message = message;
        this.type = type;
        this.mid =id;
        this.time = time;
        this.date = date;
        this.name = name;
    }

    public String getId() {
        return mid;
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
}
