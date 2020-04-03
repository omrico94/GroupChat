package com.example.groupchatapp;

//אולי בהמשך נשנה את שם המחלקה לקבוצה
public class Group {

    private String name, photoUrl, gid, description;

    private String longitude, latitude, numberOfUsers;

    public Group() {
    }

    public Group(String name,String description, String photoUrl, String groupId, String longitude, String latitude, String numberOfUsers) {
        this.name = name;
        this.photoUrl = photoUrl;
        this.gid = groupId;
        this.longitude = longitude;
        this.latitude = latitude;
        this.description =description;
        this.numberOfUsers = numberOfUsers;
    }


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

    public String getNumberOfUsers() {
        return numberOfUsers;
    }
}


