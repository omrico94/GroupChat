package com.example.groupchatapp;

//אולי בהמשך נשנה את שם המחלקה לקבוצה
public class Group {

    public String m_Name, m_PhotoUrl, m_GroupId,m_Description;
    public int m_Longitude, m_Latitude, m_NumberOfUsers;

    public Group() {
    }

    public Group(String name,String description, String photoUrl, String grpoupId, int longitude, int latitude, int numberOfUsers) {
        this.m_Name = name;
        this.m_PhotoUrl = photoUrl;
        this.m_GroupId = grpoupId;
        this.m_Longitude = longitude;
        this.m_Latitude = latitude;
        this.m_Description=description;
        this.m_NumberOfUsers = numberOfUsers;
    }

    public String getDescription() {
        return m_Description;
    }

    public String getName() {
        return m_Name;
    }

    public String getPhotoUrl() {
        return m_PhotoUrl;
    }

    public String getGroupId() {
        return m_GroupId;
    }

    public int getLongitude() {
        return m_Longitude;
    }

    public int getLatitude() {
        return m_Latitude;
    }

    public int getNumberOfUsers() {
        return m_NumberOfUsers;
    }
}


