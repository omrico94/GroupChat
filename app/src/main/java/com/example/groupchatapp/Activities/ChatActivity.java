package com.example.groupchatapp.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupchatapp.Adapters.MessageAdapter;
import com.example.groupchatapp.FirebaseListenerService;
import com.example.groupchatapp.LoginManager;
import com.example.groupchatapp.Models.Group;
import com.example.groupchatapp.Models.Message;
import com.example.groupchatapp.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private String groupId, groupName, groupImageStr, userSenderId, userSenderName;
    private TextView groupNameTextView;
    private CircleImageView groupCircleImageView;
    private Toolbar chatToolBar;
    private ImageButton sendMessageButton,sendFilesButton;
    private EditText messageInputText;
    private DatabaseReference rootRef ,m_ExitRef;

    private final List<Message> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;

    private String saveCurrentTime,saveCurrentDate,checker="" , myUrl="";
    private Uri fileUri;
    private StorageTask uploadTask;

    private ProgressDialog loadingBar;

    private LoginManager m_LoginManager;
    private String m_CountryCode;

    private ChildEventListener m_MessageEventListener, m_UserExitFromGroupEventListener;
    private int indexOfCurrentDate = 0;

    private Group m_CurrentGroup;
    private TextView m_CantSendMessageTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        m_LoginManager = LoginManager.getInstance();
        m_CountryCode = LoginManager.getInstance().getLocationManager().getCountryCode();

        userSenderId = m_LoginManager.getLoggedInUser().getValue().getId();
        userSenderName = m_LoginManager.getLoggedInUser().getValue().getName();
        rootRef= FirebaseDatabase.getInstance().getReference();

        m_ExitRef = FirebaseDatabase.getInstance().getReference();

        m_CurrentGroup =(Group) getIntent().getExtras().get("group");
        groupId =m_CurrentGroup.getId();
        groupName =m_CurrentGroup.getName();
        groupImageStr = m_CurrentGroup.getPhotoUrl() != null ? m_CurrentGroup.getPhotoUrl() : "default_image";


        Toast.makeText(ChatActivity.this, groupName,Toast.LENGTH_SHORT).show();

        initializeControllers();

        groupNameTextView.setText(groupName);
        Picasso.get().load(groupImageStr).placeholder(R.drawable.groupicon).into(groupCircleImageView);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendTextMessage();
            }
        });


        sendFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CharSequence options[]= new CharSequence[]
                        {
                                "Images",
                                "PDF Files",
                                "Ms Word Files"
                        };

                AlertDialog.Builder builder =  new AlertDialog.Builder(ChatActivity.this);
                builder .setTitle("Select the File");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        if(i==0)
                        {
                            checker = "image";

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent,"Select Image"),438);
                        }
                        if(i==1)
                        {
                            checker = "pdf";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/pdf");
                            startActivityForResult(intent.createChooser(intent,"Select PDF File"),438);

                        }
                        if(i==2)
                        {
                            checker = "docx";
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            intent.setType("application/msword");
                            startActivityForResult(intent.createChooser(intent,"Select Ms Word File"),438);

                        }
                    }
                });
                builder.show();
            }
        });

        initChildEventListeners();
        rootRef.child("Groups").child(m_CountryCode).child(groupId).child("Messages")
                .addChildEventListener(m_MessageEventListener);
        m_ExitRef.child("Users").child(m_LoginManager.getLoggedInUser().getValue().getId()).child("groupsId").child(groupId)
                .addChildEventListener(m_UserExitFromGroupEventListener);
    }

    private void initChildEventListeners() {
        m_MessageEventListener= new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s)
                    {
                        Message messages = dataSnapshot.getValue(Message.class);
                        if(messageReceivedAfterUserJoin(messages)) {
                            messagesList.add(messages);
                            messageAdapter.notifyItemInserted(messagesList.size()-1);
                            userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());

                        }

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };

        m_UserExitFromGroupEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                sendMessageButton.setEnabled(m_LoginManager.getLoggedInUser().getValue().isUserInGroup(groupId));
                sendFilesButton.setEnabled(m_LoginManager.getLoggedInUser().getValue().isUserInGroup(groupId));
                messageInputText.setEnabled(m_LoginManager.getLoggedInUser().getValue().isUserInGroup(groupId));
                m_CantSendMessageTextView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

    }

    private boolean messageReceivedAfterUserJoin(Message message) {
        //indexOfCurrentDate
        Date exitDate = null;
        Date messageDate = null;
        try {
            messageDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(message.getDate() + " " + message.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }

        while(indexOfCurrentDate < m_LoginManager.getLoggedInUser().getValue().getGroupsId().get(groupId).size()) {
            String timeUserJoinToGroup = m_LoginManager.getLoggedInUser().getValue().getGroupsId().get(groupId).get(indexOfCurrentDate).getFirst();
            String timeUserExitFromGroup = m_LoginManager.getLoggedInUser().getValue().getGroupsId().get(groupId).get(indexOfCurrentDate).getSecond();
            try {
                Date joinDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(timeUserJoinToGroup);
                if (!timeUserExitFromGroup.isEmpty()) {
                    exitDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(timeUserExitFromGroup);
                }

                if (joinDate.compareTo(messageDate) > 0) {
                    return false;
                }

                else
                {
                    if (timeUserExitFromGroup.isEmpty() || exitDate.compareTo(messageDate) > 0) {
                        return true;
                    } else {
                        indexOfCurrentDate++;
                    }
                }

            } catch (ParseException e) {
            }
        }

        return false;
    }



    private void initializeControllers() {

        chatToolBar=findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolBar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar,null);
        actionBar.setCustomView(actionBarView);

        groupNameTextView =findViewById(R.id.custom_group_name);
        groupCircleImageView =findViewById(R.id.custom_group_image);
        groupCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendUserToGroupInfoActivity();
            }
        });

        sendMessageButton = findViewById(R.id.send_message_button);
        sendFilesButton = findViewById(R.id.send_files_button);
        sendMessageButton.setEnabled(m_LoginManager.getLoggedInUser().getValue().isUserInGroup(groupId));
        sendFilesButton.setEnabled(m_LoginManager.getLoggedInUser().getValue().isUserInGroup(groupId));

        m_CantSendMessageTextView = findViewById(R.id.CantSendMessageTextView);
        if(m_LoginManager.getLoggedInUser().getValue().isUserInGroup(m_CurrentGroup.getId())) {
            m_CantSendMessageTextView.setVisibility(View.INVISIBLE);
        } else {
            m_CantSendMessageTextView.setVisibility(View.VISIBLE);
        }
