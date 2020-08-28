package com.example.groupchatapp.Activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;

import com.example.groupchatapp.R;

public class ProgressDialogActivity {
    Activity activity;
    Dialog dialog;


    ProgressDialogActivity(Activity i_activity) {
        this.activity = i_activity;

    }


    void startDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.custom_dialog, null));
        builder.setCancelable(false);

        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }





    void dismissDialog()
    {
        dialog.dismiss();
    }

}
