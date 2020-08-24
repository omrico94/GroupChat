package com.example.groupchatapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.groupchatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class RegisterActivity extends AppCompatActivity
{
    private Button CreateAccountButton;
    private EditText UserEmail,UserPassword,VerifyPassword, UserName;
    private TextView AlreadyHaveAnAccountLink;
    private FirebaseAuth mAuth;
    private ProgressDialog loadingBar;
    private DatabaseReference RootRef;
    private ProgressDialogActivity ProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth=FirebaseAuth.getInstance();
        RootRef= FirebaseDatabase.getInstance().getReference();
        initializeFields();


        AlreadyHaveAnAccountLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                SendUserToLoginActivity();
            }
        });

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount()
    {
        String email=UserEmail.getText().toString();
        String password=UserPassword.getText().toString();
        String verifyPassword = VerifyPassword.getText().toString();
        String userName = UserName.getText().toString();

        if(TextUtils.isEmpty((userName)))
        {
            Toast.makeText(this,"Please enter your name", Toast.LENGTH_SHORT).show();
        }
        else if(TextUtils.isEmpty(email))
        {
            Toast.makeText(this,"Please enter your email", Toast.LENGTH_SHORT).show();

        }
        else if(TextUtils.isEmpty(password))
        {
            Toast.makeText(this,"Please enter password", Toast.LENGTH_SHORT).show();

        }
        else if(!TextUtils.equals(password,verifyPassword))
        {
            String message;

            if(TextUtils.isEmpty(verifyPassword))
            {
                message = "Please verify your password";
            }
            else
            {
                message = "Passwords doesn't match";
            }

            Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
            UserPassword.setBackgroundResource(R.drawable.rounded_register_red);
            VerifyPassword.setBackgroundResource(R.drawable.rounded_register_red);
        }
        else
        {
            ProgressDialog.startDialog();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        final String deviceToken = FirebaseInstanceId.getInstance().getToken();
                        final String currentUserID = mAuth.getCurrentUser().getUid();
                        RootRef.child("Users").child(currentUserID).setValue("");
                        RootRef.child("Users").child(currentUserID).child("token").setValue(deviceToken);
                        RootRef.child("Users").child(currentUserID).child("name").setValue(userName);
                        RootRef.child("Users").child(currentUserID).child("id").setValue(currentUserID);
                        SendUserToMapsActivity();
                        Toast.makeText(RegisterActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                        ProgressDialog.dismissDialog();
                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(RegisterActivity.this, "Error:" + message, Toast.LENGTH_SHORT).show();
                        ProgressDialog.dismissDialog();
                    }
                }
            });
        }

     }

    private void  initializeFields()
    {
        CreateAccountButton = findViewById(R.id.register_button);
        UserEmail = findViewById(R.id.register_email);
        UserPassword = findViewById(R.id.register_password);
        VerifyPassword = findViewById(R.id.verify_password);
        UserName = findViewById(R.id.register_name);
        AlreadyHaveAnAccountLink = findViewById(R.id.already_have_account_link);
        ProgressDialog = new ProgressDialogActivity(RegisterActivity.this);
    }

    private void SendUserToLoginActivity()
    {
        //Intent loginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
        //startActivity(loginIntent);
        finish();
    }

    private void SendUserToMapsActivity()
    {
        Intent mapsIntent = new Intent(RegisterActivity.this,MapsActivity.class);
        mapsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mapsIntent);
        finish();
    }

}
