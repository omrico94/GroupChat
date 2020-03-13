package com.example.groupchatlogic;

import android.icu.util.IslamicCalendar;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.groupchatapp.LoginActivity;
import com.example.groupchatapp.RegisterActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginManager
{
    private static  LoginManager Instance = null;

    private FirebaseUser currentUser;
    private DatabaseReference RootRef, userRef;
    private FirebaseAuth mAuth;
    public String Exception;

    public static LoginManager getInstance()
    {
        if(Instance == null)
        {
           Instance = new LoginManager();
        }

        return Instance;
    }

    private LoginManager()
    {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        RootRef = FirebaseDatabase.getInstance().getReference();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    public boolean IsCurrentUserExist()
    {
        boolean isExist = false;
        if(currentUser != null)
        {
            isExist = true;
        }

        return isExist;
    }


    public void CreateNewAccount(String email,String password)
    {
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>()
            {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task)
                {
                    if(task.isSuccessful())
                    {
                        final String deviceToken = FirebaseInstanceId.getInstance().getToken();
                        final String currentUserID = mAuth.getCurrentUser().getUid();
                        RootRef.child("Users").child(currentUserID).setValue("");
                        RootRef.child("Users").child(currentUserID).child("device_token").setValue(deviceToken);

                        Exception = null;
                    }
                    else
                    {
                        Exception = task.getException().toString();
                    }
                }
            });
    }

    public void AllowUserToLogin(String email,String password)
    {
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful())
                    {
                        final String currentUserId = mAuth.getCurrentUser().getUid();
                        final String deviceToken = FirebaseInstanceId.getInstance().getToken();
                        userRef.child(currentUserId).child("device_token").setValue(deviceToken)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful())
                                        {
                                           Exception = null;
                                        }
                                    }
                                });
                    }
                    else
                    {
                       Exception = task.getException().toString();
                    }
                }
            });
        }
    }


