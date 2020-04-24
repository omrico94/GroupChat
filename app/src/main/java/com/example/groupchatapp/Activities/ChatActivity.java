package com.example.groupchatapp.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
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
    private DatabaseReference rootRef;

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

    private ChildEventListener m_MessageEventListener , m_GroupUsersIdEventListener;

    private DatabaseReference m_GroupRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        m_LoginManager = LoginManager.getInstance();
        m_CountryCode = LoginManager.getInstance().getLocationManager().getCountryCode();

        userSenderId = m_LoginManager.getLoggedInUser().getValue().getUid();
        userSenderName = m_LoginManager.getLoggedInUser().getValue().getName();
        rootRef= FirebaseDatabase.getInstance().getReference();

        m_GroupRef = FirebaseDatabase.getInstance().getReference();

        groupId =getIntent().getExtras().get("group_id").toString();
        groupName =getIntent().getExtras().get("group_name").toString();
        if(getIntent().getExtras().get("group_image")!=null) {//maybe their is a better way to handle this
            groupImageStr = getIntent().getExtras().get("group_image").toString();
        }

        Toast.makeText(ChatActivity.this, groupName,Toast.LENGTH_SHORT).show();

        initializeControllers();

        groupNameTextView.setText(groupName);
        Picasso.get().load(groupImageStr).placeholder(R.drawable.profile_image).into(groupCircleImageView);

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
        FirebaseListenerService.addChildEventListenerToRemoveList( rootRef.child("Groups").child(m_CountryCode).child(groupId).child("Messages"),m_MessageEventListener);
        m_GroupRef.child("Groups").child(m_CountryCode).child(groupId).child("usersId")
                .addChildEventListener(m_GroupUsersIdEventListener);
        FirebaseListenerService.addChildEventListenerToRemoveList(m_GroupRef.child("Groups").child(m_CountryCode).child(groupId).child("usersId"),m_GroupUsersIdEventListener);

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



        m_GroupUsersIdEventListener= new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                String userId = dataSnapshot.getValue(String.class);

                if (userId.equals(m_LoginManager.getLoggedInUser().getValue().getUid())) {
                    //The user is out of group's radius.
                    //Now we finish the chat activity.
                    finish();
                } else {
                    //Someone is out from the group.
                    //Can use for users in group.
                }
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

        String timeUserJoinToGroup=m_LoginManager.getLoggedInUser().getValue().getGroupsId().get(groupId);
        try {
                Date joinDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(timeUserJoinToGroup);
                Date messageDate= new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(message.getDate() + " " + message.getTime());
                return joinDate.compareTo(messageDate)<=0;
            }
            catch (ParseException e) {

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

        sendMessageButton = findViewById(R.id.send_message_button);
        sendFilesButton = findViewById(R.id.send_files_button);

        messageInputText = findViewById(R.id.input_message);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==438 && resultCode==RESULT_OK&&data!=null &&data.getData()!=null)
        {
            fileUri=data.getData();
            uploadMessageToStorage();
        }
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

       final String messageSenderRef = "Groups/" + m_CountryCode + "/" + groupId + "/Messages/";

       final Map messageTextBody = new HashMap() {
           {
               put("mid", messagePushId);
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
}
