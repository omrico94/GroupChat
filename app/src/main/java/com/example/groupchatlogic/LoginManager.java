package com.example.groupchatlogic;


import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;


public class LoginManager
{
    private static LoginManager Instance = null;

    private DatabaseReference RootRef, userRef;
    private FirebaseAuth mAuth;

    public static synchronized LoginManager getInstance() {
        if (Instance == null) {
            Instance = new LoginManager();
        }

        return Instance;
    }


    private LoginManager() {
        mAuth = FirebaseAuth.getInstance();
        RootRef = FirebaseDatabase.getInstance().getReference();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    public boolean IsCurrentUserExist()
    {
        return mAuth.getCurrentUser() != null;
    }


    public void CreateNewAccountWithEmailAndPassword(String email, String password)
    {
        mAuth.createUserWithEmailAndPassword(email, password);
        final String deviceToken = FirebaseInstanceId.getInstance().getToken();
        final String currentUserID = mAuth.getCurrentUser().getUid();
        RootRef.child("Users").child(currentUserID).setValue("");
        RootRef.child("Users").child(currentUserID).child("device_token").setValue(deviceToken);
    }


    public void VerifyUserExistence(String email, String password)
    {
        mAuth.signInWithEmailAndPassword(email, password);
        final String currentUserId = mAuth.getCurrentUser().getUid();
        final String deviceToken = FirebaseInstanceId.getInstance().getToken();
        userRef.child(currentUserId).child("device_token").setValue(deviceToken);
    }

    public void SignOut()
    {
        mAuth.signOut();
    }
    
    
    public String GetCurrentUserID()
    {
        return mAuth.getCurrentUser().getUid();
    }
}