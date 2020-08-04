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

import com.example.groupchatapp.FirebaseListenerService;
import com.example.groupchatapp.LoginManager;
import com.example.groupchatapp.R;
import com.google.android.gms.tasks.Continuation;
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

    private Button updateAccountSettings, logOutButton;
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

        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                m_LoginManager.Logout();
                FirebaseListenerService.removeAllChildListeners();
                sendUserToLoginActivity();

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

    private void sendUserToLoginActivity() {

        Intent loginIntent = new Intent(SettingsActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
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
                uploadImageToStorage();
            }

            m_LoginManager.getLoggedInUser().getValue().setStatus(setStatus);
            m_LoginManager.getLoggedInUser().getValue().setName(setUserName);

            profileMap.put("name",setUserName);

            if (setStatus.isEmpty()) {
                setStatus = "Available";
            }

            profileMap.put("status",setStatus);

            RootRef.child("Users").child(currentUserID).updateChildren(profileMap).addOnCompleteListener(task -> {
                if(task.isSuccessful())
                {
                    SendUserToMainActivity();
                    Toast.makeText(SettingsActivity.this,"Profile updated seccessfully",Toast.LENGTH_SHORT).show();
                }
                else
                {
                    //String message =task.getException().toString();
                    //Toast.makeText(SettingsActivity.this,"Error:" +message,Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private void RetrieveUserInfo()
    {

                String retrieveStatus = m_LoginManager.getLoggedInUser().getValue().getStatus()!=null ? m_LoginManager.getLoggedInUser().getValue().getStatus() : "";
                String retrieveProfileImage = m_LoginManager.getLoggedInUser().getValue().getPhotoUrl();

                userName.setText(m_LoginManager.getLoggedInUser().getValue().getName());
                userStatus.setText(retrieveStatus);
                if(retrieveProfileImage==null)
                {
                    userProfileImage.setImageResource(R.drawable.profile_image);
                }
                else {
                    Picasso.get().load(retrieveProfileImage).into(userProfileImage);
                }
    }

    private void initializeFields() {

        updateAccountSettings=findViewById(R.id.upadte_settings_button);
        logOutButton=findViewById(R.id.log_out_button);
        userName = findViewById(R.id.set_user_name);
        userStatus = findViewById(R.id.set_profile_status);
        userProfileImage = findViewById(R.id.set_group_image);
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

    private void uploadImageToStorage() {
        StorageReference filePath = UserProfileImageRef.child(currentUserID + ".jpg");
        filePath.putFile(imageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return filePath.getDownloadUrl();
            }
        }).
                addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if (task.isSuccessful())
                        {
                            //Toast.makeText(SettingsActivity.this,"Profile image uploaded successfully",Toast.LENGTH_SHORT).show();
                            final String downloadUrl = task.getResult().toString();
                    RootRef.child("Users").child(currentUserID).child("photoUrl")
                            .setValue(downloadUrl);
                    m_LoginManager.getLoggedInUser().getValue().setPhotoUrl(downloadUrl);
                }
                else
                {
                    String message = task.getException().toString();
                    //Toast.makeText(SettingsActivity.this,"Error "+message,Toast.LENGTH_SHORT).show();
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
