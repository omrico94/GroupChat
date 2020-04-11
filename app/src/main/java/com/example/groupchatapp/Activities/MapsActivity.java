package com.example.groupchatapp.Activities;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Picture;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.groupchatapp.Models.Group;
import com.example.groupchatapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<Group> groups;
    private ImageButton m_settingsButton,m_myGroupsButton,m_addGroupsButton;
    private double m_latitude,m_longitude;
    private LatLng m_currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        groups = (ArrayList<Group>) getIntent().getSerializableExtra("groups");
        m_latitude=(Double)getIntent().getExtras().get("latitude");
        m_longitude=(Double)getIntent().getExtras().get("longitude");
        m_currentLocation = new LatLng(m_latitude,m_longitude);
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
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(m_currentLocation,15.0f));
        if(groups.size()!=0)
        {
            addGroupsToMap();
        }
    }

    private void addGroupsToMap()
    {
        LatLng group;
        int  i = 0;

        group = new LatLng(Double.valueOf(groups.get(0).getLatitude()),Double.valueOf(groups.get(0).getLongitude()));
        mMap.addMarker(new MarkerOptions().position(group).title(groups.get(i).getName()));
        for(i=1; i<groups.size();i++)
        {
            group = new LatLng(Double.valueOf(groups.get(i).getLatitude()),Double.valueOf(groups.get(i).getLongitude()));
            mMap.addMarker(new MarkerOptions().position(group).title(groups.get(i).getName()).snippet(groups.get(i).getPhotoUrl()));
        }

       // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(group,15.0f));//מעביר את המפה לאיפה שעלתה הקבוצה האחרונה רק לבדיקות בפועל זה יראה את המיקום הנוכחי
    }


    private void bindButtons()
    {
        m_settingsButton =findViewById(R.id.settings_button);
        m_settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToSettingsActivity();
            }
        });
        m_myGroupsButton =findViewById(R.id.my_groups_button);
        m_myGroupsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToMyGroupsActivity();
            }
        });
        m_addGroupsButton =findViewById(R.id.add_group_button);
        m_addGroupsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToCreateGroupActivity();
            }
        });
    }

    private void SendUserToSettingsActivity()
    {
        Intent settingsIntent = new Intent(MapsActivity.this,SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void SendUserToMyGroupsActivity()
    {
        Intent myGroupsIntent = new Intent(MapsActivity.this,MyGroupsActivity.class);
        startActivity(myGroupsIntent);
    }

    private void SendUserToCreateGroupActivity()
    {
        Intent createGroupIntent = new Intent(MapsActivity.this,CreateGroupActivity.class);
        createGroupIntent.putExtra("longitude",m_longitude);
        createGroupIntent.putExtra("latitude",m_latitude);
        startActivity(createGroupIntent);
    }



}
