package com.example.groupchatapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.ArrayMap;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.groupchatapp.Adapters.MapWrapperAdapter;
import com.example.groupchatapp.FirebaseListenerService;
import com.example.groupchatapp.LoginManager;
import com.example.groupchatapp.Models.Group;
import com.example.groupchatapp.Models.IDisplayable;
import com.example.groupchatapp.OnInfoWindowElemTouchListener;
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
import com.squareup.picasso.Picasso;

import java.util.Map;

import android.content.Context;
import android.view.ViewGroup;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;


public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ImageButton m_settingsButton, m_myGroupsButton, m_addGroupsButton, m_locationButton;
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

    //info window
    private ViewGroup m_infoWindow;
    private TextView m_infoTitle;
    private TextView m_infoSnippet;
    private TextView m_participantsNumber;
    private Button m_joinGroupButton,m_exitGroupButton,m_chatButton;
    private OnInfoWindowElemTouchListener m_infoButtonListener;
    private CircleImageView m_groupImage;
    private ImageView m_descriptionImage, m_participantsNumberImage;

    private MapWrapperAdapter m_mapWrapperAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        m_mapWrapperAdapter = findViewById(R.id.map_relative_layout);
        mapFragment.getMapAsync(this);

        m_LoginManager = LoginManager.getInstance();


        initLoggedInListener();
        m_LoginManager.Login(m_OnLoggedInListener);

        initLocationInitListener();
        initLocationLimitChange();
        initOnLocationPermissionChange();

        m_LoginManager.getLocationManager().setOnLocationLimitChange(m_OnLocationLimitChange, 50);

        initializeFields();
        setOnClickButtons();

    }

    private void initializeFields() {

        m_settingsButton = findViewById(R.id.settings_button);
        m_myGroupsButton = findViewById(R.id.my_groups_button);
        m_addGroupsButton = findViewById(R.id.add_group_button);
        m_locationButton = findViewById(R.id.location_button);
        // We want to reuse the info window for all the markers,
        // so let's create only one class member instance
        m_infoWindow = (ViewGroup)getLayoutInflater().inflate(R.layout.custom_infowindow, null);

        m_infoTitle = m_infoWindow.findViewById(R.id.group_name_IW);
        m_infoSnippet = m_infoWindow.findViewById(R.id.group_description_IW);
        m_participantsNumber = m_infoWindow.findViewById(R.id.participants_number_IW);
        m_groupImage = m_infoWindow.findViewById(R.id.image_of_group);
        m_participantsNumberImage = m_infoWindow.findViewById(R.id.participants_number_IW_image);
        m_descriptionImage = m_infoWindow.findViewById(R.id.group_description_IW_image);

        m_joinGroupButton = m_infoWindow.findViewById(R.id.join_group_button_IW);
        m_exitGroupButton = m_infoWindow.findViewById(R.id.exit_group_button_IW);
        m_chatButton = m_infoWindow.findViewById(R.id.chat_IW);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        // MapWrapperLayout initialization
        // 39 - default marker height
        // 20 - offset between the default InfoWindow bottom edge and it's content bottom edge
        m_mapWrapperAdapter.init(mMap, getPixelsFromDp(this, 39 + 20));


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

                return false;
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                restartMap();
            }
        });

        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                m_infoTitle.setText(marker.getTitle());
                m_infoButtonListener.setMarker(marker);

                decideWhatToShowInInfoWindow(marker);

                return m_infoWindow;
            }
        });
    }

    private void decideWhatToShowInInfoWindow(Marker currentGroupMarker) {

        int whatToShow = View.VISIBLE;
        Group groupToDisplay = ((Group) currentGroupMarker.getTag());
        if (!Utils.isGroupInMyLocation(groupToDisplay)) {
            whatToShow = View.GONE;
            m_groupImage.setImageResource(R.drawable.appicon);
            hideJoinAndExitGroupButtons();
        }
        else {
            if (groupToDisplay.getPhotoUrl() == null) {
                m_groupImage.setImageResource(R.drawable.groupicon);
            } else {
                Picasso.get().load(groupToDisplay.getPhotoUrl()).into(m_groupImage, new com.squareup.picasso.Callback() {

                    @Override
                    public void onSuccess() {
                        m_mapWrapperAdapter.refreshInfoWindo(currentGroupMarker);
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });

            }
                m_infoSnippet.setText(currentGroupMarker.getSnippet());
                m_participantsNumber.setText(String.valueOf(((Group) currentGroupMarker.getTag()).getUsersId().size()));

                if (!m_LoginManager.getLoggedInUser().getValue().isUserInGroup(((Group) currentGroupMarker.getTag()).getId())) {
                    showJoinGroupButton();
                } else {
                    showExitGroupButton();
                }
        }


         m_descriptionImage.setVisibility(whatToShow);
         m_infoSnippet.setVisibility(whatToShow);
         m_participantsNumberImage.setVisibility(whatToShow);
         m_participantsNumber.setVisibility(whatToShow);

         m_mapWrapperAdapter.setMarkerWithInfoWindow(currentGroupMarker, m_infoWindow);

    }


    private void restartMap() {
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
            Marker currentGroupMarker = markers.get(currentGroup.getId());
            m_mapWrapperAdapter.refreshInfoWindo(currentGroupMarker);
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
            public void onChange()
            {
                if(!m_LoginManager.getLocationManager().isLocationOn())
                {
                    mMap.setMyLocationEnabled(false);

                }else {
                    mMap.setMyLocationEnabled(true);
                }

                m_OnLocationLimitChange.onLimitChange();
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

                createMarkerForGroup(groupToAdd, isInGroup, isGroupInMyLocation);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                Group changedGroup = dataSnapshot.getValue(Group.class);

                Marker changedGroupMarker = markers.get(changedGroup.getId());
                changedGroupMarker.setTag(changedGroup);
                m_mapWrapperAdapter.refreshInfoWindo(changedGroupMarker);


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

    private void createMarkerForGroup(Group groupToAdd, boolean isInGroup, boolean isGroupInMyLocation) {

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
                if (m_LoginManager.getLocationManager().getCountryCode() != null) {
                    SendUserToMyGroupsActivity();
                } else {
                    Toast.makeText(MapsActivity.this, "Turn on location!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        m_locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!m_LoginManager.getLocationManager().isLocationOn())
                {
                    m_LoginManager.getLocationManager().EnableLocationIfNeeded();

                }else{
                    float zoom = mMap.getCameraPosition().zoom >= 15.0f ? mMap.getCameraPosition().zoom : 15.0f;
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(m_LoginManager.getLocationManager().GetLocationInLatLang(), zoom));
                }
            }
        });

        m_addGroupsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendUserToCreateGroupActivity();
            }
        });


        // Setting custom OnTouchListener which deals with the pressed state
        // so it shows up
        m_infoButtonListener = new OnInfoWindowElemTouchListener(m_joinGroupButton) {
            @Override

            protected void onClickConfirmed(View v, Marker marker) {


                if (currentGroup.isPrivateGroup()) {

                    final EditText input = new EditText(MapsActivity.this);
                    input.setBackgroundResource(R.drawable.rounded_layout_gray);
                    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); // the inputs look like dot and not the text.
                    final AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this, R.style.MyDialogTheme)
                            .setView(input)
                            .setTitle("Password required")
                            .setMessage("Please enter the password of " + currentGroup.getName() + " :")
                            .setPositiveButton("Join", null)
                            .setNegativeButton("Cancel", null)
                            .create();

                    alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                        @Override
                        public void onShow(DialogInterface dialog) {

                            Button b = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                            b.setOnClickListener(new View.OnClickListener() {

                                @Override
                                public void onClick(View view) {
                                    if (input.getText().toString().equals(currentGroup.getPassword())) {
                                        LoginManager.getInstance().addNewGroupIdToCurrentUser(currentGroup.getId());
                                        sendUserToChatActivity();
                                        alertDialog.hide();

                                    } else {
                                        alertDialog.setMessage("Wrong password, please try again...");
                                        input.setBackgroundResource(R.drawable.rounded_layout_red);
                                        b.setText("retry");
                                    }

                                }
                            });
                        }
                    });
                    alertDialog.show();
                } else {
                    LoginManager.getInstance().addNewGroupIdToCurrentUser(currentGroup.getId());
                    sendUserToChatActivity();

                }
            }

        };

        m_joinGroupButton.setOnTouchListener(m_infoButtonListener);

        m_infoButtonListener = new OnInfoWindowElemTouchListener(m_exitGroupButton){
            @Override
            protected void onClickConfirmed(View v, Marker marker) {

                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    LoginManager.getInstance().exitFromGroup(currentGroup.getId());
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
                            .setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener)
                            .create();
                dialogAlert.show();

                }
            };



        m_exitGroupButton.setOnTouchListener(m_infoButtonListener);

        m_infoButtonListener = new OnInfoWindowElemTouchListener(m_chatButton){
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
               sendUserToChatActivity();
            }
        };

        m_chatButton.setOnTouchListener(m_infoButtonListener);

    }

    private void sendUserToChatActivity()
    {
        Intent chatIntent = new Intent(MapsActivity.this, ChatActivity.class);
        chatIntent.putExtra("group",currentGroup);
        MapsActivity.this.startActivity(chatIntent);
    }
    private void showExitGroupButton() {
        m_joinGroupButton.setVisibility(View.GONE);
        m_exitGroupButton.setVisibility(View.VISIBLE);
        m_chatButton.setVisibility(View.VISIBLE);
    }
    private void showJoinGroupButton() {
        m_joinGroupButton.setVisibility(View.VISIBLE);
        m_exitGroupButton.setVisibility(View.GONE);
        m_chatButton.setVisibility(View.GONE);
    }
    private void hideJoinAndExitGroupButtons(){
        m_joinGroupButton.setVisibility(View.GONE);
        m_exitGroupButton.setVisibility(View.GONE);
        m_chatButton.setVisibility(View.GONE);
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

                    } else {
                        changeMarkerColor(isGroupInMyGroups, group.getId(), isGroupInMyLocation);

                    }

                    if (currentGroup != null) {
                        Marker currentGroupMarker = markers.get(currentGroup.getId());
                        m_mapWrapperAdapter.refreshInfoWindo(currentGroupMarker);
                    }
                }
            }
        };
    }

    private void initLocationInitListener() {

        m_OnLocationInit=new OnLocationInit() {
            @Override
            public void onSuccess() {
                OnLocationProvide();
                if(m_LoginManager.getLocationManager().isLocationOn()) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(m_LoginManager.getLocationManager().GetLocationInLatLang(),15.0f));
                }
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

            public void onChildChanged(DataSnapshot dataSnapshotGroupId, String s) {
                String groupId = dataSnapshotGroupId.getKey();

                changeMarkerColor(m_LoginManager.getLoggedInUser().getValue().isUserInGroup(groupId), groupId, Utils.isGroupInMyLocation((Group) markers.get(groupId).getTag()));
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshotGroupId) {
                m_GroupsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {

                        String groupId = dataSnapshotGroupId.getKey();

                        if (dataSnapshot.child(groupId).child("historyUsersId").getChildrenCount() == 0 && dataSnapshot.child(groupId).child("usersId").getChildrenCount() ==0 ) {
                            m_GroupsRef.child(groupId).removeValue();
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

    public static int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int)(dp * scale + 0.5f);
    }

}