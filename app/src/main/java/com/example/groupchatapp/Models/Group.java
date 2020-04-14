package com.example.groupchatapp.Models;

import java.util.HashMap;

public class Group {

    private String name, photoUrl, gid, description;

    private String longitude, latitude,password, radius;

    private HashMap<String,String> usersId = new HashMap<>();

    public Group() {
    }

    public Group(String name,String description, String photoUrl, String groupId, String longitude, String latitude, HashMap<String,String> usersId,String password, String radius) {
        this.name = name;
        this.photoUrl = photoUrl;
        this.gid = groupId;
        this.longitude = longitude;
        this.latitude = latitude;
        this.description =description;
        this.usersId = usersId;
        this.password = password;
        this.radius = radius;
    }

    public String getRadius() { return radius; }

    public String getName() {
        return name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getGid() {
        return gid;
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

    public boolean isWithPassword() {
        return password!=null;
    }
}


