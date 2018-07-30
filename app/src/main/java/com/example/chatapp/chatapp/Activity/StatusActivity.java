package com.example.chatapp.chatapp.Activity;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.chatapp.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity implements View.OnClickListener{


    private TextInputEditText statusInput;
    private Button statusButton;
    private Toolbar toolbar;
    private ProgressDialog progressDialog;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    private String status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        setUpStatus();
        statusButton.setOnClickListener(this);
    }

    public void setUpStatus(){
        progressDialog = new ProgressDialog(this);
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
        status = getIntent().getStringExtra("Data");
        statusInput = (TextInputEditText) findViewById(R.id.statusInput);
        statusInput.setHint(status);
        statusButton = (Button) findViewById(R.id.statusButton);
        toolbar = (Toolbar) findViewById(R.id.status_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.statusButton:
                saveStatus();
                break;
        }
    }

    private void saveStatus() {
        String status = statusInput.getText().toString();
        if(!status.isEmpty()){
            progressDialog.setMessage("Saving status...");
            progressDialog.show();
            databaseReference.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(StatusActivity.this, "Status saved...", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                }
            });
        }else{
            Toast.makeText(StatusActivity.this, "Invalid input...", Toast.LENGTH_SHORT).show();
        }
    }
}
