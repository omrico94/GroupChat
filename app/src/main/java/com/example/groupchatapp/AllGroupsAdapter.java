package com.example.groupchatapp;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import java.util.ArrayList;

public class AllGroupsAdapter extends GroupsAdapter {


    public AllGroupsAdapter(ArrayList<Group> mGroups, Context mContext) {
       super(mGroups,mContext);
    }

    @Override
    void onClickItem(View view, Group currentGroup, String groupPhoto) {
        Intent joinGroupIntent = new Intent(mContext, JoinToGroupActivity.class);
        joinGroupIntent.putExtra("group_id", currentGroup.getGid());
        joinGroupIntent.putExtra("group_name", currentGroup.getName());
        joinGroupIntent.putExtra("group_image", groupPhoto);
        joinGroupIntent.putExtra("group_password", currentGroup.getPassword());
        mContext.startActivity(joinGroupIntent);
    }
}
