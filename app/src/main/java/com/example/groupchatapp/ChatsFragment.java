package com.example.groupchatapp;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatsFragment extends MyFragment {

private View privateChatsView;
private RecyclerView chatsList;
private DatabaseReference chatsRef;
private RecyclerViewAdapter groups;
private final ArrayList<DataSnapshot> groupsToDisplay = new ArrayList<DataSnapshot>();

    public ChatsFragment() {
        // Required empty public constructor
        title="Chat";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatsView=inflater.inflate(R.layout.fragment_chats,container,false);

        chatsList=privateChatsView.findViewById(R.id.chats_list);
        groups = new RecyclerViewAdapter(groupsToDisplay,getContext());
        chatsList.setLayoutManager(new LinearLayoutManager(getContext()));
        chatsRef= FirebaseDatabase.getInstance().getReference().child("new Groups");
        chatsList.setAdapter(groups);

        return  privateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();



            chatsRef.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {

                    Iterator iterator = dataSnapshot.getChildren().iterator();
                    while (iterator.hasNext()) {
                        if (((DataSnapshot) iterator.next()).child("code").getValue().toString().equals("11")) {
                            groupsToDisplay.add(((DataSnapshot) iterator.next()));
                            groups.notifyDataSetChanged();
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError)
                {

                }
            });

        }

    private static class RecyclerViewAdapter extends RecyclerView.Adapter<ChatsViewHolder> {
        private ArrayList<DataSnapshot> mGroups;
        private Context mContext;

        public RecyclerViewAdapter(ArrayList<DataSnapshot> mGroups, Context mContext) {
            this.mGroups = mGroups;
            this.mContext = mContext;
        }

        @NonNull
        @Override
        public ChatsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.groups_display_layout, parent, false);
            return new ChatsViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ChatsViewHolder holder, int position) {

            final String groupId = mGroups.get(position).getKey();
            final String[] retImage = {"default_image"};
            if (mGroups.get(position).hasChild("image")) {
                retImage[0] = mGroups.get(position).child("image").getValue().toString();
                Picasso.get().load(retImage[0]).placeholder(R.drawable.profile_image).into(holder.profileImage);
            }

            final String retName = mGroups.get(position).child("name").getValue().toString();
            holder.userName.setText(retName);

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
    }

    private static class ChatsViewHolder extends RecyclerView.ViewHolder
    {
        CircleImageView profileImage;
        TextView userName;

        public ChatsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.user_profile_name);
            profileImage=itemView.findViewById(R.id.users_profile_image);
        }
    }
}
