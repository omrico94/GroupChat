package com.example.groupchatapp;

import com.example.groupchatapp.Models.Group;

import java.util.ArrayList;

public class Utils {

    public static int findIndexOfGroup(ArrayList<Group> groupsArr, Group group)
    {
        int i;
        for (i = 0; i < groupsArr.size(); i++) {
            if (groupsArr.get(i).getGid().equals(group.getGid())) {
                break;
            }
        }

        if (i == groupsArr.size()) {
            i = -1;
        }

        return i;
    }
}
