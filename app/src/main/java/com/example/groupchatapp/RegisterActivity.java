package com.example.groupchatapp;


import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.groupchatlogic.LoginManager;


public class RegisterActivity extends AppCompatActivity
{
    private Button CreateAccountButton;
    private EditText UserEmail,UserPassword;
    private TextView AlreadyHaveAnAccountLink;
    private ProgressDialog loadingBar;

    private LoginManager Manager = LoginManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initializeFields();


        AlreadyHaveAnAccountLink.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                SendUserToLoginActivity();
            }
        });

        CreateAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CreateNewAccount();
            }
        });
    }

    private void CreateNewAccount()
    {
        String email=UserEmail.getText().toString();
        String password=UserPassword.getText().toString();

        if(TextUtils.isEmpty(email))
        {
            Toast.makeText(this,"Please enter email", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(password))
        {
            Toast.makeText(this,"Please enter password", Toast.LENGTH_SHORT).show();
        }
        else
        {
            loadingBar.setTitle("Creating new account");
            loadingBar.setMessage("Please wait, while we are creating account for you");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            try
            {
                Manager.CreateNewAccountWithEmailAndPassword(email, password);
                SendUserToSettingsActivity();
                Toast.makeText(RegisterActivity.this,"Account created successfully",Toast.LENGTH_SHORT).show();
            }
            catch (Exception e)
            {
                String message = e.getMessage();
                Toast.makeText(RegisterActivity.this,"Error:" + message,Toast.LENGTH_SHORT).show();
            }
            finally
            {
                loadingBar.dismiss();
            }
        }
    }

    private void  initializeFields()
    {
        CreateAccountButton = findViewById(R.id.register_button);
        UserEmail = findViewById(R.id.register_email);
        UserPassword = findViewById(R.id.register_password);
        AlreadyHaveAnAccountLink = findViewById(R.id.already_have_account_link);
        loadingBar=new ProgressDialog(this);
    }

    private void SendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(RegisterActivity.this,LoginActivity.class);
        startActivity(loginIntent);
    }

    private void SendUserToSettingsActivity()
    {
        Intent settingsIntent = new Intent(RegisterActivity.this,SettingsActivity.class);
        startActivity(settingsIntent);
    }
}
