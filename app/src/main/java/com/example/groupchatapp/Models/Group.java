package com.example.groupchatapp.Models;

import java.io.Serializable;
import java.util.HashMap;


//רק בשביל להעביר מידע למפות ימחק בהמשך serializable
//   אולי בהמשך נשנה את שם המחלקה לקבוצה
public class Group implements Serializable ,IDisplayable {

    private String name, photoUrl, id, description;

    private String longitude, latitude,password, radius;

    private HashMap<String,String> usersId = new HashMap<>();

    int numberOfParticipants;

    public Group() {
    }

    public Group(String name,String description, String photoUrl, String groupId, String longitude, String latitude, HashMap<String,String> usersId,String password, String radius, int numberOfParticipants) {
        this.name = name;
        this.photoUrl = photoUrl;
        this.id = groupId;
        this.longitude = longitude;
        this.latitude = latitude;
        this.description =description;
        this.usersId = usersId;
        this.password = password;
        this.radius = radius;
        this.numberOfParticipants = numberOfParticipants;
    }

    public String getRadius() { return radius; }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getDescription() {
        return description;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public HashMap<String,String> getUsersId() {
        return usersId;
    }

    public String getPassword() {
        return password;
    }

    public boolean isPrivateGroup() {
        return password!=null;
    }

    @Override
    public String getId() {return id;}

    public int getNumberOfParticipants(){ return numberOfParticipants; }

    public void setNumberOfParticipants(int numberOfParticipants) { this.numberOfParticipants = numberOfParticipants; }
}


