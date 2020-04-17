package com.example.groupchatapp.Activities;

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

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button updateAccountSettings;
    private EditText userName,userStatus;
    private CircleImageView userProfileImage;
    private String currentUserID;
    private DatabaseReference RootRef;
    private StorageReference UserProfileImageRef;
    private ProgressDialog loadingBar;
    private Toolbar settingsToolBar;
    private  Uri imageUri;
    private LoginManager m_LoginManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        m_LoginManager = LoginManager.getInstance();
        currentUserID=m_LoginManager.getFireBaseCurrentUser().getUid();
        RootRef= FirebaseDatabase.getInstance().getReference();
        UserProfileImageRef = FirebaseStorage.getInstance().getReference().child("Profile images");

        initializeFields();

        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });

        RetrieveUserInfo();

        userProfileImage.setOnClickListener(view -> {

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        });
    }

    private void UpdateSettings() {

        String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();

        if(TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(this,"Please write your user name first",Toast.LENGTH_SHORT).show();
        }

//        Status is not mandatory!!!
//        if(TextUtils.isEmpty(setStatus))
//        {
//            Toast.makeText(this,"Please write your status",Toast.LENGTH_SHORT).show();
//        }

        else
        {
            HashMap<String,Object> profileMap=new HashMap<>();

            if(imageUri!=null)
            {
                uploadImageToStorage(currentUserID);
            }

            m_LoginManager.getLoggedInUser().getValue().setStatus(setStatus);
            m_LoginManager.getLoggedInUser().getValue().setName(setUserName);

            profileMap.put("name",setUserName);
            profileMap.put("status",setStatus);

            RootRef.child("Users").child(currentUserID).updateChildren(profileMap).addOnCompleteListener(task -> {
                if(task.isSuccessful())
                {
                    SendUserToMainActivity();
                    Toast.makeText(SettingsActivity.this,"Profile updated seccessfully",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    String message =task.getException().toString();
                    Toast.makeText(SettingsActivity.this,"Error:" +message,Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private void RetrieveUserInfo()
    {

                String retrieveStatus = m_LoginManager.getLoggedInUser().getValue().getStatus()!=null ? m_LoginManager.getLoggedInUser().getValue().getStatus() : "";
                String retrieveProfileImage = m_LoginManager.getLoggedInUser().getValue().getPhotoUrl()!=null ?  m_LoginManager.getLoggedInUser().getValue().getPhotoUrl() : "default_image";

                userName.setText(m_LoginManager.getLoggedInUser().getValue().getName());
                userStatus.setText(retrieveStatus);
                Picasso.get().load(retrieveProfileImage).placeholder(R.drawable.profile_image).into(userProfileImage);

    }

    private void initializeFields() {

        updateAccountSettings=findViewById(R.id.upadte_settings_button);
        userName = findViewById(R.id.set_user_name);
        userStatus = findViewById(R.id.set_profile_status);
        userProfileImage = findViewById(R.id.set_profile_image);
        loadingBar = new ProgressDialog(this);

        settingsToolBar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(settingsToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");
    }

    private void SendUserToMainActivity()
    {
        finish();
    }

    private void uploadImageToStorage(String groupId) {
        StorageReference filePath = UserProfileImageRef.child(groupId + ".jpg");
        filePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if (task.isSuccessful())
                {
                    Toast.makeText(SettingsActivity.this,"profile image uploaded successfully",Toast.LENGTH_SHORT).show();
                    final String downloadUrl = task.getResult().getDownloadUrl().toString();
                    RootRef.child("Users").child(currentUserID).child("photoUrl")
                            .setValue(downloadUrl);
                    m_LoginManager.getLoggedInUser().getValue().setPhotoUrl(downloadUrl);
                }
                else
                {
                    String message = task.getException().toString();
                    Toast.makeText(SettingsActivity.this,"Error "+message,Toast.LENGTH_SHORT).show();
                }
            }
        });
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
                Picasso.get().load(imageUri).into(userProfileImage);

            }

            // else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
            //     Exception error = result.getError();
            // }
        }

    }
}
