package com.example.groupchatapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;


    private FirebaseAuth mAuth;

    private DatabaseReference UsersRef;

    private double m_latitude,m_longitude;

    private LoggedInUser m_LoggedInUser;

    private User m_CurrentUser;


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
        m_LoggedInUser=LoggedInUser.getInstance();

        UsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                m_CurrentUser=dataSnapshot.child(mAuth.getCurrentUser().getUid()).getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    @Override
    protected  void onStart()
    {
        super.onStart();

      if(m_CurrentUser.getUid() == null)
      {
        SendUserToLoginActivity();
      }
      else if(m_CurrentUser.getName()==null)
      {
          SendUserToSettingsActivity();
      }
  //    else if(m_LoggedInUser.getCurrentUser().getName()==null)
  //    {
  //        SendUserToSettingsActivity();
  //    }

   //   UsersRef.addListenerForSingleValueEvent(new ValueEventListener() {
   //       @Override
   //       public void onDataChange(DataSnapshot dataSnapshot) {
   //           m_CurrentUser=dataSnapshot.child(currentUser.getUid()).getValue(User.class); //לבדוק מה מתקבל
   //       }
//
   //       @Override
   //       public void onCancelled(DatabaseError databaseError) {
//
   //       }
   //   });


        //להכניס לפונקציה - רון
       if (ContextCompat.checkSelfPermission(
               getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION
       ) != PackageManager.PERMISSION_GRANTED) {
           ActivityCompat.requestPermissions(
                   MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
       } else {
           getCurrentLocation();
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
             mAuth.signOut();
             SendUserToLoginActivity();
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
                getCurrentLocation();
            }
            else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
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

}

