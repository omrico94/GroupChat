package com.example.groupchatapp;


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

import de.hdodenhof.circleimageview.CircleImageView;

//לדעתי מה שיש במחלקה כאן זה בעצם מה שאנחנו רוצים שיהיה בקבוצה
/**
 * A simple {@link Fragment} subclass.
 */
public class ContactsFragment extends MyFragment {

    private View contactsView;
    private RecyclerView myContactList;
    private DatabaseReference contactsRef,usersRef;
    private FirebaseAuth mAuth;
    private String currentUserId;

    public ContactsFragment() {
        title="Groups";
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        contactsView=inflater.inflate(R.layout.fragment_contacts,container,false);
        myContactList=contactsView.findViewById(R.id.contacts_list);
        myContactList.setLayoutManager(new LinearLayoutManager(getContext()));
        mAuth=FirebaseAuth.getInstance();
        contactsRef=FirebaseDatabase.getInstance().getReference().child("new Groups");
        usersRef=FirebaseDatabase.getInstance().getReference().child("new Groups");

        return  contactsView;

    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions options =  new FirebaseRecyclerOptions.Builder<Groups>()
                .setQuery(contactsRef, Groups.class)
                .build();

        FirebaseRecyclerAdapter<Groups,ContactsViewHolder> adapter = new FirebaseRecyclerAdapter<Groups, ContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsViewHolder contactsViewHolder, int position, @NonNull Groups contacts) {

                String usersId = getRef(position).getKey();
                usersRef.child(usersId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String userName = dataSnapshot.child("name").getValue().toString();
                        contactsViewHolder.userName.setText(userName);

                        if(dataSnapshot.hasChild("image"))
                        {
                            String profileImage = dataSnapshot.child("image").getValue().toString();
                            Picasso.get().load(profileImage).placeholder(R.drawable.profile_image).into(contactsViewHolder.groupImage);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view =  LayoutInflater.from(parent.getContext()).inflate(R.layout.groups_display_layout,parent,false);
                ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                return viewHolder;
            }
        };

        myContactList.setAdapter(adapter);
        adapter.startListening();

    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userName;
        CircleImageView groupImage;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName=itemView.findViewById(R.id.user_profile_name);
            groupImage =itemView.findViewById(R.id.users_profile_image);

        }
    }
}
