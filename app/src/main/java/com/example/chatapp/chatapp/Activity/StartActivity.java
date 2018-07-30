package com.example.chatapp.chatapp.Activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.chatapp.chatapp.R;

public class StartActivity extends AppCompatActivity implements View.OnClickListener {

    private Button buttonStartLogIn;
    private Button buttonStartCreateAccount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        setUpStart();
        buttonStartLogIn.setOnClickListener(this);
        buttonStartCreateAccount.setOnClickListener(this);
    }


    public void setUpStart(){
        buttonStartLogIn = (Button) findViewById(R.id.buttonStartLogIn);
        buttonStartCreateAccount = (Button) findViewById(R.id.buttonStartCreateAccount);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.buttonStartLogIn:
                startActivity(new Intent(StartActivity.this,LogInActivity.class));
                break;
            case R.id.buttonStartCreateAccount:
                startActivity(new Intent(StartActivity.this,RegisterActivity.class));
                break;
        }

    }
}
