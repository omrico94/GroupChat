package com.example.groupchatapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupsViewHolder> {

    private ArrayList<Group> mGroups;
    private Context mContext;

    public GroupsAdapter(ArrayList<Group> mGroups, Context mContext) {
        this.mGroups = mGroups;
        this.mContext = mContext;//אם זה רק משומש ב mygroups אז אפשר בלי זה
    }

    @NonNull
    @Override
    public GroupsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.groups_display_layout, parent, false);
        return new GroupsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final GroupsViewHolder holder, int position) {

        final String groupId = mGroups.get(position).getGroupId();
        final String[] retImage = {"default_image"};
        if (!mGroups.get(position).getPhotoUrl().isEmpty()) {
            retImage[0] = mGroups.get(position).getPhotoUrl();
            Picasso.get().load(retImage[0]).placeholder(R.drawable.profile_image).into(holder.groupPhoto);
        }

        final String retName = mGroups.get(position).getName();
        holder.groupName.setText(retName);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent chatIntent = new Intent(mContext, ChatActivity.class);
                chatIntent.putExtra("group_id", groupId);
                chatIntent.putExtra("group_name", retName);
                chatIntent.putExtra("group_image", retImage);
                mContext.startActivity(chatIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mGroups.size();
    }

    public class GroupsViewHolder extends RecyclerView.ViewHolder {
        CircleImageView groupPhoto;
        TextView groupName;

        public GroupsViewHolder(@NonNull View itemView) {
            super(itemView);

            groupName = itemView.findViewById(R.id.user_profile_name);
            groupPhoto = itemView.findViewById(R.id.users_profile_image);
        }
    }
}

