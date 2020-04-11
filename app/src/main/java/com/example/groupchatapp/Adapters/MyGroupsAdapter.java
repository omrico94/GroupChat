package com.example.groupchatapp.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.groupchatapp.Activities.ChatActivity;
import com.example.groupchatapp.LoginManager;
import com.example.groupchatapp.Models.Group;
import com.example.groupchatapp.R;

import java.util.ArrayList;

public class MyGroupsAdapter extends GroupsAdapter {


    public MyGroupsAdapter(ArrayList<Group> mGroups, Context mContext) {
        super(mGroups, mContext);
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

    @Override
    public void onBindViewHolder(@NonNull final GroupsViewHolder holder, int position) {

        super.onBindViewHolder(holder, position);
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Group group = mGroups.get(position);

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //  צריך לבדוק למה חייב את השורה הבאה כדי שהקבוצה תרד מהמסך בצורה מיידית
                                mGroups.remove(position);
                                LoginManager.getInstance().removeGroupIdFromCurrentUser(group.getGid());

                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog dialogAlert = new AlertDialog.Builder(mContext, R.style.MyDialogTheme)
                        .setTitle("Confirm")
                        .setMessage("remove " + group.getName() + " from MyGroups?")
                        .setPositiveButton("Yes",dialogClickListener)
                        .setNegativeButton("No", dialogClickListener)
                        .create();
                dialogAlert.show();
                return false;
            }
        });
    }



}
