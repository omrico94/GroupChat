package com.example.groupchatapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.groupchatapp.LoginManager;
import com.example.groupchatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateGroupActivity extends AppCompatActivity {

    private Button updateGroupButton;
    private EditText groupName, groupDescription,groupPassword;
    private CircleImageView groupImage;
    private DatabaseReference RootRef;
    private StorageReference groupImageRef;
    private ProgressDialog loadingBar;
    private Toolbar createGroupToolBar;
    private LoginManager m_LoginManager;
    private  Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        RootRef= FirebaseDatabase.getInstance().getReference();
        groupImageRef = FirebaseStorage.getInstance().getReference().child("Group images");

        initializeFields();


        updateGroupButton.setOnClickListener(v -> UpdateSettings());

 //       RetrieveUserInfo();

        groupImage.setOnClickListener(view -> {

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        });

        m_LoginManager = LoginManager.getInstance();
    }

    private void UpdateSettings() {

        String setGroupName = groupName.getText().toString();

        String setGroupDescription = groupDescription.getText().toString();

        String setGroupPassword = groupPassword.getText().toString();

        if(TextUtils.isEmpty(setGroupName))
        {
            Toast.makeText(this,"Please write your group name first",Toast.LENGTH_SHORT).show();
        }

     //  if(TextUtils.isEmpty(setGroupCode))
     //  {
     //      Toast.makeText(this,"Please write your status",Toast.LENGTH_SHORT).show();
     //  }

        else
        {

            final String currentUserId=m_LoginManager.getLoggedInUser().getValue().getUid();
            final Map usersIdMap  = new HashMap(){
                {
                    put(currentUserId,currentUserId);
                }};


            final String groupId = RootRef.child("Groups").push().getKey();
            if(imageUri!=null)
            { uploadImageToStorage(groupId);}

            HashMap<String,Object> profileMap=new HashMap<>();
            profileMap.put("gid",groupId);
            profileMap.put("name",setGroupName);
            profileMap.put("description",setGroupDescription);
            profileMap.put("latitude",getIntent().getExtras().get("latitude").toString());
            profileMap.put("longitude",getIntent().getExtras().get("longitude").toString());
            profileMap.put("usersId",usersIdMap);
            if(!setGroupPassword.isEmpty())
            {
                profileMap.put("password",setGroupPassword);
            }

            RootRef.child("Groups").child(groupId).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        m_LoginManager.addNewGroupIdToCurrentUser(groupId);
                        SendUserToMyGroupsActivity();
                        Toast.makeText(CreateGroupActivity.this,"Group created successfully",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        String message =task.getException().toString();
                        Toast.makeText(CreateGroupActivity.this,"Error:" +message,Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void uploadImageToStorage(String groupId) {
        StorageReference filePath = groupImageRef.child(groupId + ".jpg");
        filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if (task.isSuccessful())
                {
                    Toast.makeText(CreateGroupActivity.this,"Group image uploaded successfully",Toast.LENGTH_SHORT).show();
                    final String downloadUrl = task.getResult().getDownloadUrl().toString();
                    RootRef.child("Groups").child(groupId).child("photoUrl")
                            .setValue(downloadUrl);
                }
                else
                {
                    String message = task.getException().toString();
                    Toast.makeText(CreateGroupActivity.this,"Error "+message,Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //private void RetrieveUserInfo() פונקציה שתתאים לנו שנרצה לאפשר עריכה של פרטים לקבוצה קיימת
   //{
   //    RootRef.child("new Group").child(currentUserID).addValueEventListener(new ValueEventListener()
   //    {
   //
   //
   //     @Override
   //     public void onDataChange(DataSnapshot dataSnapshot)
   //     {
   //         if((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
   //         {
   //             String retrieveGroupName = dataSnapshot.child("name").getValue().toString();
   //             String retrieveGroupCode = dataSnapshot.child("code").getValue().toString();
   //             groupName.setText(retrieveGroupName);
   //             groupDescription.setText(retrieveGroupCode);
////
   //             if(dataSnapshot.hasChild("image"))
   //             {
   //                 String retrieveProfileImage = dataSnapshot.child("image").getValue().toString();
   //                 Picasso.get().load(retrieveProfileImage).into(groupImage);
   //             }
////
   //         }
////
   //         else
   //         {
   //             groupName.setVisibility(View.VISIBLE);
   //             Toast.makeText(CreateGroupActivity.this,"Please set and update your profile information",Toast.LENGTH_SHORT).show();
   //         }
   //     }
//
   //        @Override
   //        public void onCancelled(DatabaseError databaseError)
   //        {
//
   //        }
   //    });
   //}

   private void initializeFields() {

       updateGroupButton =findViewById(R.id.update_group_button);
       groupName = findViewById(R.id.set_group_name);
       groupPassword = findViewById(R.id.set_group_password);

       groupDescription = findViewById(R.id.set_group_description);
       groupImage = findViewById(R.id.set_group_image);
       loadingBar = new ProgressDialog(this);

       createGroupToolBar = findViewById(R.id.create_group_toolbar);
       setSupportActionBar(createGroupToolBar);
       getSupportActionBar().setDisplayHomeAsUpEnabled(true);
       getSupportActionBar().setDisplayShowCustomEnabled(true);
       getSupportActionBar().setTitle("Create New Group");
   }

   private void SendUserToMainActivity()
   {
 //     Intent mainIntent = new Intent(CreateGroupActivity.this,MainActivity.class);
 //     mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
 //     startActivity(mainIntent);
       finish();
   }


    private void SendUserToMyGroupsActivity()
    {
             Intent myGroupsIntent = new Intent(CreateGroupActivity.this,MyGroupsActivity.class);
        myGroupsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
             startActivity(myGroupsIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                loadingBar.setTitle("Set Group Image");
                loadingBar.setMessage("Please wait,your group image is updating");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                imageUri = result.getUri();
                loadingBar.dismiss();
                Picasso.get().load(imageUri).into(groupImage);

            }

         // else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
         //     Exception error = result.getError();
         // }
        }

    }
}