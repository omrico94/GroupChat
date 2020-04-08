package com.example.groupchatapp;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyGroupsActivity extends AppCompatActivity {


    private RecyclerView m_GroupList;
    private DatabaseReference m_GroupsRef, m_UsersRef;
    private GroupsAdapter m_GroupsAdapter;
    private final ArrayList<Group> groupsToDisplay = new ArrayList<Group>();
    private User m_CurrentUser;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_groups);
        m_GroupList = findViewById(R.id.chats_list);
        mToolbar = findViewById(R.id.my_groups_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("My Groups");
        m_GroupsAdapter = new GroupsAdapter(groupsToDisplay, this);
        m_GroupList.setLayoutManager(new LinearLayoutManager(this));
        m_GroupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        m_UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        m_GroupList.setAdapter(m_GroupsAdapter);
    }


    @Override
    public void onStart() {
        super.onStart();

        //m_UsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
        //    @Override
        //    public void onDataChange(DataSnapshot dataSnapshot) {
        //        m_CurrentUser = dataSnapshot.child(FirebaseAuth.getInstance().getLoggedInUser().getUid()).getValue(User.class);
        //    }

        //    @Override
        //    public void onCancelled(DatabaseError databaseError) {

        //    }
        //});
        final androidx.lifecycle.Observer<User> currentUserObserver = new Observer<User>() {
            @Override
            public void onChanged(User currentUser) {

                m_GroupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        if (currentUser != null) {
                            groupsToDisplay.clear();
                            //להכניס סינון אם רוצים. להסתכל על FirebaseRecyclerOptions
                            //ברור שכרגע זה לא יעיל.. שווה לחשוב על משהו אחר
                            for (String gid : currentUser.getGroupsId()) {
                                groupsToDisplay.add(dataSnapshot.child(gid).getValue(Group.class));
                            }

                            m_GroupsAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }
        };

        LoginManager.getInstance().getLoggedInUser().observe(this, currentUserObserver);


        //     Iterator iterator = dataSnapshot.getChildren().iterator();
        //     while (iterator.hasNext()) {
        //         DataSnapshot nextSnapshot = ((DataSnapshot) iterator.next());
        //         if (nextSnapshot.child("code").getValue().toString().equals("12")) {
        //             groupsToDisplay.add(nextSnapshot);
        //             m_GroupsAdapter.notifyDataSetChanged();
        //         }
        //     }
    }


}


