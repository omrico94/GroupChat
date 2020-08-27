package com.example.groupchatapp.Models;

import java.util.ArrayList;
import java.util.HashMap;

public class User implements IDisplayable {

        private String photoUrl;
        private String name;
        private String id;
        private String token;
        private String status;

        private HashMap<String, ArrayList<MyPair<String,String>>> groupsId = new HashMap<>();

        //keep empty c'tor for firebase downloading the user
        public User() {
        }

        public User(String uid, String displayName, String token, String photoUrl, String status, HashMap<String, ArrayList<MyPair<String,String>>>  groupsId) {
            this.id = id;
            this.name = displayName;
            this.token = token;
            this.photoUrl = photoUrl;
            this.groupsId = groupsId;
            this.status =status;
        }

        @Override
        public String getPhotoUrl() {
            return photoUrl;
        }

        @Override
        public String getName() {
            return name;
        }

        public String getToken() {
            return token;
        }

        public String getStatus() {
            return status;
        }

        public HashMap<String, ArrayList<MyPair<String,String>>>  getGroupsId() {
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

        @Override
        public String getId(){return id;}

    public boolean isUserInGroup(String groupId)
    {
        return groupsId.get(groupId) != null &&
               groupsId.get(groupId).get(getGroupsId().get(groupId).size() - 1).getSecond().isEmpty();
    }
}


