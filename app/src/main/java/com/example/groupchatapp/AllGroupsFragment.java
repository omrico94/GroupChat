package com.example.groupchatapp;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class AllGroupsFragment extends MyFragment {
    private View privateChatsView;
    private RecyclerView m_GroupList;
    private DatabaseReference m_GroupsRef, m_UsersRef;
    private AllGroupsAdapter m_GroupsAdapter;
    private final ArrayList<Group> groupsToDisplay = new ArrayList<>();



    public AllGroupsFragment() {
        // Required empty public constructor
        title = "All Group";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        privateChatsView = inflater.inflate(R.layout.fragment_chats, container, false);

        m_GroupList = privateChatsView.findViewById(R.id.chats_list);
        m_GroupsAdapter = new AllGroupsAdapter(groupsToDisplay, getContext());
        m_GroupList.setLayoutManager(new LinearLayoutManager(getContext()));
        m_GroupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        m_UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        m_GroupList.setAdapter(m_GroupsAdapter);


        return privateChatsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        final androidx.lifecycle.Observer<User> currentUserObserver =
                currentUser -> m_GroupsRef.addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        if (currentUser != null) {
                            groupsToDisplay.clear();

                            for (DataSnapshot ds : dataSnapshot.getChildren()) {

                                if (!currentUser.getGroupsId().contains(ds.child("gid").getValue()))
                                    groupsToDisplay.add(ds.getValue(Group.class));
                            }
                            m_GroupsAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        LoginManager.getInstance().getLoggedInUser().observe(this, currentUserObserver);
        //אם משנים דאטה בייס בקבוצות צריך להוסיף עוד קינון
    }
}




