package com.example.groupchatapp;

import androidx.lifecycle.MutableLiveData;

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

//  public Task<AuthResult> CreateUserWithEmailAndPassword(String email,String password)
//  {
//      return mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//          @Override
//          public void onComplete(@NonNull Task<AuthResult> task)
//          {

//              if(task.isSuccessful())
//              {
//                 m_FireBaseCurrentUser=mAuth.getCurrentUser();
//                 final String deviceToken = FirebaseInstanceId.getInstance().getToken();

//                 m_FireBaseCurrentUser = mAuth.getCurrentUser();

//                 userRef.child(m_FireBaseCurrentUser.getUid()).setValue("");

//                 userRef.child(m_FireBaseCurrentUser.getUid()).child("token").setValue(deviceToken);

//              }
//          }

//      });
//  }

    public FirebaseUser getFireBaseCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public void Logout()
    {
        mAuth.signOut();
        m_CurrentUser.setValue(null);
        isLoggedIn=false;
    }

    public void Login()
    {
        isLoggedIn=true;
        m_CurrentUser = new MutableLiveData<>();
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                m_CurrentUser.setValue(dataSnapshot.child(mAuth.getCurrentUser().getUid()).getValue(User.class));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public boolean isNewUser()
    {
       return m_CurrentUser.getValue().getName()==null;
    }

    public boolean isUserExist()
    {
        return mAuth.getCurrentUser()!=null;
    }


    public void addNewGroupIdToCurrentUser(String groupId)
    {
        m_CurrentUser.getValue().getGroupsId().add(groupId);
        userRef.child(m_CurrentUser.getValue().getUid()).child("groupsId").setValue( m_CurrentUser.getValue().getGroupsId());

    }

    public void removeGroupIdFromCurrentUser(String groupId)
    {
        m_CurrentUser.getValue().getGroupsId().remove(groupId);
        userRef.child(m_CurrentUser.getValue().getUid()).child("groupsId").setValue( m_CurrentUser.getValue().getGroupsId());
    }

}
