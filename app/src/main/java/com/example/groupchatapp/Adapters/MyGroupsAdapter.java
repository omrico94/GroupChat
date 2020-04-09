package com.example.groupchatapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.example.groupchatapp.Activities.ChatActivity;
import com.example.groupchatapp.Models.Group;

import java.util.ArrayList;

public class MyGroupsAdapter extends GroupsAdapter {


 public MyGroupsAdapter(ArrayList<Group> mGroups, Context mContext) {
        super(mGroups,mContext);
    }

    @Override
    void onClickItem(View view, Group currentGroup, String groupPhoto) {
        Intent chatIntent = new Intent(mContext, ChatActivity.class);
        chatIntent.putExtra("group_id", currentGroup.getGid());
        chatIntent.putExtra("group_name", currentGroup.getName());
        chatIntent.putExtra("group_image", groupPhoto);
        mContext.startActivity(chatIntent);
    }
}
