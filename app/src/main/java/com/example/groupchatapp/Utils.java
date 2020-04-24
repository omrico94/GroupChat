package com.example.groupchatapp;

import android.location.Location;

import com.example.groupchatapp.Models.Group;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Utils {

    public static int findIndexOfGroup(ArrayList<Group> groupsArr, String groupID)
    {
        int i;
        for (i = 0; i < groupsArr.size(); i++) {
            if (groupsArr.get(i).getGid().equals(groupID)) {
                break;
            }
        }

        if (i == groupsArr.size()) {
            i = -1;
        }

        return i;
    }

    public static boolean isGroupInMyLocation(Group group) {
        float[] result = new float[1];
        Location.distanceBetween(LoginManager.getInstance().getLocationManager().getLatitude(),
                LoginManager.getInstance().getLocationManager().getLongitude(),
                Double.valueOf(group.getLatitude()), Double.valueOf(group.getLongitude()), result);

        return result[0] < Float.valueOf(group.getRadius());
    }
}