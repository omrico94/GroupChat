package com.example.groupchatapp.Activities;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ImageButton m_settingsButton, m_myGroupsButton, m_addGroupsButton,m_joinGroup,m_exitGroup;
    private LatLng m_currentLocation;
    private DatabaseReference m_GroupsRef;
    private AllGroupsAdapter m_GroupsAdapter;
    private final ArrayList<Group> groupsToDisplay = new ArrayList<>();
    private LoginManager m_LoginManager;
    private OnLoggedIn m_OnLoggedInListener;
    private Geocoder m_Geocoder;
    private ArrayMap<String,Marker> markers = new ArrayMap<String, Marker>();
    private ArrayMap<String,Group> groupsID = new ArrayMap<String,Group>();
    private Group currentGroup;
    Circle m_radiusCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        m_Geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //m_GroupsAdapter = new AllGroupsAdapter(groupsToDisplay, this);

        m_LoginManager = LoginManager.getInstance();

        if (!m_LoginManager.IsLoggedIn()) {
            initLoggedInListener();
            m_LoginManager.Login(m_OnLoggedInListener);
        }

        bindButtons();

        hideJoinAndExitGroupButtons();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setMyLocationEnabled(true);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(m_LoginManager.getLocationManager().GetLocationInLatLang(), 15.0f));

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                currentGroup = groupsID.get(marker.getId());
                if(m_radiusCircle!=null) {
                    m_radiusCircle.remove();
                }

                    m_radiusCircle = mMap.addCircle(new CircleOptions()
                        .center(marker.getPosition())
                        .radius(Double.parseDouble(currentGroup.getRadius()))
                        .strokeColor(Color.BLUE));

                if(!m_LoginManager.getLoggedInUser().getValue().getGroupsId().containsKey(marker.getTag()))
                {
                    m_joinGroup.setVisibility(View.VISIBLE);
                    m_exitGroup.setVisibility(View.GONE);
                }else
                {
                    m_joinGroup.setVisibility(View.GONE);
                    m_exitGroup.setVisibility(View.VISIBLE);
                }

                return false;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                hideJoinAndExitGroupButtons();
                m_radiusCircle.remove();
            }
        });

    }

    private void initLoggedInListener() {
        m_OnLoggedInListener = new OnLoggedIn() {
            @Override
            public void onSuccess() {

                //new Thread(()->CheckPermissionLocation()).start();
                m_LoginManager.getLocationManager().CheckPermissionLocation(MapsActivity.this);
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFailure() {

            }

        };
    }

    public void OnGroupRefProvide() {

        groupsToDisplay.clear();
        //m_GroupsAdapter.notifyDataSetChanged();

        String countryCode = m_LoginManager.getLocationManager().getCountryCode();
        m_GroupsRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(countryCode);

        m_GroupsRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Marker groupMarker;
                float BMColor = 0;

                Group groupToAdd = dataSnapshot.getValue(Group.class);
                LatLng groupLocation = new LatLng(Double.valueOf(groupToAdd.getLatitude()), Double.valueOf(groupToAdd.getLongitude()));

                if (!m_LoginManager.getLoggedInUser().getValue().getGroupsId().containsKey(groupToAdd.getGid())) {
                    groupsToDisplay.add(dataSnapshot.getValue(Group.class));
                    //m_GroupsAdapter.notifyDataSetChanged();
                }
                else
                {
                    BMColor = 150;
                }

                groupMarker =  mMap.addMarker(new MarkerOptions().position(groupLocation).icon(BitmapDescriptorFactory.defaultMarker(BMColor)).title(groupToAdd.getName()).snippet(groupToAdd.getDescription()));
                groupMarker.setTag(groupToAdd.getGid());
                markers.put(groupToAdd.getGid(),groupMarker);
                groupsID.put(groupMarker.getId(),groupToAdd);
            }


            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                Group changedGroup = dataSnapshot.getValue(Group.class);

                int indexToChange = Utils.findIndexOfGroup(groupsToDisplay, changedGroup);

                if (!m_LoginManager.getLoggedInUser().getValue().getGroupsId().containsKey(changedGroup.getGid())) {

                    if (indexToChange == -1) {
                        groupsToDisplay.add(changedGroup);
                        Marker markerToChange = markers.get(changedGroup.getGid());
                        markerToChange.setIcon(BitmapDescriptorFactory.defaultMarker());
                    } else {
                        groupsToDisplay.set(indexToChange, changedGroup);
                    }
                } else {
                    if (indexToChange != -1) {
                        groupsToDisplay.remove(indexToChange);
                        Marker markerToChange = markers.get(changedGroup.getGid());
                        markerToChange.setIcon(BitmapDescriptorFactory.defaultMarker(150f));
                    }
                }

               // m_GroupsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Group groupToRemove = dataSnapshot.getValue(Group.class);

                int indexToRemove = Utils.findIndexOfGroup(groupsToDisplay, groupToRemove);
                if (indexToRemove != -1) {
                    groupsToDisplay.remove(indexToRemove);
                    //m_GroupsAdapter.notifyDataSetChanged();
                }

                removeMarkerByGroup(groupToRemove.getGid());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }

    private void removeMarkerByGroup(String groupId)
    {
        Marker markerToRemove = markers.get(groupId);
        markerToRemove.remove();
    }

    private void bindButtons() {
        m_settingsButton = findViewById(R.id.settings_button);
        m_settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToSettingsActivity();
            }
        });
        m_myGroupsButton = findViewById(R.id.my_groups_button);
        m_myGroupsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToMyGroupsActivity();
            }
        });
        m_addGroupsButton = findViewById(R.id.add_group_button);
        m_addGroupsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToCreateGroupActivity();
            }
        });

        m_joinGroup = findViewById(R.id.join_group);
        m_joinGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showExitGroupButton();
                SendUserToJoinToGroupActivity();
            }
        });

        m_exitGroup = findViewById(R.id.exit_group);
        m_exitGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showJoinGroupButton();
            }
        });
    }

    private void showExitGroupButton() {
        m_joinGroup.setVisibility(View.GONE);
        m_exitGroup.setVisibility(View.VISIBLE);
    }
    private void showJoinGroupButton() {
        m_joinGroup.setVisibility(View.VISIBLE);
        m_exitGroup.setVisibility(View.GONE);
    }
    private void hideJoinAndExitGroupButtons(){
        m_joinGroup.setVisibility(View.GONE);
        m_exitGroup.setVisibility(View.GONE);
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