package com.example.groupchatapp;

import android.location.Location;

import com.example.groupchatapp.Models.Group;
import com.example.groupchatapp.Models.IDisplayable;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    public static int findIndexOfGroup(ArrayList<IDisplayable> displayableArr, String displayableID)
    {
        int i;
        for (i = 0; i < displayableArr.size(); i++) {
            if (displayableArr.get(i).getId().equals(displayableID)) {
                break;
            }
        }

        if (i == displayableArr.size()) {
            i = -1;
        }

        return i;
    }

    public static boolean isGroupInMyLocation(Group group) {
        if (!LoginManager.getInstance().getLocationManager().isLocationOn()) {
            return false;
        }

        float[] result = new float[1];
        Location.distanceBetween(LoginManager.getInstance().getLocationManager().getLatitude(),
                LoginManager.getInstance().getLocationManager().getLongitude(),
                Double.valueOf(group.getLatitude()), Double.valueOf(group.getLongitude()), result);

        return result[0] <= Float.valueOf(group.getRadius());
    }
}