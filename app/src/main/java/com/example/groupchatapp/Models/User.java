package com.example.groupchatapp.Models;
import java.util.ArrayList;

    public class User {

        private String photoUrl;
        private String name;
        private String uid;
        private String token;
        private String status;

        private ArrayList<String> groupsId = new ArrayList<>();

        //keep empty c'tor for firebase downloading the user
        public User() {
        }

        //new user c'tor
        public User(String uid, String displayName, String token, String photoUrl, String status ,ArrayList<String> groupsId) {
            this.uid = uid;
            this.name = displayName;
            this.token = token;
            this.photoUrl = photoUrl;
            this.groupsId = groupsId;
            this.status =status;
        }

        public String getPhotoUrl() {
            return photoUrl;
        }

        public String getName() {
            return name;
        }

        public String getUid() {
            return uid;
        }

        public String getToken() {
            return token;
        }

        public String getStatus() {
            return status;
        }

        public ArrayList<String> getGroupsId() {
            return groupsId;
        }


        public void setName(String name) {
            this.name = name;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public void setPhotoUrl(String photoUrl) {
            this.photoUrl = photoUrl;
        }
    }


