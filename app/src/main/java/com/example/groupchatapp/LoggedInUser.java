package com.example.groupchatapp;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoggedInUser {

    private static LoggedInUser Instance = null;
    private boolean isLoggedIn;
    private DatabaseReference userRef;
     private FirebaseUser m_FireBaseCurrentUser;
     private FirebaseAuth mAuth;
    private LiveData<User> m_CurrentUser;

    public LiveData<User> getCurrentUser(){

        if (!isFireBaseUserExsist()) {
            throw new NullPointerException("User is not LoggedIn");
        }

        return m_CurrentUser;
    }

    public static synchronized LoggedInUser getInstance() {
        if (Instance == null) {
            Instance = new LoggedInUser();
        }

        return Instance;
    }

    //////////////////לבדוק...
    private LoggedInUser() {

        
        m_FireBaseCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        isLoggedIn = false;

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                m_CurrentUser = dataSnapshot.child(m_FireBaseCurrentUser.getUid()).getValue(User.class);
                isLoggedIn = true;
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }




    public boolean IsLoggedIn() {
        return isLoggedIn;
    }

    private boolean isFireBaseUserExsist() {
        return m_FireBaseCurrentUser != null;
    }

    public Task<AuthResult> CreateUserWithEmailAndPassword(String email,String password)
    {

        return mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {

                if(task.isSuccessful())
                {
                    final String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    m_FireBaseCurrentUser = mAuth.getCurrentUser();
                    userRef.child(m_FireBaseCurrentUser.getUid()).setValue("");
                    userRef.child(m_FireBaseCurrentUser.getUid()).child("token").setValue(deviceToken);

                }
            }

        });
    }



}
