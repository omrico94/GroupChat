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
import com.example.groupchatapp.Models.IDisplayable;
import com.example.groupchatapp.R;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.util.ArrayList;

public class MyGroupsAdapter extends IdisplayableAdapter {

    public MyGroupsAdapter(ArrayList<IDisplayable> mGroups, Context mContext) {
        super(mGroups, mContext);
    }

    @Override
    public void onBindViewHolder(@NonNull final IDisplayableViewHolder holder, int position) {

        super.onBindViewHolder(holder, position);
        final String retImage = m_Displayables.get(position).getPhotoUrl()!=null? m_Displayables.get(position).getPhotoUrl():"default_image";
        Picasso.get().load(retImage).placeholder(R.drawable.groupicon).into(holder.IDisplayablePhoto);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (LoginManager.getInstance().getLocationManager().isLocationOn()) {
                    Group group = (Group)m_Displayables.get(position);
                    Intent chatIntent = new Intent(mContext, ChatActivity.class);
                    chatIntent.putExtra("group",group);
                    mContext.startActivity(chatIntent);
                } else {
                    Toast.makeText(mContext, "Turn on location!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                IDisplayable group = m_Displayables.get(position) ;

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                LoginManager.getInstance().removeGroupIdFromCurrentUser(group.getId());
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                AlertDialog dialogAlert = new AlertDialog.Builder(mContext, R.style.MyDialogTheme)
                        .setTitle("Confirm")
                        .setMessage("Do you want to remove " + group.getName() + " from your groups?")
                        .setPositiveButton("Yes",dialogClickListener)
                        .setNegativeButton("No", dialogClickListener)
                        .create();
                dialogAlert.show();
                return false;
            }
        });
    }



}
