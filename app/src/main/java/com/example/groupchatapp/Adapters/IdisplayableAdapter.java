package com.example.groupchatapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupchatapp.Models.IDisplayable;
import com.example.groupchatapp.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

 public class IdisplayableAdapter extends RecyclerView.Adapter<IdisplayableAdapter.IDisplayableViewHolder> {

    protected ArrayList<IDisplayable> m_Displayables;
    protected Context mContext;

    public IdisplayableAdapter(ArrayList<IDisplayable> i_Idisplayables, Context mContext) {
        this.m_Displayables = i_Idisplayables;
        this.mContext = mContext;
    }


    @NonNull
    @Override
    public IDisplayableViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.displayable_layout, parent, false);
        return new IDisplayableViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final IDisplayableViewHolder holder, int position) {

        final String retName = m_Displayables.get(position).getName();
        holder.IDisplayableName.setText(retName);
    }

    @Override
    public int getItemCount() {
        return m_Displayables.size();
    }


    public class IDisplayableViewHolder extends RecyclerView.ViewHolder {
        CircleImageView IDisplayablePhoto;
        TextView IDisplayableName;

        public IDisplayableViewHolder(@NonNull View itemView) {
            super(itemView);
            IDisplayableName = itemView.findViewById(R.id.displayable_name);
            IDisplayablePhoto = itemView.findViewById(R.id.displayable_image);
        }
    }
}

