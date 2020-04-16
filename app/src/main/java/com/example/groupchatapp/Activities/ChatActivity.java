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

    private String groupId, groupName, groupImageStr, userSenderId;
    private TextView groupNameTextView;//בסרטון יש כאן גם נראה לאחורנה
    private CircleImageView groupCircleImageView;
    private Toolbar chatToolBar;
    private ImageButton sendMessageButton,sendFilesButton;
    private EditText messageInputText;
    //private FirebaseAuth mAuth;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


        m_LoginManager = LoginManager.getInstance();
        m_CountryCode = LoginManager.getInstance().getLocationManager().getCountryCode();

        userSenderId = m_LoginManager.getLoggedInUser().getValue().getUid();
        rootRef= FirebaseDatabase.getInstance().getReference();
        groupId =getIntent().getExtras().get("group_id").toString();
        groupName =getIntent().getExtras().get("group_name").toString();
        if(getIntent().getExtras().get("group_image")!=null) {//maybe their is a better way to handle this
            groupImageStr = getIntent().getExtras().get("group_image").toString();
        }

        Toast.makeText(ChatActivity.this, groupId,Toast.LENGTH_SHORT).show();
        Toast.makeText(ChatActivity.this, groupName,Toast.LENGTH_SHORT).show();

        initializeControllers();

        groupNameTextView.setText(groupName);
        Picasso.get().load(groupImageStr).placeholder(R.drawable.profile_image).into(groupCircleImageView);

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessage();
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

        rootRef.child("Groups").child(m_CountryCode).child(groupId).child("Message")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s)
                    {
                        Message messages = dataSnapshot.getValue(Message.class);
                        if(messageReceivedAfterUserJoin(messages)) {
                            messagesList.add(messages);
                        }
                        messageAdapter.notifyDataSetChanged();

                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());

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
                });
    }

    private boolean messageReceivedAfterUserJoin(Message message) {

        //צריך לשפר.. לא אוהב את הקטע שמחזיר false בסוף
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
        messageAdapter = new MessageAdapter(messagesList,groupId);
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
            loadingBar.setTitle("Sending File");
            loadingBar.setMessage("Please wait,we are sending the file");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            fileUri=data.getData();

            if(!checker.equals("image")) // docx or pdf
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Document Files");
                final String messageSenderRef ="Groups/"+m_CountryCode+"/"+groupId +"/Message/";

                DatabaseReference userMessageKeyRef =
                        rootRef.child("Groups").child(m_CountryCode).child(groupId).child("Message").push();
                final String messagePushId = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushId + "." + checker);

                filePath.putFile(fileUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            final Map  messageTextBody  = new HashMap(){
                                {
                                    put("mid",messagePushId);
                                    put("message", task.getResult().getStorage().getDownloadUrl().toString());
                                    put("name", fileUri.getLastPathSegment());
                                    put("type", checker);
                                    put("from", userSenderId);
                                    put("time", saveCurrentTime);
                                    put("date", saveCurrentDate);

                                }};

                            Map  messageBodyDetails  = new HashMap(){
                                {
                                    put(messageSenderRef+messagePushId,messageTextBody);
                                }};

                            rootRef.updateChildren(messageBodyDetails);
                            loadingBar.dismiss();

                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        loadingBar.dismiss();
                        Toast.makeText(ChatActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                        double p = (100.0 *taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                        loadingBar.setMessage((int) p + " % Uploading");
                    }
                });
            }
            else if(checker.equals("image"))
            {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("Image Files");
                final String messageSenderRef ="Groups/"+m_CountryCode+"/"+groupId +"/Message/";

                DatabaseReference userMessageKeyRef =
                        rootRef.child("Groups").child(m_CountryCode).child(groupId).child("Message").push();
                final String messagePushId = userMessageKeyRef.getKey();

                final StorageReference filePath = storageReference.child(messagePushId + "." + "jpg");
                uploadTask=filePath.putFile(fileUri);

                uploadTask.continueWithTask(new Continuation() {
                    @Override
                    public Object then(@NonNull Task task) throws Exception {
                        if(!task.isSuccessful())
                        {
                            throw task.getException();
                        }
                        return filePath.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {

                        if(task.isSuccessful())
                        {
                            Uri downloadUrl = task.getResult();
                            myUrl=downloadUrl.toString();

                            final Map  messageTextBody  = new HashMap(){
                                {
                                    put("mid",messagePushId);
                                    put("message", myUrl);
                                    put("name", fileUri.getLastPathSegment());
                                    put("type", checker);
                                    put("from", userSenderId);
                                    put("time", saveCurrentTime);
                                    put("date", saveCurrentDate);

                                }};

                            Map  messageBodyDetails  = new HashMap(){
                                {
                                    put(messageSenderRef+messagePushId,messageTextBody);
                                }};

                            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {

                                    if(task.isSuccessful())
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this,"Message sent successfully",Toast.LENGTH_SHORT).show();
                                    }
                                    else
                                    {
                                        loadingBar.dismiss();
                                        Toast.makeText(ChatActivity.this,"Error: message sent failed ",Toast.LENGTH_SHORT).show();
                                    }
                                    messageInputText.setText("");
                                }
                            });
                        }
                    }
                });

            }
            else
            {
                loadingBar.dismiss();
                Toast.makeText(this, "Error, Nothing selected",Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();


    }

    private void sendMessage() {

       final String messageText = messageInputText.getText().toString();

        if (TextUtils.isEmpty(messageText))
        {
            Toast.makeText(this, "Cannot send empty message", Toast.LENGTH_SHORT).show();
        }
        else
            {

                final String messageSenderRef ="Groups/"+m_CountryCode+"/"+groupId +"/Message/";

                DatabaseReference userMessageKeyRef =
                        rootRef.child("new Group").child(groupId).child("Message").push();
                final String messagePushId = userMessageKeyRef.getKey();

                final Map  messageTextBody  = new HashMap(){
                    {
                        put("mid",messagePushId);
                        put("message", messageText);
                        put("type", "text");
                        put("from", userSenderId);
                        put("time", saveCurrentTime);
                        put("date", saveCurrentDate);

                    }};

                Map  messageBodyDetails  = new HashMap(){
                    {
                        put(messageSenderRef+messagePushId,messageTextBody);
                    }};

                rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                        {
                            Toast.makeText(ChatActivity.this,"Message sent successfully",Toast.LENGTH_SHORT).show();
                        }
                        else
                        {
                            Toast.makeText(ChatActivity.this,"Error: message sent failed ",Toast.LENGTH_SHORT).show();
                        }
                        messageInputText.setText("");
                    }
                });



            }
    }
}
