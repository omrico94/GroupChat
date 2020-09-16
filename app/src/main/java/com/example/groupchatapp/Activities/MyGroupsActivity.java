package com.example.groupchatapp.Activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupchatapp.Adapters.MyGroupsAdapter;
import com.example.groupchatapp.FirebaseListenerService;
import com.example.groupchatapp.LoginManager;
import com.example.groupchatapp.Models.Group;
import com.example.groupchatapp.Models.IDisplayable;
import com.example.groupchatapp.R;
import com.example.groupchatapp.Utils;
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
    private final ArrayList<IDisplayable> groupsToDisplay = new ArrayList<>();
    private Toolbar mToolbar;
    private ChildEventListener m_MyGroupsChildEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_my_groups);
        m_GroupList = findViewById(R.id.chats_list);
        mToolbar = findViewById(R.id.my_groups_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("My Groups");
        m_GroupsAdapter = new MyGroupsAdapter(groupsToDisplay, this);
        m_GroupList.setLayoutManager(new LinearLayoutManager(this));
        String countryCode = LoginManager.getInstance().getLocationManager().getCountryCode();

        m_GroupsRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(countryCode);

        m_UsersGroupsRef = FirebaseDatabase.getInstance().getReference().child("Users").child(LoginManager.getInstance().getLoggedInUser().getValue().getId()).child("groupsId");

        m_GroupList.setAdapter(m_GroupsAdapter);

        initMyGroupsChildEventListener();
        m_UsersGroupsRef.addChildEventListener(m_MyGroupsChildEventListener);
        FirebaseListenerService.addChildEventListenerToRemoveList(m_UsersGroupsRef,m_MyGroupsChildEventListener);

    }

    private void initMyGroupsChildEventListener() {

        m_MyGroupsChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshotGroupId, String s) {

                m_GroupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {

                        String groupId = dataSnapshotGroupId.getKey();
                        Group group = dataSnapshot.child(groupId).getValue(Group.class);
                        groupsToDisplay.add(group);
                        m_GroupsAdapter.notifyItemInserted(groupsToDisplay.size()-1);
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
            public void onChildRemoved(DataSnapshot dataSnapshotGroupId) {
                String groupId = dataSnapshotGroupId.getKey();

                //Maybe we don't need this check.. only to be sure..
                int index = Utils.findIndexOfGroup(groupsToDisplay, groupId);
                if (index != -1) {
                    groupsToDisplay.remove(index);
                    m_GroupsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }
}

