package com.example.groupchatapp.Models;
import java.util.HashMap;

public class User {

        private String photoUrl;
        private String name;
        private String uid;
        private String token;
        private String status;

        private HashMap<String,String> groupsId = new HashMap<>();

        private String m_CountryCode;
        private double m_Latitude;
        private double m_Longitude;

        //keep empty c'tor for firebase downloading the user
        public User() {
        }

        //new user c'tor
        public User(String uid, String displayName, String token, String photoUrl, String status ,HashMap<String,String> groupsId) {
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

        public HashMap<String,String> getGroupsId() {
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

        public void setCountryCode(String CountryCode) { this.m_CountryCode = CountryCode; }

        public String getCountryCode() { return m_CountryCode; }

        public double getLatitude() { return m_Latitude; }
        public double getLongitude() { return m_Longitude; }

        public void setLatitude(double latitude) { this.m_Latitude = latitude; }
        public void setLongitude(double longitude) { this.m_Longitude = longitude; }

}


