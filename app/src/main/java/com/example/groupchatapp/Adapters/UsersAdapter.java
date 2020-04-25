package com.example.groupchatapp.Adapters;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.groupchatapp.Models.IDisplayable;
import com.example.groupchatapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UsersAdapter extends IdisplayableAdapter {

    public UsersAdapter(ArrayList<IDisplayable> i_Users, Context mContext) {
        super(i_Users, mContext);
    }

    @Override
    public void onBindViewHolder(@NonNull final IDisplayableViewHolder holder, int position) {

        super.onBindViewHolder(holder, position);
        final String retImage = m_Displayables.get(position).getPhotoUrl() != null ? m_Displayables.get(position).getPhotoUrl() : "default_image";
        Picasso.get().load(retImage).placeholder(R.drawable.profile_image).into(holder.IDisplayablePhoto);
    }
}
