package com.example.groupchatapp.Activities;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<Group> groups;
    private ImageButton m_settingsButton, m_myGroupsButton, m_addGroupsButton;
    private double m_latitude, m_longitude;
    private LatLng m_currentLocation;
    private DatabaseReference m_GroupsRef;
    private AllGroupsAdapter m_GroupsAdapter;
    private final ArrayList<Group> groupsToDisplay = new ArrayList<>();
    private LoginManager m_LoginManager;
    private LocationListener m_LocationListener;
    private LocationManager m_LocationManager;
    private OnLoggedIn m_OnLoggedInListener;
    private Geocoder m_Geocoder;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        m_Geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        m_GroupsAdapter = new AllGroupsAdapter(groupsToDisplay, this);

        m_LoginManager = LoginManager.getInstance();

        if (!m_LoginManager.IsLoggedIn()) {
            initLoggedInListener();
            m_LoginManager.Login(m_OnLoggedInListener);
        }


        groups = (ArrayList<Group>) getIntent().getSerializableExtra("groups");
        m_latitude = 31.8784;
        m_longitude = 35.0078;
        m_currentLocation = new LatLng(m_latitude, m_longitude);
        bindButtons();

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(m_currentLocation, 15.0f));

    }



    private void OnGroupRefProvide() {

        //groupsToDisplay.clear();
        m_GroupsAdapter.notifyDataSetChanged();

        String countryCode = m_LoginManager.getLoggedInUser().getValue().getCountryCode();
        m_GroupsRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(countryCode);
        m_GroupsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                Group groupToAdd = dataSnapshot.getValue(Group.class);
                LatLng group = new LatLng(Double.valueOf(groupToAdd.getLatitude()), Double.valueOf(groupToAdd.getLongitude()));
                Marker m =  mMap.addMarker(new MarkerOptions().position(group).title(groupToAdd.getName()).snippet(groupToAdd.getPhotoUrl()));

                if (!m_LoginManager.getLoggedInUser().getValue().getGroupsId().containsKey(groupToAdd.getGid())) {
                    groupsToDisplay.add(dataSnapshot.getValue(Group.class));
                    m_GroupsAdapter.notifyDataSetChanged();
                }
            }


            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                Group changedGroup = dataSnapshot.getValue(Group.class);

                int indexToChange = Utils.findIndexOfGroup(groupsToDisplay, changedGroup);

                if (!m_LoginManager.getLoggedInUser().getValue().getGroupsId().containsKey(changedGroup.getGid())) {


                    if (indexToChange == -1) {

                        groupsToDisplay.add(changedGroup);
                    } else {
                        groupsToDisplay.set(indexToChange, changedGroup);

                    }

                } else {
                    if (indexToChange != -1) {
                        groupsToDisplay.remove(indexToChange);
                    }
                }

                m_GroupsAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Group groupToRemove = dataSnapshot.getValue(Group.class);

                int indexToRemove = Utils.findIndexOfGroup(groupsToDisplay, groupToRemove);
                if (indexToRemove != -1) {
                    groupsToDisplay.remove(indexToRemove);
                    m_GroupsAdapter.notifyDataSetChanged();
                }
            }


            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
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
        createGroupIntent.putExtra("longitude", m_longitude);
        createGroupIntent.putExtra("latitude", m_latitude);
        startActivity(createGroupIntent);
    }

    private void initLoggedInListener() {
        m_OnLoggedInListener = new OnLoggedIn() {
            @Override
            public void onSuccess() {

                //new Thread(()->CheckPermissionLocation()).start();
                CheckPermissionLocation();
            }

            @Override
            public void onStart() {

            }

            @Override
            public void onFailure() {

            }

        };
    }

    private void createLocationManagerAndListener() {
        m_LocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                m_LoginManager.getLoggedInUser().getValue().setLatitude(location.getLatitude());
                m_LoginManager.getLoggedInUser().getValue().setLongitude(location.getLongitude());
                getFromLocationGeocoder();
                Toast.makeText(MapsActivity.this, "Location Changed!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
                getCurrentLocation();
                Toast.makeText(MapsActivity.this, "Searching your location", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onProviderDisabled(String provider) {
                m_LoginManager.getLoggedInUser().getValue().setLatitude(0);
                m_LoginManager.getLoggedInUser().getValue().setLongitude(0);
                m_LoginManager.getLoggedInUser().getValue().setCountryCode(null);
                Toast.makeText(MapsActivity.this, "Turn on location!", Toast.LENGTH_SHORT).show();
            }
        };

        m_LocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        try {
            m_LocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 1, m_LocationListener);
        } catch (SecurityException e) {

        }
    }

    private void getFromLocationGeocoder() {
        if (m_LoginManager.getLoggedInUser().getValue().getCountryCode() == null) {
            try {
                List<Address> lstAdd = m_Geocoder.getFromLocation(
                        m_LoginManager.getLoggedInUser().getValue().getLatitude(),
                        m_LoginManager.getLoggedInUser().getValue().getLongitude(), 1);
                if (lstAdd.size() > 0) {
                    String countryCode = lstAdd.get(0).getCountryCode();
                    m_LoginManager.getLoggedInUser().getValue().setCountryCode(countryCode);
                    OnGroupRefProvide();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void EnableLocationIfNeeded() {

        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (!provider.contains("gps")) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this, R.style.MyDialogTheme);
            builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                    .setCancelable(false)
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.setTitle("GROUPI");
            alert.show();
        }
    }

    private void SendUserToLoginActivity() {
        Intent loginIntent = new Intent(MapsActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createLocationManagerAndListener(); //App can use location!
                getCurrentLocation();
            } else {
                //Can't use the app message.
                //For Using the app you need to go to setting and enable location permissions to the app.
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void CheckPermissionLocation() {
        if (ContextCompat.checkSelfPermission(
                getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MapsActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            createLocationManagerAndListener(); //App can use location!
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        EnableLocationIfNeeded();
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.getFusedLocationProviderClient(MapsActivity.this)
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(MapsActivity.this)
                                .removeLocationUpdates(this);
                        if (locationResult != null && locationResult.getLocations().size() > 0) {
                            int latestLocationIndex = locationResult.getLocations().size() - 1;
                            m_LoginManager.getLoggedInUser().getValue().setLatitude(
                                    locationResult.getLocations().get(latestLocationIndex).getLatitude());
                            m_LoginManager.getLoggedInUser().getValue().setLongitude(
                                    locationResult.getLocations().get(latestLocationIndex).getLongitude());
                            getFromLocationGeocoder();
                        }
                    }
                }, Looper.getMainLooper());
    }


}