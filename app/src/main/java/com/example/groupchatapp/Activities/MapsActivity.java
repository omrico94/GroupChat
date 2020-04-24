package com.example.groupchatapp.Activities;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Picture;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.util.ArrayMap;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.collection.LLRBNode;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


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



    private ViewGroup infoWindow;
    private TextView infoTitle;
    private TextView infoSnippet;
    private Button infoButton1, infoButton2;
    private OnInfoWindowElemTouchListener infoButtonListener;

    private MapWrapperLayout m_mapWrapperLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        m_mapWrapperLayout = (MapWrapperLayout)findViewById(R.id.map_relative_layout);
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

        // MapWrapperLayout initialization
        // 39 - default marker height
        // 20 - offset between the default InfoWindow bottom edge and it's content bottom edge
        m_mapWrapperLayout.init(mMap, getPixelsFromDp(this, 39 + 20));

        // We want to reuse the info window for all the markers,
        // so let's create only one class member instance
        this.infoWindow = (ViewGroup)getLayoutInflater().inflate(R.layout.custom_infowindow, null);

        this.infoTitle = (TextView)infoWindow.findViewById(R.id.nameTxt);
        this.infoSnippet = (TextView)infoWindow.findViewById(R.id.addressTxt);
        this.infoButton1 = (Button)infoWindow.findViewById(R.id.btnOne);
        this.infoButton2 = (Button)infoWindow.findViewById(R.id.btnTwo);

        // Setting custom OnTouchListener which deals with the pressed state
        // so it shows up
        this.infoButtonListener = new OnInfoWindowElemTouchListener(infoButton1, getResources().getDrawable(R.drawable.joingroup), getResources().getDrawable(R.drawable.joingroup)){
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                // Here we can perform some action triggered after clicking the button
                Toast.makeText(MapsActivity.this, "click on button 1", Toast.LENGTH_SHORT).show();
            }
        };
        this.infoButton1.setOnTouchListener(infoButtonListener);

        infoButtonListener = new OnInfoWindowElemTouchListener(infoButton2, getResources().getDrawable(R.drawable.exitgroup),getResources().getDrawable(R.drawable.exitgroup)){
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                Toast.makeText(getApplicationContext(), "click on button 2", Toast.LENGTH_LONG).show();
            }
        };
        infoButton2.setOnTouchListener(infoButtonListener);

        /*infoWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "click on infowindow", Toast.LENGTH_LONG).show();
            }
        });*/

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Setting up the infoWindow with current's marker info
                infoSnippet.setText(marker.getTitle());
                infoTitle.setText(marker.getSnippet());
                infoButtonListener.setMarker(marker);


                // We must call this to set the current marker and infoWindow references
                // to the MapWrapperLayout
                m_mapWrapperLayout.setMarkerWithInfoWindow(marker, infoWindow);
                return infoWindow;
            }
        });

    }

    private void decideIfJoinOrExitButton(Marker marker) {
        if(!m_LoginManager.getLoggedInUser().getValue().getGroupsId().containsKey(((Group) marker.getTag()).getGid())
                && Utils.isGroupInMyLocation(currentGroup)) {
            showJoinGroupButton();
        }
        else if (m_LoginManager.getLoggedInUser().getValue().getGroupsId().containsKey(((Group) marker.getTag()).getGid())) {
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
        if (currentGroup != null && markers.containsKey(currentGroup.getGid())) {
            decideIfJoinOrExitButton(markers.get(currentGroup.getGid()));
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

    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dp * scale + 0.5f);
    }

    public void  initGroupsChildEventListener() {

        m_newGroupsRefChildValueListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Group groupToAdd = dataSnapshot.getValue(Group.class);
                boolean isInGroup = m_LoginManager.getLoggedInUser().getValue().getGroupsId().containsKey(groupToAdd.getGid());

                boolean isGroupInMyLocation = Utils.isGroupInMyLocation(groupToAdd);

                if (isInGroup && !isGroupInMyLocation) {
                    //The user is in the group but not in its location....
                    //Now we remove him from the group.
                    LoginManager.getInstance().removeGroupIdFromCurrentUser(groupToAdd.getGid());
                    isInGroup = false;
                }

                createMarkerforGroup(groupToAdd, isInGroup, isGroupInMyLocation);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                Group changedGroup = dataSnapshot.getValue(Group.class);
                boolean isInGroup = m_LoginManager.getLoggedInUser().getValue().getGroupsId().containsKey(changedGroup.getGid());
                boolean isGroupInMyLocation = Utils.isGroupInMyLocation(changedGroup);

                changeMarkerColor(isInGroup, changedGroup.getGid(), isGroupInMyLocation);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                Group groupToRemove = dataSnapshot.getValue(Group.class);
                removeMarkerByGroupID(groupToRemove.getGid());
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
        markers.put(groupToAdd.getGid(),groupMarker);

        changeMarkerColor(isInGroup,groupToAdd.getGid(),isGroupInMyLocation);
    }

    private void removeMarkerByGroupID(String groupId)
    {
        if (currentGroup != null && currentGroup.getGid().equals(groupId)) {
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
                                LoginManager.getInstance().removeGroupIdFromCurrentUser(currentGroup.getGid());
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
                        .setMessage("Remove " + currentGroup.getName() + " from MyGroups?")
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
        joinGroupIntent.putExtra("group_id", currentGroup.getGid());
        joinGroupIntent.putExtra("group_name",currentGroup.getName());
        joinGroupIntent.putExtra("group_image",currentGroup.getPhotoUrl());
        joinGroupIntent.putExtra("group_password", currentGroup.getPassword());
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
                    isGroupInMyGroups = m_LoginManager.getLoggedInUser().getValue().getGroupsId().containsKey(group.getGid());

                    if (isGroupInMyGroups && !isGroupInMyLocation) {
                        //The user is in the group but not in its location....
                        //Now we remove him from the group.
                        LoginManager.getInstance().removeGroupIdFromCurrentUser(group.getGid());
                        isGroupInMyGroups = false;
                    }

                    if (currentGroup != null) {
                        decideIfJoinOrExitButton(markers.get(group.getGid()));
                    }

                    changeMarkerColor(isGroupInMyGroups, group.getGid(), isGroupInMyLocation);
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


        m_UsersGroupsRef = FirebaseDatabase.getInstance().getReference().child("Users").child(LoginManager.getInstance().getLoggedInUser().getValue().getUid()).child("groupsId");

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
                            m_GroupsRef.child(groupId).child("usersId").child(LoginManager.getInstance().getLoggedInUser().getValue().getUid()).removeValue();

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