package com.example.groupchatapp;

//אולי בהמשך נשנה את שם המחלקה לקבוצה
public class Contacts {

    public String name,status,image;

    public Contacts() {}
    public Contacts(String name, String status, String image) {
        this.name = name;
        this.status = status;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getImage() {
        return image;
    }
}
