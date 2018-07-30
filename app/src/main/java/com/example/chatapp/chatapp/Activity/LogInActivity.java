package com.example.chatapp.chatapp.Activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.example.chatapp.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LogInActivity extends AppCompatActivity implements View.OnClickListener {

    private TextInputEditText logInYourEmail;
    private TextInputEditText logInPassword;
    private Button logInButton;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);
        setUpLogIn();
        logInButton.setOnClickListener(this);
    }

    public void setUpLogIn(){
        progressDialog = new ProgressDialog(this);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        toolbar = (Toolbar) findViewById(R.id.logIn_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("LogIn:");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        logInYourEmail = this.findViewById(R.id.logInYourEmail);
        logInPassword = this.findViewById(R.id.logInPass);
        logInButton =this.findViewById(R.id.logInButton);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.logInButton:
                userLogIn();
                break;
        }
    }

    private void userLogIn() {
        String email = logInYourEmail.getText().toString();
        String password = logInPassword.getText().toString();
        progressDialog.setMessage("Authentication...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(email)){
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                user = auth.getCurrentUser();
                                //Token ID
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                databaseReference.child(user.getUid()).child("device_token").setValue(deviceToken).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        progressDialog.dismiss();
                                        Intent intent = new Intent(LogInActivity.this,MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                            } else {

                                Toast.makeText(LogInActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    });
        }
    }
}
