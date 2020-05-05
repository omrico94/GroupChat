package com.example.groupchatapp.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.groupchatapp.Adapters.UsersAdapter;
import com.example.groupchatapp.FirebaseListenerService;
import com.example.groupchatapp.LoginManager;
import com.example.groupchatapp.Models.Group;
import com.example.groupchatapp.Models.IDisplayable;
import com.example.groupchatapp.Models.User;
import com.example.groupchatapp.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupInfoActivity extends AppCompatActivity {

    private RecyclerView m_UsersList;
    private DatabaseReference m_CurrentGroupUsersIdRef, m_UsersRef;
    private UsersAdapter m_UsersAdapter;
    private final ArrayList<IDisplayable> m_UsersToDisplay = new ArrayList<>();
    private Toolbar mToolbar;
    private String m_GroupId, m_GroupName, m_GroupPhotoUrl, m_GroupDescription;
    private ChildEventListener m_CurrentGroupUsersIdChildEventListener;
    private TextView m_UsersCounterTextView, m_GroupDescriptionTextView;
    private CircleImageView m_GroupCircleImageView;
    private Button m_ExitFromGroupButton;
    private int m_UsersCounter = 0;
    private Group m_CurrentGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_info);
        m_CurrentGroup = (Group) getIntent().getExtras().get("group");
        m_GroupId = m_CurrentGroup.getId();
        m_GroupName = m_CurrentGroup.getName();
        m_GroupDescription = m_CurrentGroup.getDescription();


        String countryCode = LoginManager.getInstance().getLocationManager().getCountryCode();
        initUI();
        m_CurrentGroupUsersIdRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(countryCode).child(m_GroupId).child("usersId");

        m_UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        m_UsersList.setAdapter(m_UsersAdapter);

        initCurrentGroupUsersIdChildEventListener();
        m_CurrentGroupUsersIdRef.addChildEventListener(m_CurrentGroupUsersIdChildEventListener);
        FirebaseListenerService.addChildEventListenerToRemoveList(m_CurrentGroupUsersIdRef, m_CurrentGroupUsersIdChildEventListener);

    }

    private void initUI() {
        m_UsersList = findViewById(R.id.group_friends);
        mToolbar = findViewById(R.id.group_friends_page_toolbar);
        m_ExitFromGroupButton = findViewById(R.id.group_info_exit_group_button);
        m_GroupDescriptionTextView = findViewById(R.id.group_desc);
        m_UsersCounterTextView = findViewById(R.id.friends_counter_textView);
        m_GroupCircleImageView = findViewById(R.id.group_image_imageView);
        m_GroupPhotoUrl = m_CurrentGroup.getPhotoUrl() != null ? m_CurrentGroup.getPhotoUrl() : "default_image";

        m_GroupDescriptionTextView.setText(m_GroupDescription);
        Picasso.get().load(m_GroupPhotoUrl).placeholder(R.drawable.groupicon).into(m_GroupCircleImageView);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle(m_GroupName);

        m_UsersAdapter = new UsersAdapter(m_UsersToDisplay, this);
        m_UsersList.setLayoutManager(new LinearLayoutManager(this));

        m_ExitFromGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                LoginManager.getInstance().exitFromGroup(m_CurrentGroup.getId());
                                SendUserToMyGroupsActivity();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                android.app.AlertDialog dialogAlert = new AlertDialog.Builder(GroupInfoActivity.this, R.style.MyDialogTheme)
                        .setTitle("Confirm")
                        .setMessage("Remove " + m_CurrentGroup.getName() + " from MyGroups?")
                        .setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener)
                        .create();
                dialogAlert.show();

            }
        });
    }

    private void initCurrentGroupUsersIdChildEventListener() {

        m_CurrentGroupUsersIdChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshotUserId, String s) {

                m_UsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {

                        String userId = dataSnapshotUserId.getKey();
                        User user = dataSnapshot.child(userId).getValue(User.class);
                        if(user.isUserInGroup(m_GroupId)) {
                            m_UsersToDisplay.add(user);
                            m_UsersAdapter.notifyItemInserted(m_UsersToDisplay.size() - 1);
                            m_UsersCounter++;
                            m_UsersCounterTextView.setText(String.valueOf(m_UsersCounter));
                        }
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
            public void onChildRemoved(DataSnapshot dataSnapshotUserId) {

                //לשאול את החברה, אבל לדעתי עדיף שניצור כל פעם מחדש את כל הרשימה כדי שנקבל תמונות עדכניות של משתמשים
                //  m_UsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                //      @Override
                //      public void onDataChange(final DataSnapshot dataSnapshot) {

                //          String userId = dataSnapshotUserId.getKey();
                //          User user = dataSnapshot.child(userId).getValue(User.class);
                //          m_UsersToDisplay.add(user);
                //          m_UsersAdapter.notifyItemRemoved(m_UsersToDisplay.size()-1);
                //          m_UsersCounter++;
                //          m_UsersCounterTextView.setText(m_UsersCounter);
                //      }
                //      @Override
                //      public void onCancelled(DatabaseError databaseError) {

                //      }
                //  });
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    @Override
    public void onBackPressed() {
        SendUserToChatActivity();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
           SendUserToChatActivity();
        }
        return true;
    }

    private void SendUserToChatActivity() {
        Intent chatIntent = new Intent(this, ChatActivity.class);
        chatIntent.putExtra("group", m_CurrentGroup);
        this.startActivity(chatIntent);
        finish();
    }

    private void SendUserToMyGroupsActivity() {
        Intent myGroupsIntent = new Intent(this, MyGroupsActivity.class);
        this.startActivity(myGroupsIntent);
        finish();
    }



}
