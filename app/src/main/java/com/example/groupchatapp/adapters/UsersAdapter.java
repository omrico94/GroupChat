package com.example.groupchatapp.adapters;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.groupchatapp.models.IDisplayable;
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
        final String retImage = m_Displayables.get(position).getPhotoUrl();
        if ( retImage == null) {
            holder.IDisplayablePhoto.setImageResource(R.drawable.profile_image);
        } else {
            Picasso.get().load(retImage).into(holder.IDisplayablePhoto);
        }
    }
}
