package com.example.groupchatapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.groupchatapp.FirebaseListenerService;
import com.example.groupchatapp.LoginManager;
import com.example.groupchatapp.Models.Group;
import com.example.groupchatapp.OnLocationInit;
import com.example.groupchatapp.OnLocationLimitChange;
import com.example.groupchatapp.OnLocationPermissionChange;

import com.example.groupchatapp.OnLoggedIn;
import com.example.groupchatapp.R;
import com.example.groupchatapp.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ImageButton m_settingsButton, m_myGroupsButton, m_addGroupsButton,m_joinGroupButton,m_exitGroupButton;
    private DatabaseReference m_GroupsRef,m_UsersGroupsRef;
    private LoginManager m_LoginManager;
    private OnLoggedIn m_OnLoggedInListener;
    private OnLocationInit m_OnLocationInit;
    private OnLocationLimitChange m_OnLocationLimitChange;
    private OnLocationPermissionChange m_OnLocationpermissionChange;
    private ArrayMap<String,Marker> markers = new ArrayMap<String, Marker>();

    private Group currentGroup;
    private  Circle m_radiusCircle;
    private ChildEventListener m_newGroupsRefChildValueListener, m_UsersGroupsRefChildValueListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        m_LoginManager = LoginManager.getInstance();

        if (!m_LoginManager.IsLoggedIn()) {

            initLoggedInListener();
            initLocationInitListener();
            initLocationLimitChange();
            m_LoginManager.getLocationManager().setOnLocationLimitChange(m_OnLocationLimitChange, 50);
            m_LoginManager.Login(m_OnLoggedInListener);
        }
        initOnLocationPermissionChange();
        initializeFields();
        setOnClickButtons();

        hideJoinAndExitGroupButtons();//כל הפונקציות האלו לא עובדות נכון  אני בונה על זה שיהיה כפתורים במסך מידע

    }


    private void initializeFields() {

        m_settingsButton = findViewById(R.id.settings_button);
        m_myGroupsButton = findViewById(R.id.my_groups_button);
        m_addGroupsButton = findViewById(R.id.add_group_button);
        m_joinGroupButton = findViewById(R.id.join_group);
        m_exitGroupButton = findViewById(R.id.exit_group);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                currentGroup = (Group) marker.getTag();
                if(m_radiusCircle!=null) {
                    m_radiusCircle.remove();
                }

                    m_radiusCircle = mMap.addCircle(new CircleOptions()
                        .center(marker.getPosition())
                        .radius(Double.parseDouble(currentGroup.getRadius()))
                        .strokeColor(Color.BLUE));

                decideIfJoinOrExitButton(marker);

                return false;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                RestartMap();
            }
        });

    }

    private void decideIfJoinOrExitButton(Marker marker) {
        if(!m_LoginManager.getLoggedInUser().getValue().isUserInGroup(((Group) marker.getTag()).getId())
                && Utils.isGroupInMyLocation(currentGroup)) {
            showJoinGroupButton();
        }
        else if (m_LoginManager.getLoggedInUser().getValue().isUserInGroup(((Group) marker.getTag()).getId())) {
            showExitGroupButton();
        }
        else {
            hideJoinAndExitGroupButtons();
        }
    }

    private void RestartMap() {
        hideJoinAndExitGroupButtons();
        if(m_radiusCircle!=null) {
            m_radiusCircle.remove();
        }
        currentGroup = null;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (currentGroup != null && markers.containsKey(currentGroup.getId())) {
            decideIfJoinOrExitButton(markers.get(currentGroup.getId()));
        }
    }

    private void initLoggedInListener() {
        m_OnLoggedInListener = new OnLoggedIn() {
            @Override
            public void onSuccess() {
                initGroupsChildEventListener();
                m_LoginManager.getLocationManager().CheckPermissionLocation(MapsActivity.this , m_OnLocationInit);
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFailure() {

            }

        };
    }

    private void initOnLocationPermissionChange() {
        m_OnLocationpermissionChange = new OnLocationPermissionChange() {
            @Override
            public void onChange() {
                if(!m_LoginManager.getLocationManager().isLocationOn())
                {
                    mMap.setMyLocationEnabled(false);

                }else{
                    mMap.setMyLocationEnabled(true);
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(m_LoginManager.getLocationManager().GetLocationInLatLang(), 15.0f));

                }
            }
        };

        m_LoginManager.getLocationManager().setOnLocationPermssionChange(m_OnLocationpermissionChange);
    }

    public void  initGroupsChildEventListener() {

        m_newGroupsRefChildValueListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Group groupToAdd = dataSnapshot.getValue(Group.class);
                boolean isInGroup = m_LoginManager.getLoggedInUser().getValue().isUserInGroup(groupToAdd.getId());

                boolean isGroupInMyLocation = Utils.isGroupInMyLocation(groupToAdd);

                if (isInGroup && !isGroupInMyLocation) {
                    //The user is in the group but not in its location....
                    //Now we remove him from the group.
                    LoginManager.getInstance().exitFromGroup(groupToAdd.getId());
                    isInGroup = false;
                }

                createMarkerforGroup(groupToAdd, isInGroup, isGroupInMyLocation);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                Group changedGroup = dataSnapshot.getValue(Group.class);
                boolean isInGroup = m_LoginManager.getLoggedInUser().getValue().isUserInGroup(changedGroup.getId());
                boolean isGroupInMyLocation = Utils.isGroupInMyLocation(changedGroup);

                changeMarkerColor(isInGroup, changedGroup.getId(), isGroupInMyLocation);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                Group groupToRemove = dataSnapshot.getValue(Group.class);
                removeMarkerByGroupID(groupToRemove.getId());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }


    private void changeMarkerColor(boolean isInGroup,String groupID, boolean isGroupInMyLocation) {

        float markerColor = 210.0f;//קבוצה שאני לא ברדיוס שלה ולא בה - כחול (עמרי על אחריותך שזה יהיה אפור!!!!)

        if(isInGroup && isGroupInMyLocation) {
            markerColor = 120.0f;//קבוצה שאני בה וגם ברדיוס שלה - ירוק
        } else if (!isInGroup && isGroupInMyLocation) {
            markerColor = 0.0f;//קבוצה שאני לא בה אבל בתוך הרדיוס שלה - אדום
        }

        markers.get(groupID).setIcon(BitmapDescriptorFactory.defaultMarker(markerColor));
    }

    private void createMarkerforGroup(Group groupToAdd, boolean isInGroup, boolean isGroupInMyLocation) {

        Marker groupMarker;
        LatLng groupLocation = new LatLng(Double.valueOf(groupToAdd.getLatitude()), Double.valueOf(groupToAdd.getLongitude()));

        groupMarker =  mMap.addMarker(new MarkerOptions().position(groupLocation).title(groupToAdd.getName()).snippet(groupToAdd.getDescription()));
        groupMarker.setTag(groupToAdd);
        markers.put(groupToAdd.getId(),groupMarker);

        changeMarkerColor(isInGroup,groupToAdd.getId(),isGroupInMyLocation);
    }

    private void removeMarkerByGroupID(String groupId)
    {
        if (currentGroup != null && currentGroup.getId().equals(groupId)) {
            if (m_radiusCircle != null) {
                m_radiusCircle.remove();
                hideJoinAndExitGroupButtons();
            }
        }

        Marker markerToRemove = markers.get(groupId);
        markerToRemove.remove();
        markers.remove(groupId);
    }

    private void setOnClickButtons() {

        m_settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToSettingsActivity();
            }
        });

        m_myGroupsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (LoginManager.getInstance().getLocationManager().isLocationOn()) {
                    SendUserToMyGroupsActivity();
                }
            }
        });

        m_addGroupsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToCreateGroupActivity();
            }
        });

        m_joinGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showExitGroupButton();
                SendUserToJoinToGroupActivity();
            }
        });

        m_exitGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                LoginManager.getInstance().exitFromGroup(currentGroup.getId());
                                showJoinGroupButton();
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //No button clicked
                                break;
                        }
                    }
                };

                android.app.AlertDialog dialogAlert = new AlertDialog.Builder(MapsActivity.this, R.style.MyDialogTheme)
                        .setTitle("Confirm")
                        .setMessage("Do you want to leave " + currentGroup.getName() + "?")
                        .setPositiveButton("Yes",dialogClickListener)
                        .setNegativeButton("No", dialogClickListener)
                        .create();
                dialogAlert.show();

            }
        });
    }

    private void showExitGroupButton() {
        m_joinGroupButton.setVisibility(View.GONE);
        m_exitGroupButton.setVisibility(View.VISIBLE);
    }
    private void showJoinGroupButton() {
        m_joinGroupButton.setVisibility(View.VISIBLE);
        m_exitGroupButton.setVisibility(View.GONE);
    }
    private void hideJoinAndExitGroupButtons(){
        m_joinGroupButton.setVisibility(View.GONE);
        m_exitGroupButton.setVisibility(View.GONE);
    }

    private void SendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MapsActivity.this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void SendUserToMyGroupsActivity() {
        Intent myGroupsIntent = new Intent(MapsActivity.this, MyGroupsActivity.class);
        startActivity(myGroupsIntent);
    }

    private void SendUserToCreateGroupActivity() {
        Intent createGroupIntent = new Intent(MapsActivity.this, CreateGroupActivity.class);
        createGroupIntent.putExtra("longitude", m_LoginManager.getLocationManager().getLatitude());
        createGroupIntent.putExtra("latitude", m_LoginManager.getLocationManager().getLongitude());
        startActivity(createGroupIntent);
    }

    private void SendUserToJoinToGroupActivity()
    {
        Intent joinGroupIntent = new Intent(MapsActivity.this, JoinToGroupActivity.class);
        joinGroupIntent.putExtra("group", currentGroup);
        startActivity(joinGroupIntent);
    }

    private void initLocationLimitChange() {

        m_OnLocationLimitChange=new OnLocationLimitChange() {
            @Override
            public void onLimitChange() {
                Group group;
                boolean isGroupInMyLocation, isGroupInMyGroups;
                for (Map.Entry<String,Marker> pair : markers.entrySet()){

                    group = (Group) pair.getValue().getTag();

                    isGroupInMyLocation = Utils.isGroupInMyLocation(group);
                    isGroupInMyGroups = m_LoginManager.getLoggedInUser().getValue().isUserInGroup(group.getId());

                    if (isGroupInMyGroups && !isGroupInMyLocation) {
                        //The user is in the group but not in its location....
                        //Now we remove him from the group.
                        LoginManager.getInstance().exitFromGroup(group.getId());
                        isGroupInMyGroups = false;
                    }

                    if (currentGroup != null) {
                        decideIfJoinOrExitButton(markers.get(group.getId()));
                    }

                    changeMarkerColor(isGroupInMyGroups, group.getId(), isGroupInMyLocation);
                }
            }
        };
    }

    private void initLocationInitListener() {

        m_OnLocationInit=new OnLocationInit() {
            @Override
            public void onSuccess() {
                OnLocationProvide();
            }

            @Override
            public void onFailure() {

            }
        };
    }

    public void OnLocationProvide() {

        String countryCode = m_LoginManager.getLocationManager().getCountryCode();
        m_GroupsRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(countryCode);

        FirebaseListenerService.addChildEventListenerToRemoveList(m_GroupsRef,m_newGroupsRefChildValueListener);
        m_GroupsRef.addChildEventListener(m_newGroupsRefChildValueListener );


        m_UsersGroupsRef = FirebaseDatabase.getInstance().getReference().child("Users").child(LoginManager.getInstance().getLoggedInUser().getValue().getId()).child("groupsId");

        initUserGroupsIdListener();
        FirebaseListenerService.addChildEventListenerToRemoveList(m_UsersGroupsRef,m_UsersGroupsRefChildValueListener);
        m_UsersGroupsRef.addChildEventListener(m_UsersGroupsRefChildValueListener);
    }

    private void initUserGroupsIdListener() {

        m_UsersGroupsRefChildValueListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshotGroupId, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String groupId = dataSnapshot.getKey();
                changeMarkerColor(m_LoginManager.getLoggedInUser().getValue().isUserInGroup(groupId), groupId, Utils.isGroupInMyLocation((Group) markers.get(groupId).getTag()));
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshotGroupId) {
                m_GroupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {

                        String groupId = dataSnapshotGroupId.getKey();

                        if (dataSnapshot.child(groupId).child("usersId").getChildrenCount() == 1) // only current user was in group
                        {
                            m_GroupsRef.child(groupId).removeValue();

                        } else {
                            m_GroupsRef.child(groupId).child("usersId").child(LoginManager.getInstance().getLoggedInUser().getValue().getId()).removeValue();

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                m_LoginManager.getLocationManager().createLocationManagerAndListener(); //App can use location!
                m_LoginManager.getLocationManager().getCurrentLocation();
            } else {
                //Can't use the app message.
                //For Using the app you need to go to setting and enable location permissions to the app.
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}