package com.example.groupchatapp;

import android.location.Location;

//אולי בהמשך נשנה את שם המחלקה לקבוצה
public class Groups {

    public String name,image;
    public int code;

    public Groups() {}
    public Groups(String name, int code, String image) {
        this.name = name;
        this.image = image;
        this.code=code;
    }

    public String getName() {
        return name;
    }


    public String getImage() {
        return image;
    }

    public int getCode() {
        return code;
    }
}
