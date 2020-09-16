package com.example.groupchatapp;

import android.location.Location;

import com.example.groupchatapp.Models.Group;
import com.example.groupchatapp.Models.IDisplayable;

import java.util.ArrayList;

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