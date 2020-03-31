package com.example.groupchatapp;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class MyGroupsFragment extends MyFragment {

    private View privateChatsView;
    private RecyclerView m_GroupList;
    private DatabaseReference m_GroupsRef,m_UsersRef;
    private GroupsAdapter m_GroupsAdapter;
    private final ArrayList<Group> groupsToDisplay = new ArrayList<Group>();

    private User m_CurrentUser;


    public MyGroupsFragment() {
        // Required empty public constructor
        title = "My Groups";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);

        m_GroupList = privateChatsView.findViewById(R.id.chats_list);
        m_GroupsAdapter = new GroupsAdapter(groupsToDisplay, getContext());
        m_GroupList.setLayoutManager(new LinearLayoutManager(getContext()));
        m_GroupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        m_UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        m_GroupList.setAdapter(m_GroupsAdapter);


        return privateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        m_UsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                m_CurrentUser=dataSnapshot.child(FirebaseAuth.getInstance().getCurrentUser().getUid()).getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        m_GroupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                groupsToDisplay.clear();
                //להכניס סינון אם רוצים. להסתכל על FirebaseRecyclerOptions
                //ברור שכרגע זה לא יעיל.. שווה לחשוב על משהו אחר
                for (String gid : m_CurrentUser.getGroupsId()) {
                    groupsToDisplay.add(dataSnapshot.child(gid).getValue(Group.class));
                }
                m_GroupsAdapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
