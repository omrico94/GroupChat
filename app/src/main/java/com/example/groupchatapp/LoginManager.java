package com.example.groupchatapp;

import androidx.lifecycle.MutableLiveData;

import com.example.groupchatapp.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginManager {

    private static LoginManager Instance = null;
    private boolean isLoggedIn;
    private DatabaseReference userRef;
     private FirebaseAuth mAuth;

     //יש מצב שאפשר לעשות אותו פשוט user
    private MutableLiveData<User> m_CurrentUser;


    public MutableLiveData<User> getLoggedInUser(){

        if (!isLoggedIn) {
            throw new NullPointerException("User is not login");
        }

        return m_CurrentUser;
    }

    public static synchronized LoginManager getInstance() {
        if (Instance == null) {
            Instance = new LoginManager();
        }

        return Instance;
    }

    private LoginManager() {

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();
        isLoggedIn = false;

    }


    public boolean IsLoggedIn() {
        return isLoggedIn;
    }

    public FirebaseUser getFireBaseCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void Logout()
    {
        mAuth.signOut();
        //אפשר אולי להוסיף כאן משהו שישמור את כל המידע בדאטה בייס, למקרה שיש משהו לא שמור
        m_CurrentUser.setValue(null);
        isLoggedIn=false;
    }

    public void Login(OnLoggedIn listener) {

        listener.onStart();

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                isLoggedIn = true;
                m_CurrentUser = new MutableLiveData<>();
                m_CurrentUser.setValue(dataSnapshot.child(mAuth.getCurrentUser().getUid()).getValue(User.class));
                listener.onSuccess();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

                listener.onFailure();
            }
        });

    }

    public boolean isUserExist()
    {
        return mAuth.getCurrentUser()!=null;
    }


    public void addNewGroupIdToCurrentUser(String groupId)
    {
        m_CurrentUser.getValue().getGroupsId().put(groupId,groupId);
        userRef.child(m_CurrentUser.getValue().getUid()).child("groupsId").child(groupId).setValue(groupId);
    }

    public void removeGroupIdFromCurrentUser(String groupId)
    {
        m_CurrentUser.getValue().getGroupsId().remove(groupId);
        userRef.child(m_CurrentUser.getValue().getUid()).child("groupsId").child(groupId).removeValue();
    }

}
