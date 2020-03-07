package com.example.groupchatapp;


import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends MyFragment
{
    private View groupFragmentView;
    private ListView list_view;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> list_of_groups = new ArrayList<>();
    private DatabaseReference GroupRef;



    public GroupsFragment()
    {
        // Required empty public constructor
        title="Groups";
    }
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        groupFragmentView = inflater.inflate(R.layout.fragment_groups, container, false);

        GroupRef = FirebaseDatabase.getInstance().getReference().child("new Groups");

        InitializeFields();

        RetrieveAndDisplayGroups();


        list_view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id)
            {
                String currentGroupName = adapterView.getItemAtPosition(position).toString();
                Intent groupChatIntent = new Intent(getContext(),GroupChatActivity.class);
                groupChatIntent.putExtra("groupName",currentGroupName);
                startActivity(groupChatIntent);
            }
        });

        return groupFragmentView;
    }


    private void InitializeFields()
    {
        list_view =  groupFragmentView.findViewById(R.id.list_view);
        arrayAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,list_of_groups);
        list_view.setAdapter(arrayAdapter);
    }

    private void RetrieveAndDisplayGroups()
    {
        GroupRef.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                Set<String> set = new HashSet<>();
                Iterator iterator = dataSnapshot.getChildren().iterator();

                while(iterator.hasNext())
                {
                    if(((DataSnapshot)iterator.next()).child("code").getValue().toString().equals("11"))
                    {
                        set.add(((DataSnapshot)iterator.next()).child("name").getValue().toString());
                    }

                }

                list_of_groups.clear();
                list_of_groups.addAll(set);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }


}
