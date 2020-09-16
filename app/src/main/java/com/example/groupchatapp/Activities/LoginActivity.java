package com.example.groupchatapp.Activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.groupchatapp.LoginManager;
import com.example.groupchatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity {

    private Button LoginButton;
    private EditText UserEmail,UserPassword;
    private TextView NeedNewAccountLink,ForgetPasswordLink;
    private ProgressDialog loadingBar;
    private DatabaseReference userRef;
    private LoginManager m_LoginManager;
    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private ProgressDialogActivity ProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        m_LoginManager = LoginManager.getInstance();
        mAuth=FirebaseAuth.getInstance();

        userRef= FirebaseDatabase.getInstance().getReference().child("Users");
        initializeFields();


        NeedNewAccountLink.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                SendUserToRegisterActivity();
            }
        });

        LoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AllowUserToLogin();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (m_LoginManager.isUserExist()) {

            SendUserToMapsActivity();
        }
    }

    private void AllowUserToLogin() {
        String email=UserEmail.getText().toString();
        String password=UserPassword.getText().toString();

        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(this,"Please enter email", Toast.LENGTH_SHORT).show();

        }
        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(this,"Please enter password", Toast.LENGTH_SHORT).show();
        }
        else
        {
            ProgressDialog.startDialog();



            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        final String currentUserId=m_LoginManager.getFireBaseCurrentUser().getUid();
                        final String deviceToken = FirebaseInstanceId.getInstance().getToken();
                        userRef.child(currentUserId).child("token").setValue(deviceToken)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful())
                                        {
                                            SendUserToMapsActivity();
                                            //Toast.makeText(LoginActivity.this,"Logged in successful",Toast.LENGTH_SHORT).show();
                                            ProgressDialog.dismissDialog();
                                        }
                                    }
                                });
                    }
                    else
                    {
                        //String message=task.getException().toString();
                        Toast.makeText(LoginActivity.this,"Error: Incorrect username or password",Toast.LENGTH_SHORT).show();
                        ProgressDialog.dismissDialog();
                    }
                }
            });
        }
    }

    private void  initializeFields()
    {
        LoginButton = findViewById(R.id.login_button);
        UserEmail = findViewById(R.id.login_email);
        UserPassword = findViewById(R.id.login_password);
        NeedNewAccountLink = findViewById(R.id.need_new_account_link);
        ForgetPasswordLink = findViewById(R.id.forget_password_link);

        ProgressDialog = new ProgressDialogActivity(LoginActivity.this);

    }

    private void SendUserToRegisterActivity()
    {
        Intent registerIntent = new Intent(LoginActivity.this,RegisterActivity.class);
        startActivity(registerIntent);
    }

    private void SendUserToMapsActivity()
    {
        Intent mapIntent = new Intent(LoginActivity.this,MapsActivity.class);
        mapIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mapIntent);
        finish();
    }


}
