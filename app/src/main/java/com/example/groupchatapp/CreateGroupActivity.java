package com.example.groupchatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateGroupActivity extends AppCompatActivity {

    private Button updateGroupButton;
    private EditText groupName, groupDescription;
    private CircleImageView groupImage;
    private DatabaseReference RootRef;
    private StorageReference groupImageRef;
    private ProgressDialog loadingBar;
    private Toolbar createGroupToolBar;
    private static final int galleryPic=1;
    private String uniqueID;
    private LoginManager m_LoginManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        RootRef= FirebaseDatabase.getInstance().getReference();
        groupImageRef = FirebaseStorage.getInstance().getReference().child("Group images");

        initializeFields();


        updateGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

 //       RetrieveUserInfo();

        groupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent galleryIntent=new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent,galleryPic);
            }
        });

        m_LoginManager = LoginManager.getInstance();
    }

    private void UpdateSettings() {

        String setGroupName = groupName.getText().toString();

        String setGroupDescription = groupDescription.getText().toString();

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
            final String groupId = RootRef.child("Groups").push().getKey();
            uniqueID = groupId;
            HashMap<String,Object> profileMap=new HashMap<>();
            profileMap.put("gid",groupId);
            profileMap.put("name",setGroupName);
            profileMap.put("description",setGroupDescription);
            profileMap.put("latitude",getIntent().getExtras().get("latitude").toString());
            profileMap.put("longitude",getIntent().getExtras().get("longitude").toString());
            profileMap.put("numberOfUsers","1");



            RootRef.child("Groups").child(groupId).updateChildren(profileMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        m_LoginManager.addNewGroupIdToCurrentUser(groupId);
                        RootRef.child("Users").
                                child(m_LoginManager.getLoggedInUser().getValue().getUid()).
                                child("groupsId").setValue(m_LoginManager.getLoggedInUser().getValue().getGroupsId());
                        SendUserToMainActivity();
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
       groupDescription = findViewById(R.id.set_group_code);
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
       Intent mainIntent = new Intent(CreateGroupActivity.this,MainActivity.class);
       mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
       startActivity(mainIntent);
       finish();
   }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==galleryPic && resultCode==RESULT_OK && data!=null)
        {
            //חסר משהו. צריך לראות שוב את החלק הזה בסרטון של הsettings
            Uri imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {

                loadingBar.setTitle("Set Group Image");
                loadingBar.setMessage("Please wait,your group image is updating");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();

                StorageReference filePath = groupImageRef.child(uniqueID + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful())
                        {
                            Toast.makeText(CreateGroupActivity.this,"Group image uploaded successfully",Toast.LENGTH_SHORT).show();
                            final String downloadUrl = task.getResult().getDownloadUrl().toString();
                            RootRef.child("Groups").child(uniqueID).child("image")
                                    .setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if(task.isSuccessful())
                                            {
                                                Toast.makeText(CreateGroupActivity.this, "Image saved in databse", Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                            else
                                            {
                                                String message = task.getException().toString();
                                                Toast.makeText(CreateGroupActivity.this,"Error "+message,Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                        }
                                    });
                        }
                        else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(CreateGroupActivity.this,"Error "+message,Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
            }

            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }

    }
}

