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
import android.location.LocationListener;
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

import com.example.groupchatapp.Adapters.AllGroupsAdapter;
import com.example.groupchatapp.LoginManager;
import com.example.groupchatapp.Models.Group;
import com.example.groupchatapp.OnLocationInit;
import com.example.groupchatapp.OnLocationLimitChange;
import com.example.groupchatapp.OnLogOut;
import com.example.groupchatapp.OnLoggedIn;
import com.example.groupchatapp.R;
import com.example.groupchatapp.Utils;
import com.google.android.gms.location.LocationCallback;
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

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ImageButton m_settingsButton, m_myGroupsButton, m_addGroupsButton,m_joinGroupButton,m_exitGroupButton;
    private DatabaseReference m_GroupsRef,m_UsersGroupsRef;
    private final ArrayList<Group> groupsToDisplay = new ArrayList<>();
    private LoginManager m_LoginManager;
    private OnLoggedIn m_OnLoggedInListener;
    private OnLocationInit m_OnLocationInit;
    private OnLocationLimitChange m_OnLocationLimitChange;
    private OnLogOut m_OnLogOutListener;

    private ArrayMap<String,Marker> markers = new ArrayMap<String, Marker>();

    private Group currentGroup;
    private  Circle m_radiusCircle;

    private HashMap<DatabaseReference, ValueEventListener> m_RemoveListenersMap;
    private ChildEventListener m_newGroupsRefChildValueListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        m_LoginManager = LoginManager.getInstance();

        m_RemoveListenersMap=new HashMap<>();

        if (!m_LoginManager.IsLoggedIn()) {

            //איפשהו בתוך התנאי כאן צריך להכניס את קריאת האתחול ללימיט ליסינר שנמצא במחלקה המיקום (כנראה לפני הלוגין אבל לא הייתי בטוח
            initLoggedInListener();
            initLocationInitListener();
            initLocationLimitChange();

            m_LoginManager.Login(m_OnLoggedInListener);
        }



        initLogOutListener();
        initializeFields();
        setOnClickButtons();

        hideJoinAndExitGroupButtons();//כל הפונקציות האלו לא עובדות נכון  אני בונה על זה שיהיה כפתורים במסך מידע

    }

    private void initLogOutListener() {


        m_OnLogOutListener = new OnLogOut() {
            @Override
            public void OnClickLogOut() {
                SendUserToLoginActivity();
            }
        };
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
        mMap.setMyLocationEnabled(true);


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

                if(!m_LoginManager.getLoggedInUser().getValue().getGroupsId().containsKey(((Group) marker.getTag()).getGid()))
                {
                    m_joinGroupButton.setVisibility(View.VISIBLE);
                    m_exitGroupButton.setVisibility(View.GONE);
                }else
                {
                    m_joinGroupButton.setVisibility(View.GONE);
                    m_exitGroupButton.setVisibility(View.VISIBLE);
                }

                return false;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                hideJoinAndExitGroupButtons();
                if(m_radiusCircle!=null) {
                    m_radiusCircle.remove();
                }
            }
        });

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


    public void  initGroupsChildEventListener() {

        m_newGroupsRefChildValueListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                boolean isInGroup = true;
                Group groupToAdd = dataSnapshot.getValue(Group.class);

                if (!m_LoginManager.getLoggedInUser().getValue().getGroupsId().containsKey(groupToAdd.getGid())) {

                    isInGroup = false;
                }

                createMarkerforGroup(groupToAdd, isInGroup);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                boolean isInGroup = true;
                Group changedGroup = dataSnapshot.getValue(Group.class);

                if (!m_LoginManager.getLoggedInUser().getValue().getGroupsId().containsKey(changedGroup.getGid())) {

                    isInGroup = false;
                }

                changeMarkerColor(isInGroup, changedGroup.getGid());
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


    private void changeMarkerColor(boolean isInGroup,String groupID) {

        float markerColor = 0;

        if(isInGroup)
        {
            markerColor = 150;
        }

        markers.get(groupID).setIcon(BitmapDescriptorFactory.defaultMarker(markerColor));
    }

    private void createMarkerforGroup(Group groupToAdd, boolean isInGroup) {

        Marker groupMarker;
        LatLng groupLocation = new LatLng(Double.valueOf(groupToAdd.getLatitude()), Double.valueOf(groupToAdd.getLongitude()));

        groupMarker =  mMap.addMarker(new MarkerOptions().position(groupLocation).title(groupToAdd.getName()).snippet(groupToAdd.getDescription()));
        groupMarker.setTag(groupToAdd);
        markers.put(groupToAdd.getGid(),groupMarker);

        changeMarkerColor(isInGroup,groupToAdd.getGid());
    }

    private void removeMarkerByGroupID(String groupId)
    {
        Marker markerToRemove = markers.get(groupId);
        markerToRemove.remove();
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
                SendUserToMyGroupsActivity();
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
                showExitGroupButton();
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

    private void SendUserToLoginActivity() {
             m_GroupsRef.removeEventListener(m_newGroupsRefChildValueListener);
             Intent loginIntent = new Intent(MapsActivity.this, LoginActivity.class);
             loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
             startActivity(loginIntent);
             finish();
         }

    private void initLocationLimitChange() {

        m_OnLocationLimitChange=new OnLocationLimitChange() {
            @Override
            public void onLimitChange() {
                //כאן צריך לשים את הפונקציה שאתה רוצה שתעבור על הקבוצות. שים לב שצריך לקרוא למטודת האתחול שנמצאת במחלקה של המיקום לפני
            }
        };
    }

    private void initLocationInitListener() {

        m_OnLocationInit=new OnLocationInit() {
            @Override
            public void onSuccess() {
                OnLocationProvide();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(m_LoginManager.getLocationManager().GetLocationInLatLang(), 15.0f));
            }

            @Override
            public void onFailure() {

            }
        };
    }

    public void OnLocationProvide() {

        //groupsToDisplay.clear();

        String countryCode = m_LoginManager.getLocationManager().getCountryCode();
        m_GroupsRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(countryCode);

        m_GroupsRef.addChildEventListener(m_newGroupsRefChildValueListener );
        m_LoginManager.InitLogOutListener(m_OnLogOutListener);

        m_UsersGroupsRef = FirebaseDatabase.getInstance().getReference().child("Users").child(LoginManager.getInstance().getLoggedInUser().getValue().getUid()).child("groupsId");

        m_UsersGroupsRef.addChildEventListener(new ChildEventListener() {
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
        });

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