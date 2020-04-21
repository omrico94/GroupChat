package com.example.groupchatapp;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.example.groupchatapp.Models.Group;
import com.example.groupchatapp.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class LoginManager {

    private static LoginManager Instance = null;
    private boolean isLoggedIn;
    private DatabaseReference m_UsersRef,m_GroupsRef;
    private FirebaseAuth mAuth;
    //   private HashMap<String,Group> m_MyGroupsMap;
    private LocationManager m_LocationManager;

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

 //       m_MyGroupsMap =new HashMap<>();
        m_UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        m_GroupsRef=FirebaseDatabase.getInstance().getReference().child("Groups");
        mAuth = FirebaseAuth.getInstance();
        isLoggedIn = false;
        m_LocationManager = new LocationManager();
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
//        m_MyGroupsMap.clear();
        m_CurrentUser.setValue(null);
        isLoggedIn=false;
        m_LocationManager.Logout();
    }

    public void Login(OnLoggedIn listener) {

        listener.onStart();
        ValueEventListener m_LoginValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                isLoggedIn = true;
                m_CurrentUser = new MutableLiveData<>();
                m_CurrentUser.setValue(dataSnapshot.child(mAuth.getCurrentUser().getUid()).getValue(User.class));
                listener.onSuccess();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        m_UsersRef.addListenerForSingleValueEvent(m_LoginValueListener);
    }

    public boolean isUserExist()
    {
        return mAuth.getCurrentUser()!=null;
    }


    public void addNewGroupIdToCurrentUser(String groupId)
    {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        String dateStr=formatter.format(date);
        m_CurrentUser.getValue().getGroupsId().put(groupId,dateStr);
        m_UsersRef.child(m_CurrentUser.getValue().getUid()).child("groupsId").child(groupId).setValue(dateStr);
    }

    public void removeGroupIdFromCurrentUser(String groupId)
    {
        m_CurrentUser.getValue().getGroupsId().remove(groupId);
        m_UsersRef.child(m_CurrentUser.getValue().getUid()).child("groupsId").child(groupId).removeValue();
    }

    public LocationManager getLocationManager() { return m_LocationManager; }


 //  private void initMyGroupsChildEventListener() {

 //      for (String groupId : m_CurrentUser.getValue().getGroupsId().keySet()) {
 //          m_GroupsRef.child(m_LocationManager.getCountryCode()).child(groupId).addListenerForSingleValueEvent(new ValueEventListener() {
 //              @Override
 //              public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

 //                  m_MyGroupsMap.put(groupId, dataSnapshot.getValue(Group.class));
 //              }

 //              @Override
 //              public void onCancelled(@NonNull DatabaseError databaseError) {

 //              }
 //          });
 //      }

 //  }

 //  public Map<String, Group> GetCurrentUserGroupsMap()
 //  {
 //      if(m_LocationManager.getCountryCode()==null)
 //      {
 //          throw new NullPointerException("Cannot fetch group user because location is disable");
 //      }

 //      initMyGroupsChildEventListener();
 //      return Collections.unmodifiableMap(m_MyGroupsMap);
 //  }
}
