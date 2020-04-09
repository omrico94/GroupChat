package com.example.groupchatapp;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyGroupsActivity extends AppCompatActivity {


    private RecyclerView m_GroupList;
    private DatabaseReference m_GroupsRef, m_UsersGroupsRef;
    private MyGroupsAdapter m_GroupsAdapter;
    private final ArrayList< Group> groupsToDisplay = new ArrayList<>();
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_groups);
        m_GroupList = findViewById(R.id.chats_list);
        mToolbar = findViewById(R.id.my_groups_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("My Groups");
        m_GroupsAdapter = new MyGroupsAdapter(groupsToDisplay, this);
        m_GroupList.setLayoutManager(new LinearLayoutManager(this));
        m_GroupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        m_UsersGroupsRef = FirebaseDatabase.getInstance().getReference().child("Users").child(LoginManager.getInstance().getLoggedInUser().getValue().getUid()).child("groupsId");


        m_GroupList.setAdapter(m_GroupsAdapter);


        m_UsersGroupsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshotGroupId, String s) {

                m_GroupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {

                        String groupId = dataSnapshotGroupId.getValue().toString();
                        Group group = dataSnapshot.child(groupId).getValue(Group.class);
                        groupsToDisplay.add(group);


                        m_GroupsAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

}

