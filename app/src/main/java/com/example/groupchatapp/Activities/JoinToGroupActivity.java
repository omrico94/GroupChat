package com.example.groupchatapp.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.groupchatapp.LoginManager;
import com.example.groupchatapp.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class JoinToGroupActivity extends AppCompatActivity {

    private Button joinGroup;
    private TextView groupNameEditText;
    private CircleImageView groupProfileImage;
    private String groupId,groupName,groupImageStr,groupPassword;
    private DatabaseReference RootRef;
    private Toolbar groupsToolBar;
    private EditText groupPasswordEditText;

    private LoginManager m_LoginManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_to_group);

        m_LoginManager = LoginManager.getInstance();
        RootRef= FirebaseDatabase.getInstance().getReference();
        groupId =getIntent().getExtras().get("group_id").toString();
        groupName =getIntent().getExtras().get("group_name").toString();
        if(getIntent().getExtras().get("group_image")!= null) {//maybe their is a better way to handle this
            groupImageStr = getIntent().getExtras().get("group_image").toString();
        }


        initializeFields();

        joinGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (m_LoginManager.getLocationManager().isLocationOn()) {
                    joinGroup();
                } else {
                    Toast.makeText(JoinToGroupActivity.this, "Turn on location!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        RetrieveUserInfo();

    }
    private void joinGroup() {

        if(groupPasswordEditText.getVisibility()==View.INVISIBLE || groupPasswordEditText.getText().toString().equals(groupPassword))
        {
            m_LoginManager.addNewGroupIdToCurrentUser(groupId);
            String uid=m_LoginManager.getLoggedInUser().getValue().getUid();
            String countryCode = m_LoginManager.getLocationManager().getCountryCode();
            m_LoginManager.addNewGroupIdToCurrentUser(groupId);
            RootRef.child("Groups").child(countryCode).child(groupId).child("usersId").child(uid).setValue(uid);
            //RootRef.child("Users").child(uid).child("groupsId").setValue(groupId);//אולי
            //אולי להוסיף פה את ההכנסה של הקבוצה למערך של הקבוצות של היוזר
            SendUserToChatActivity();
        }

        else
        {
            Toast.makeText(this,"invalid password", Toast.LENGTH_SHORT).show();
        }


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
        groupPasswordEditText = findViewById(R.id.password_to_join);
        if(!isPublicGroup())
        {
            groupPassword =getIntent().getExtras().get("group_password").toString();
            groupPasswordEditText.setVisibility(View.VISIBLE);
        }

        setSupportActionBar(groupsToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Join Group");

    }

    private void SendUserToChatActivity()
    {
        Intent chatIntent = new Intent(this, ChatActivity.class);
        chatIntent.putExtra("group_id", groupId);
        chatIntent.putExtra("group_name", groupName);
        chatIntent.putExtra("group_image", groupImageStr);
        this.startActivity(chatIntent);
        finish();
    }

    private boolean isPublicGroup()
    {
        return getIntent().getExtras().get("group_password")==null;
    }


}
