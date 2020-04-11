package com.example.groupchatapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import com.example.groupchatapp.Activities.ChatActivity;
import com.example.groupchatapp.Activities.MainActivity;
import com.example.groupchatapp.Activities.MyGroupsActivity;
import com.example.groupchatapp.LoginManager;
import com.example.groupchatapp.Models.Group;

import java.util.ArrayList;

public class MyGroupsAdapter extends GroupsAdapter {


 public MyGroupsAdapter(ArrayList<Group> mGroups, Context mContext) {
        super(mGroups,mContext);
    }

    @Override
    void onClickItem(View view, Group currentGroup, String groupPhoto) {
        if (LoginManager.getInstance().getLoggedInUser().getValue().getCountryCode() != null) {
            Intent chatIntent = new Intent(mContext, ChatActivity.class);
            chatIntent.putExtra("group_id", currentGroup.getGid());
            chatIntent.putExtra("group_name", currentGroup.getName());
            chatIntent.putExtra("group_image", groupPhoto);
            mContext.startActivity(chatIntent);
        } else {
            Toast.makeText(this.mContext, "Turn on location!", Toast.LENGTH_SHORT).show();
        }
    }
}