//        sendMessageButton.setClickable(m_LoginManager.isUserInGroup(groupId));
//        sendFilesButton.setClickable(m_LoginManager.isUserInGroup(groupId));


        messageInputText = findViewById(R.id.input_message);
        messageInputText.setEnabled(m_LoginManager.getLoggedInUser().getValue().isUserInGroup(groupId));

        loadingBar=new ProgressDialog(this);
        messageAdapter = new MessageAdapter(messagesList);
        userMessagesList =  findViewById(R.id.private_messages_list_of_users);
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
        userMessagesList.setAdapter(messageAdapter);

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
        saveCurrentTime = currentTime.format(calendar.getTime());
    }

    private void sendUserToGroupInfoActivity() {
        if (m_LoginManager.getLoggedInUser().getValue().isUserInGroup(m_CurrentGroup.getId())) {
            Intent groupInfoActivity = new Intent(ChatActivity.this, GroupInfoActivity.class);
            groupInfoActivity.putExtra("group", m_CurrentGroup);
            startActivityForResult(groupInfoActivity,100);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==438 && resultCode==RESULT_OK&&data!=null &&data.getData()!=null)
        {
            fileUri=data.getData();
            uploadMessageToStorage();
        }
  //      else if(requestCode == 100)
   //     {removeChildEventListeners();}
        else
        {
                Toast.makeText(this, "Error, Nothing selected",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    private void sendTextMessage() {

       final String messageText = messageInputText.getText().toString();

        if (TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "Cannot send empty message", Toast.LENGTH_SHORT).show();
        }
        else {

            final String messagePushId = rootRef.child("Groups").child(m_CountryCode).child(groupId).child("Messages").push().getKey();
            sendMessage(messagePushId,messageText,"text");
        }
    }


   private void sendMessage(String messagePushId , String messageContent , String messageType) {

       Calendar calendar = Calendar.getInstance();

       SimpleDateFormat currentDate = new SimpleDateFormat("dd/MM/yyyy");
       saveCurrentDate = currentDate.format(calendar.getTime());

       SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm:ss");
       saveCurrentTime = currentTime.format(calendar.getTime());

       final String messageSenderRef = "Groups/" + m_CountryCode + "/" + groupId + "/Messages/";

       final Map messageTextBody = new HashMap() {
           {
               put("id", messagePushId);
               put("message", messageContent);
               put("type", messageType);
               put("from", userSenderId);
               put("time", saveCurrentTime);
               put("date", saveCurrentDate);
               put("senderName", userSenderName);
           }
       };

       if(needToSaveNameFieldInDb())
       {
           messageTextBody.put("name" , fileUri.getLastPathSegment());
       }

       Map messageBodyDetails = new HashMap() {
           {
               put(messageSenderRef + messagePushId, messageTextBody);
           }
       };

       rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
           @Override
           public void onComplete(@NonNull Task<Void> task) {

               if (task.isSuccessful()) {
                   Toast.makeText(ChatActivity.this, "Message sent successfully", Toast.LENGTH_SHORT).show();
               } else {
                   Toast.makeText(ChatActivity.this, "Error: message sent failed ", Toast.LENGTH_SHORT).show();
               }
               messageInputText.setText("");
           }
       });
   }

   //כרגע הפונקציה כאן עדיין לא מספיק טובה. צריך לראות איך מאחדים את המקרים כך שגם בשליחת תמונה וגם בשליחת קובץ יהיה המדד לכמה אחוזים נשלחו כבר (או לבטל פשוט בשליחת קובץ)
    private void uploadMessageToStorage() {
        loadingBar.setTitle("Sending File");
        loadingBar.setMessage("Please wait,we are sending the file");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        StorageReference storageReference;
        StorageReference filePath;
        final String messagePushId = rootRef.child("Groups").child(m_CountryCode).child(groupId).child("Messages").push().getKey();

        if (!checker.equals("image")) // docx or pdf
        {
            storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");
            filePath = storageReference.child(messagePushId + "." + checker);
            filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                    sendMessage(messagePushId, task.getResult().getStorage().getDownloadUrl().toString(), checker);
                    loadingBar.dismiss();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {

                    loadingBar.dismiss();
                    Toast.makeText(ChatActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    double p = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                    loadingBar.setMessage((int) p + " % Uploading");
                }
            });
        } else {
            storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
            filePath = storageReference.child(messagePushId + "." + "jpg");
            uploadTask = filePath.putFile(fileUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {

                    if (task.isSuccessful()) {
                        Uri downloadUrl = task.getResult();
                        myUrl = downloadUrl.toString();
                        sendMessage(messagePushId, myUrl, checker);
                    }
                    loadingBar.dismiss();

                }
            });
        }
    }
    private boolean needToSaveNameFieldInDb()
    {
        return  fileUri!=null;
    }

   @Override
   protected void onDestroy() {
       super.onDestroy();
       removeChildEventListeners();
   }
    private void removeChildEventListeners()
    {
        rootRef.child("Groups").child(m_CountryCode).child(groupId).child("Messages").removeEventListener(m_MessageEventListener);
        m_ExitRef.child("Users").child(m_LoginManager.getLoggedInUser().getValue().getId()).child("groupsId").child(groupId).removeEventListener(m_UserExitFromGroupEventListener);
    }



}
