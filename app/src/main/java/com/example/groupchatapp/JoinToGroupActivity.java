package com.example.groupchatapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class JoinToGroupActivity extends AppCompatActivity {




    private Button joinGroup;
    private TextView groupNameEditText;
    private CircleImageView groupProfileImage;
    private String groupId,groupName,groupImageStr;
    private int numberOfUsersInGroup;
    private DatabaseReference RootRef;
    private Toolbar groupsToolBar;

    private LoginManager m_LoginManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_to_group);

        m_LoginManager = LoginManager.getInstance();
        RootRef= FirebaseDatabase.getInstance().getReference();
        groupId =getIntent().getExtras().get("group_id").toString();
        groupName =getIntent().getExtras().get("group_name").toString();
        groupImageStr =getIntent().getExtras().get("group_image").toString();
        numberOfUsersInGroup =Integer.parseInt(getIntent().getExtras().get("group_number_of_users").toString());

        initializeFields();

        joinGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                joinGroup();
            }
        });

        RetrieveUserInfo();

    }
    private void joinGroup() {

        // כשנרצה להוסיף ססמא לקובצה - נוסיף כאן איזה if
        m_LoginManager.addNewGroupIdToCurrentUser(groupId);
        //אם משנים את הדאטה בייס צריך להוסיף כאן עוד קינון של ילד
        RootRef.child("Groups").child(groupId).child("numberOfUsers").setValue(String.valueOf(numberOfUsersInGroup+1));
        SendUserToMainActivity();

    }

  private void RetrieveUserInfo() {
      groupNameEditText.setText(groupName);

      if (groupImageStr != null) {
          Picasso.get().load(groupImageStr).placeholder(R.drawable.profile_image).into(groupProfileImage);



      }
  }




    private void initializeFields() {

        joinGroup =findViewById(R.id.join_group_button);
        groupNameEditText = findViewById(R.id.group_name);
        groupProfileImage = findViewById(R.id.set_profile_image);
        groupsToolBar = findViewById(R.id.join_to_group_toolbar);
        setSupportActionBar(groupsToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Join Group");

    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(JoinToGroupActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }



}
