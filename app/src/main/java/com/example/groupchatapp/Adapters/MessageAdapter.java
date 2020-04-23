package com.example.groupchatapp.Adapters;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupchatapp.LoginManager;
import com.example.groupchatapp.Models.Message;
import com.example.groupchatapp.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>
{
    private List<Message> groupMessagesList;
    private DatabaseReference usersRef;


    public MessageAdapter(List<Message> groupMessagesList)
    {
        this.groupMessagesList = groupMessagesList;
    }


    public class MessageViewHolder extends RecyclerView.ViewHolder
    {
        public TextView senderMessageText, receiverMessageText;
        public CircleImageView receiverProfileImage;
        public ImageView messageReceiverPicture,messageSenderPicture;
        public MessageViewHolder(@NonNull View itemView)
        {
            super(itemView);

            senderMessageText =  itemView.findViewById(R.id.sender_message_text);
            receiverMessageText =  itemView.findViewById(R.id.receiver_message_text);
            receiverProfileImage =  itemView.findViewById(R.id.message_profile_image);
            messageReceiverPicture = itemView.findViewById(R.id.message_receiver_image_view);
            messageSenderPicture = itemView.findViewById(R.id.message_sender_image_view);
            usersRef=FirebaseDatabase.getInstance().getReference().child("Users");
        }

    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
    {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup, false);

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, final int position)
    {
        String currentUserId = LoginManager.getInstance().getLoggedInUser().getValue().getUid();
        Message message = groupMessagesList.get(position);
        String fromUserId = message.getFrom();
        String fromMessageType = message.getType();

        usersRef.child(fromUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                        if (dataSnapshot.hasChild("photoUrl"))
                        {
                            String receiverImage = dataSnapshot.child("photoUrl").getValue().toString();

                            Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProfileImage);
                        }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        messageViewHolder.receiverMessageText.setVisibility(View.GONE);
        messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
        messageViewHolder.senderMessageText.setVisibility(View.GONE);
        messageViewHolder.messageSenderPicture.setVisibility(View.GONE);
        messageViewHolder.messageReceiverPicture.setVisibility(View.GONE);

     if (fromMessageType.equals("text"))
     {
         if (fromUserId.equals(currentUserId))
         {
             messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);

             messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
             messageViewHolder.senderMessageText.setTextColor(Color.BLACK);
             messageViewHolder.senderMessageText.setText(message.getTime().substring(0,5) + "\n\n" + message.getMessage());
         }
         else
         {
             messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
             messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);

             messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
             messageViewHolder.receiverMessageText.setTextColor(Color.BLACK);
             messageViewHolder.receiverMessageText.setText(message.getSenderName() + " - " +message.getTime().substring(0,5) + "\n\n" + message.getMessage());
         }


     }
     else if(fromMessageType.equals("image"))
     {
         if(fromUserId.equals(currentUserId))
         {
             messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
             Picasso.get().load(message.getMessage()).into(messageViewHolder.messageSenderPicture);
         }
         else
         {
             messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
             messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
             Picasso.get().load(message.getMessage()).into(messageViewHolder.messageReceiverPicture);
         }
     }
     else if(fromMessageType.equals("pdf") || fromMessageType.equals("docx"))
     {
         if(fromUserId.equals(currentUserId))
         {
             messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
             //Ron Add this two lines.. Don't know if both are needed, but it works with both.
             messageViewHolder.messageSenderPicture.setVisibility(View.VISIBLE);
             messageViewHolder.messageSenderPicture.setBackgroundResource(R.drawable.file);
             //Picasso.get().load()

             messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {

                     Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(groupMessagesList.get(position).getMessage()));
                     messageViewHolder.itemView.getContext().startActivity(intent);
                 }
             });
         }
         else
         {
             messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
             messageViewHolder.messageReceiverPicture.setVisibility(View.VISIBLE);
             messageViewHolder.messageReceiverPicture.setBackgroundResource(R.drawable.file);

             messageViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {

                     Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(groupMessagesList.get(position).getMessage()));
                     messageViewHolder.itemView.getContext().startActivity(intent);
                 }
             });
         }
     }
  }


    @Override
    public int getItemCount()
    {
        return groupMessagesList.size();
    }

}
