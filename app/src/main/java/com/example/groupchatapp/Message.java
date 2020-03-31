package com.example.groupchatapp;

public class Message
{
    private String m_From, m_Message, m_Type, m_Time, m_Date, m_Name,m_Id;

    public Message()
    {

    }

    public Message(String id, String from, String message, String type, String time, String date, String name) {
        this.m_From = from;
        this.m_Message = message;
        this.m_Type = type;
        this.m_Id=id;
        this.m_Time = time;
        this.m_Date = date;
        this.m_Name = name;
    }

    public String getId() {
        return m_Id;
    }

    public String getFrom() {
        return m_From;
    }

    public String getMessage() {
        return m_Message;
    }

    public void setMessage(String message) {
        this.m_Message = message;
    }

    public String getType() {
        return m_Type;
    }

    public String getTime() {
        return m_Time;
    }

    public String getDate() {
        return m_Date;
    }

    public String getName() {
        return m_Name;
    }
}
