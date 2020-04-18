package com.example.groupchatapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import static android.content.Context.LOCATION_SERVICE;


public class LocationManager {

    private LocationListener m_LocationListener;
    private android.location.LocationManager m_LocationManager;
    private Geocoder m_Geocoder;
    private OnLocationInit m_OnLocationInit;
    private OnLocationLimitChange m_OnLocationLimitChange;
    private String m_CountryCode;
    private double m_Latitude;
    private double m_Longitude;
    private Context m_Context;
    private int m_LimitOfMeters;

    public void Logout() {
        this.m_CountryCode = null;
    }

    public LocationManager() {
        m_CountryCode = null;
        m_Latitude = 0;
        m_Longitude = 0;
    }

    public boolean isLocationOn() {
        return m_Latitude != 0 && m_Longitude != 0;
    }

    public String getCountryCode() { return m_CountryCode; }

    public double getLatitude() { return m_Latitude; }
    public double getLongitude() { return m_Longitude; }

    public void createLocationManagerAndListener() {
        m_LocationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                //ממליץ לשים את זה בתוך פונקציה, מכיוון שהאתחול הזה קורה כמה פעמים במחלקה

                float [] distance = new float[1];
                Location.distanceBetween(m_Latitude, m_Longitude,
                        location.getLatitude(), location.getLongitude(), distance);

                m_Latitude = location.getLatitude();
                m_Longitude = location.getLongitude();

                if (distance[0] > 5) {
                    m_OnLocationLimitChange.onLimitChange();
                }

                Toast.makeText(m_Context, "Location Changed!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {
                getCurrentLocation();
                m_OnLocationLimitChange.onLimitChange();

                Toast.makeText(m_Context, "Searching for your location...", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onProviderDisabled(String provider) {
                m_Latitude = 0;
                m_Longitude = 0;
                Toast.makeText(m_Context, "Your location is off", Toast.LENGTH_SHORT).show();
            }
        };

        m_LocationManager = (android.location.LocationManager) m_Context.getSystemService(LOCATION_SERVICE);

        try {
            m_LocationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 1000, 10, m_LocationListener);
        } catch (SecurityException e) { }
    }

    private void getFromLocationGeocoder() {
        if (m_CountryCode == null) {
            try {
                List<Address> lstAdd = m_Geocoder.getFromLocation(m_Latitude, m_Longitude, 1);
                if (lstAdd.size() > 0) {
                    String countryCode = lstAdd.get(0).getCountryCode();
                    m_CountryCode = countryCode;
                    m_OnLocationInit.onSuccess();
                }
            } catch (Exception ex) {
                m_OnLocationInit.onFailure();
            }
        }
    }

    public void CheckPermissionLocation(Context context , OnLocationInit lictener) {
        m_Context = context;
        m_Geocoder = new Geocoder(m_Context);
        m_OnLocationInit=lictener;

        if (ContextCompat.checkSelfPermission(
                m_Context, Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) m_Context, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            createLocationManagerAndListener(); //App can use location!
            getCurrentLocation();
        }
    }

    public void getCurrentLocation() {
        EnableLocationIfNeeded();
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationServices.getFusedLocationProviderClient(m_Context)
                .requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        LocationServices.getFusedLocationProviderClient(m_Context)
                                .removeLocationUpdates(this);
                        if (locationResult != null && locationResult.getLocations().size() > 0) {
                            int latestLocationIndex = locationResult.getLocations().size() - 1;
                            m_Latitude =
                                    locationResult.getLocations().get(latestLocationIndex).getLatitude();
                            m_Longitude =
                                    locationResult.getLocations().get(latestLocationIndex).getLongitude();
                            getFromLocationGeocoder();
                        }
                    }
                }, Looper.getMainLooper());
    }

    private void EnableLocationIfNeeded() {

        String provider = Settings.Secure.getString(m_Context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
        if (!provider.contains("gps")) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(m_Context, R.style.MyDialogTheme);
            builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                    .setCancelable(false)
                    .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog, final int id) {
                            m_Context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
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

    public void setOnLocationLimitChange(OnLocationLimitChange listener , int limitOfMeters)
    {
        m_OnLocationLimitChange=listener;
        m_LimitOfMeters=limitOfMeters;
    }

}
