package com.example.groupchatapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

 abstract class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.GroupsViewHolder> {

    protected ArrayList<Group> mGroups;
    protected Context mContext;

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

        final String retImage = mGroups.get(position).getPhotoUrl()!=null? mGroups.get(position).getPhotoUrl():"default_image";
        Picasso.get().load(retImage).placeholder(R.drawable.profile_image).into(holder.groupPhoto);
        final String retName = mGroups.get(position).getName();
        holder.groupName.setText(retName);

        holder.itemView.setOnClickListener(view ->onClickItem(view, mGroups.get(position),retImage));

    }

    @Override
    public int getItemCount() {
        return mGroups.size();
    }

    abstract void onClickItem(View view,Group currentGroup,String groupPhoto);

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

