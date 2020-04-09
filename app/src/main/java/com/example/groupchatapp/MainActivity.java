package com.example.groupchatapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;







public class MainActivity extends AppCompatActivity
{
    private Toolbar mToolbar;

    private RecyclerView m_GroupList;
    private DatabaseReference m_GroupsRef;
    private AllGroupsAdapter m_GroupsAdapter;
    private final ArrayList<Group> groupsToDisplay = new ArrayList<>();


    private FirebaseAuth mAuth;

    private DatabaseReference UsersRef;

    private double m_latitude,m_longitude;

    private LoginManager m_LoginManager;

    private LocationListener m_LocationListener;
    private LocationManager m_LocationManager;
    private Geocoder m_Geocoder;

    private androidx.lifecycle.Observer<User> m_CurrentUserObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        m_Geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth=FirebaseAuth.getInstance();

        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("GroupChat");


        m_GroupList =findViewById(R.id.chats_list);
        m_GroupsAdapter = new AllGroupsAdapter(groupsToDisplay, this);
        m_GroupList.setLayoutManager(new LinearLayoutManager(this));
        m_GroupsRef = FirebaseDatabase.getInstance().getReference().child("Groups");

        m_GroupList.setAdapter(m_GroupsAdapter);

        UsersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        m_LoginManager = LoginManager.getInstance();

        if(!m_LoginManager.IsLoggedIn())
        {
            m_LoginManager.Login();
        }

        final androidx.lifecycle.Observer<User> currentUserObserver = new Observer<User>() {
            @Override
            public void onChanged(User currentUser) {

                if(currentUser == null)
                {
                    SendUserToLoginActivity();
                }
                else if(currentUser.getName()==null)
                {
                    SendUserToSettingsActivity();
                }

            }

        };

        m_LoginManager.getLoggedInUser().observe(this, currentUserObserver);

        CheckPermissionLocation();

        m_CurrentUserObserver =
                currentUser -> m_GroupsRef.addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        if (currentUser != null)
                        {
                            Group groupToAdd = dataSnapshot.getValue(Group.class);
                            if (!currentUser.getGroupsId().contains(groupToAdd.getGid()))
                                groupsToDisplay.add(dataSnapshot.getValue(Group.class));
                            m_GroupsAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                        Group groupToChange = dataSnapshot.getValue(Group.class);
                        if (currentUser != null)
                        {
                            int indexToChange = findIndexOfGroup(groupToChange);
                            if (indexToChange != -1) {
                                groupsToDisplay.set(indexToChange, groupToChange);
                                m_GroupsAdapter.notifyDataSetChanged();
                            }
                            //צריך לבדוק אם השינוי היה ביציאת המשתמש
                            //ואם כן אז צריך להוסיף את הקבוצה הזאת לגרופס טו דיספליי
                        }
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        Group groupToRemove = dataSnapshot.getValue(Group.class);
                        if (currentUser != null)
                        {
                            int indexToRemove = findIndexOfGroup(groupToRemove);
                            if (indexToRemove != -1){
                                groupsToDisplay.remove(indexToRemove);
                                m_GroupsAdapter.notifyDataSetChanged();
                            }
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

    private int findIndexOfGroup(Group group)
    {
        int i;
        for (i = 0; i < groupsToDisplay.size(); i++) {
            if (groupsToDisplay.get(i).getGid() == group.getGid()) {
                break;
            }
        }

        if (i == groupsToDisplay.size()) {
            i = -1;
        }

        return i;
    }

    private void createLocationManagerAndListener() {
        m_LocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                m_latitude = location.getLatitude();
                m_longitude = location.getLongitude();
                getFromLocationGeocoder();
                Toast.makeText( MainActivity.this,"Location Changed!",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
                getCurrentLocation();
                Toast.makeText(MainActivity.this,"Provider Enabled!",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProviderDisabled(String provider) {
                m_latitude = 0;
                m_longitude = 0;
                EnableLocationIfNeeded();
                Toast.makeText(MainActivity.this,"Provider Disabled!",Toast.LENGTH_SHORT).show();
            }
        };

        m_LocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        try {
            m_LocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 1, m_LocationListener);
        }
        catch (SecurityException e){

        }
    }

    private void getFromLocationGeocoder() {
        try {
            List<Address> lstAdd = m_Geocoder.getFromLocation(m_latitude, m_longitude, 1);
            if (lstAdd.size() > 0)
            {
                String countryName = lstAdd.get(0).getCountryName();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected  void onStart()
    {
        super.onStart();

        LoginManager.getInstance().getLoggedInUser().observe(this, m_CurrentUserObserver);
        //אם משנים דאטה בייס בקבוצות צריך להוסיף עוד קינון
    }


    private void EnableLocationIfNeeded() {

        String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if(!provider.contains("gps")) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.MyDialogTheme );
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

    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void SendUserToSettingsActivity()
    {
        Intent settingsIntent = new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(settingsIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(item.getItemId()==R.id.main_logout_option)
        {
            m_LoginManager.Logout();
        }

        if(item.getItemId()==R.id.main_settings_option)
        {
            SendUserToSettingsActivity();
        }

        if(item.getItemId()==R.id.main_find_friends_option)
        {
            SendUserToFindFriendsActivity();
        }
        if(item.getItemId()==R.id.main_Create_Group_option)
        {
            SendUserToCreateGroupActivity();
        }
        if(item.getItemId()==R.id.main_my_groups_option)
        {
            SendUserToMyGroupsActivity();
        }

        return true;
    }

    private void SendUserToMyGroupsActivity() {
        Intent myGroupsIntent = new Intent(MainActivity.this,MyGroupsActivity.class);
        startActivity(myGroupsIntent);
    }

    private void SendUserToCreateGroupActivity() {
        Intent createGroupIntent = new Intent(MainActivity.this,CreateGroupActivity.class);
        createGroupIntent.putExtra("longitude",m_longitude);
        createGroupIntent.putExtra("latitude",m_latitude);
        startActivity(createGroupIntent);
    }

    private void SendUserToFindFriendsActivity() {

        Intent findFriendsIntent = new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                createLocationManagerAndListener(); //App can use location!
                getCurrentLocation();
            }
            else {
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
                    MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }
        else {
            createLocationManagerAndListener(); //App can use location!
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                .removeLocationUpdates(this);
                        if (locationResult != null && locationResult.getLocations().size() > 0) {
                            int latestLocationIndex = locationResult.getLocations().size() - 1;
                            m_latitude =
                                    locationResult.getLocations().get(latestLocationIndex).getLatitude();
                            m_longitude =
                                    locationResult.getLocations().get(latestLocationIndex).getLongitude();
                            getFromLocationGeocoder();
                        }
                    }
                }, Looper.getMainLooper());
    }


    //   @Override
    //   protected void onStop() {
    //       super.onStop();
    //       UsersRef.child(m_LoginManager.getLoggedInUser().getValue().getUid()).child("groupsId").setValue(m_LoginManager.getLoggedInUser().getValue().getGroupsId());
    //   }
}