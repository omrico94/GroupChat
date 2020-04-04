package com.example.groupchatapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MainActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;


    private FirebaseAuth mAuth;

    private DatabaseReference UsersRef;

    private double m_latitude,m_longitude;

    private LoginManager m_LoginManager;

    LocationListener m_LocationListener;
    LocationManager m_LocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth=FirebaseAuth.getInstance();

        mToolbar = findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("GroupChat");


        myViewPager = findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdapter = new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);


        myTabLayout = findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);


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

        //createLocationManagerAndListener();

        //   m_LoginManager.getLoggedInUser().observe(this, Observer<User>
    //           {currentUser ->
    //   if (m_CurrentUser.getUid() == null) {
    //       SendUserToLoginActivity();
    //   } else if (m_CurrentUser.getName() == null) {
    //       SendUserToSettingsActivity();
    //   }
    //   });


      //  UsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
      //      @Override
      //      public void onDataChange(DataSnapshot dataSnapshot) {
      //          m_CurrentUser=dataSnapshot.child(mAuth.getLoggedInUser().getUid()).getValue(User.class);
      //      }
//
      //      @Override
      //      public void onCancelled(DatabaseError databaseError) {
//
      //      }
      //  });
    }

    private void createLocationManagerAndListener() {
        m_LocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                    m_latitude = location.getLatitude();
                    m_longitude = location.getLongitude();
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
                EnableLocationIfNeeded();
            }
        };

        m_LocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        try {
            m_LocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 200, 1, m_LocationListener);
        }
        catch (SecurityException e){

        }
    }

    @Override
    protected  void onStart()
    {
        super.onStart();
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

         return true;
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

