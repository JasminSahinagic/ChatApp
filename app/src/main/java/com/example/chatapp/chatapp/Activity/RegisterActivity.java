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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    private Button registerCAButton;
    private TextInputEditText registerDisplayName;
    private TextInputEditText registerYourEmail;
    private TextInputEditText registerYourPassword;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference databaseReference;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        setUpRegister();
        registerCAButton.setOnClickListener(this);
    }

    public void setUpRegister(){
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        progressDialog = new ProgressDialog(this);
        toolbar = (Toolbar) findViewById(R.id.register_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        registerCAButton = (Button) findViewById(R.id.registerCAButton);
        registerDisplayName = (TextInputEditText) findViewById(R.id.registerDisplayName);
        registerYourEmail = (TextInputEditText) findViewById(R.id.registerYourEmal);
        registerYourPassword = (TextInputEditText) findViewById(R.id.registerPassword);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.registerCAButton:
                createAccount();
            break;
        }
    }

    private void createAccount() {
        final String name = registerDisplayName.getText().toString();
        final String email = registerYourEmail.getText().toString();
        final String pass = registerYourPassword.getText().toString();
        progressDialog.setMessage("Creating account...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        if(!TextUtils.isEmpty(name) &&!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass)){
            auth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                user = auth.getCurrentUser();
                                String deviceToken = FirebaseInstanceId.getInstance().getToken();
                                HashMap<String, String> regUser = new HashMap<>();
                                regUser.put("name",name );
                                regUser.put("status","defaultStatus");
                                regUser.put("image","default");
                                regUser.put("userID",user.getUid());
                                regUser.put("device_token",deviceToken);
                                regUser.put("userEmail",email);
                                regUser.put("userPassword",pass);
                                databaseReference.child(user.getUid()).setValue(regUser);
                                progressDialog.dismiss();
                                Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                Toast.makeText(RegisterActivity.this, "Authentication successful", Toast.LENGTH_SHORT).show();
                                finish();
                            } else {
                                // If sign in fails, display a message to the user.
                                Toast.makeText(RegisterActivity.this, "Authentication failed...",
                                        Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }
                        }
                    });
        }
    }
}
