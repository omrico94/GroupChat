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

public class AllGroupsAdapter extends RecyclerView.Adapter<AllGroupsAdapter.AllGroupsViewHolder> {

    private ArrayList<Group> mGroups;
    private Context mContext;

    public AllGroupsAdapter(ArrayList<Group> mGroups, Context mContext) {
        this.mGroups = mGroups;
        this.mContext = mContext;//אם זה רק משומש ב mygroups אז אפשר בלי זה
    }

    @NonNull
    @Override
    public AllGroupsAdapter.AllGroupsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.groups_display_layout, parent, false);
        return new AllGroupsAdapter.AllGroupsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final AllGroupsAdapter.AllGroupsViewHolder holder, int position) {

        final String groupId = mGroups.get(position).getGid();
        final String[] retImage = {"default_image"};
        if (mGroups.get(position).getPhotoUrl()!=null) {
            retImage[0] = mGroups.get(position).getPhotoUrl();
            Picasso.get().load(retImage[0]).placeholder(R.drawable.profile_image).into(holder.groupPhoto);
        }

        final String retName = mGroups.get(position).getName();
        holder.groupName.setText(retName);

        final String numberOfUsers = mGroups.get(position).getNumberOfUsers();
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent joinGroupIntent = new Intent(mContext, JoinToGroupActivity.class);
                joinGroupIntent.putExtra("group_id", groupId);
                joinGroupIntent.putExtra("group_name", retName);
                joinGroupIntent.putExtra("group_image", retImage);
                joinGroupIntent.putExtra("group_number_of_users",numberOfUsers );

                mContext.startActivity(joinGroupIntent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mGroups.size();
    }

    public class AllGroupsViewHolder extends RecyclerView.ViewHolder {
        CircleImageView groupPhoto;
        TextView groupName;

        public AllGroupsViewHolder(@NonNull View itemView) {
            super(itemView);

            groupName = itemView.findViewById(R.id.user_profile_name);
            groupPhoto = itemView.findViewById(R.id.users_profile_image);
        }
    }

}
