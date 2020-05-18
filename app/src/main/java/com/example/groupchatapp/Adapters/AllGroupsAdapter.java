package com.example.groupchatapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.example.groupchatapp.Activities.JoinToGroupActivity;
import com.example.groupchatapp.Models.Group;

import java.util.ArrayList;

//public class AllGroupsAdapter extends IdisplayableAdapter {
//
//
//    public AllGroupsAdapter(ArrayList<Group> mGroups, Context mContext) {
//       super(mGroups,mContext);
//    }
//
//
//    void onClickItem(View view, Group currentGroup, String IDisplayablePhoto) {
//        Intent joinGroupIntent = new Intent(mContext, JoinToGroupActivity.class);
//        joinGroupIntent.putExtra("group_id", currentGroup.getGid());
//        joinGroupIntent.putExtra("group_name", currentGroup.getName());
//        joinGroupIntent.putExtra("group_image", IDisplayablePhoto);
//        joinGroupIntent.putExtra("group_password", currentGroup.getPassword());
//        mContext.startActivity(joinGroupIntent);
//    }
//}
