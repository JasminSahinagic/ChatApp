package com.example.chatapp.chatapp.Activity;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.chatapp.Model.UserModel;
import com.example.chatapp.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;

public class ProfileActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView profileDisplayName;
    private TextView profileDisplayStatus;
    private ImageView profileImageView;
    private Button profileActiveButton;
    private UserModel model;
    private DatabaseReference databaseReference;
    private ProgressDialog progressDialog;
    private Button profileDeclineButton;
    private String mCurrent_state;
    private DatabaseReference mFriendRequest;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference mFriendDatabase;
    private DatabaseReference mNotificationDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setUpProfile();
        profileActiveButton.setOnClickListener(this);
        profileDeclineButton.setOnClickListener(this);
    }


    public void setUpProfile(){

        //---------------- Database
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        model = (UserModel) getIntent().getSerializableExtra("UserData");
        progressDialog = new ProgressDialog(this);
        mFriendRequest = FirebaseDatabase.getInstance().getReference().child("Friend_req");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(model.getUserID());
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("notifications");
        //----------------

        //-----ProfileActivity
        profileActiveButton = (Button) findViewById(R.id.profileSendButton);
        profileDeclineButton = (Button) findViewById(R.id.profileDeclineButton);
        profileImageView = (ImageView) findViewById(R.id.profileImage);
        profileDisplayName = (TextView) findViewById(R.id.profileDisplayName);
        profileDisplayStatus = (TextView) findViewById(R.id.profileStatus);
        mCurrent_state = "not_friends";
        //----------------
        userData();

    }

    public void userData() {

        progressDialog.setTitle("Loading data...");
        progressDialog.setCanceledOnTouchOutside(true);
        progressDialog.show();
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                profileDisplayName.setText(dataSnapshot.child("name").getValue().toString());
                profileDisplayStatus.setText(dataSnapshot.child("status").getValue().toString());
                if(!model.getImage().equals("default")){
                    Picasso.with(ProfileActivity.this).load(dataSnapshot.child("image").getValue().toString())
                            .into(profileImageView);
                }
                mFriendRequest.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.hasChild(model.getUserID())){
                            String reqType = dataSnapshot.child(model.getUserID()).child("request_type")
                                    .getValue().toString();
                            if(reqType.equals("received")){
                                mCurrent_state = "req_received";
                                profileActiveButton.setText("Accept Friend Request");
                                profileDeclineButton.setVisibility(View.VISIBLE);
                                profileDeclineButton.setEnabled(true);
                            }else if(reqType.equals("sent")){
                                mCurrent_state = "req_sent";
                                profileActiveButton.setText("Cancel friend request");
                                profileDeclineButton.setVisibility(View.INVISIBLE);
                                profileDeclineButton.setEnabled(false);
                            }
                            progressDialog.dismiss();
                        }else{
                            mFriendDatabase.child(user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                    if(dataSnapshot.hasChild(model.getUserID())){

                                        mCurrent_state = "friends";
                                        profileActiveButton.setText("UnFriend" + model.getName());
                                        profileDeclineButton.setVisibility(View.INVISIBLE);
                                        profileDeclineButton.setEnabled(false);
                                        progressDialog.dismiss();
                                    }else{
                                        mCurrent_state = "not_friends";
                                        profileActiveButton.setText("Send friend request");
                                        progressDialog.dismiss();
                                        profileDeclineButton.setVisibility(View.INVISIBLE);
                                        profileDeclineButton.setEnabled(false);
                                    }
                                }
                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {
                                    progressDialog.dismiss();
                                }
                            });
                        }
                        progressDialog.dismiss();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.profileSendButton:
                selectState();
                break;
            case R.id.profileDeclineButton:
                declineRequest();
                break;
        }
    }

    public  void declineRequest(){
        mFriendRequest.child(user.getUid()).child(model.getUserID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                mFriendRequest.child(model.getUserID()).child(user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        profileActiveButton.setEnabled(true);
                        mCurrent_state = "not_friends";
                        profileActiveButton.setText("Send friend request");
                        profileDeclineButton.setVisibility(View.INVISIBLE);
                        profileDeclineButton.setEnabled(false);

                    }
                });
            }
        });
    }


    private void selectState() {
        profileActiveButton.setEnabled(false);
        if(mCurrent_state.equals("not_friends")){
            mFriendRequest.child(user.getUid()).child(model.getUserID())
                    .child("request_type").setValue("sent").addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        mFriendRequest.child(model.getUserID()).child(user.getUid()).child("request_type")
                                .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                HashMap<String, String> data = new HashMap<>();
                                data.put("from",user.getUid());
                                data.put("type","request");
                                mNotificationDatabase.child(model.getUserID()).push().setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        profileActiveButton.setEnabled(true);
                                        profileDeclineButton.setVisibility(View.INVISIBLE);
                                        profileDeclineButton.setEnabled(false);
                                        mCurrent_state = "req_sent";
                                        profileActiveButton.setText("Cancel friend request");
                                    }
                                });
                            }
                        });
                    }else{
                        Toast.makeText(ProfileActivity.this, "Failed sending request", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        if(mCurrent_state.equals("req_sent")){
            mFriendRequest.child(user.getUid()).child(model.getUserID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mFriendRequest.child(model.getUserID()).child(user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            profileActiveButton.setEnabled(true);
                            mCurrent_state = "not_friends";
                            profileActiveButton.setText("Send friend request");
                            profileDeclineButton.setVisibility(View.INVISIBLE);
                            profileDeclineButton.setEnabled(false);

                        }
                    });
                }
            });
        }
        if(mCurrent_state.equals("req_received")){
            final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
            final HashMap<String, String> data = new HashMap<>();
            data.put("date",currentDate.toString());
            mFriendDatabase.child(user.getUid()).child(model.getUserID()).child("date").setValue(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            mFriendDatabase.child(model.getUserID()).child(user.getUid()).child("date")
                                    .setValue(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    mFriendRequest.child(user.getUid()).child(model.getUserID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            mFriendRequest.child(model.getUserID()).child(user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    profileActiveButton.setEnabled(true);
                                                    mCurrent_state = "friends";
                                                    profileActiveButton.setText("UnFriend "+ model.getName());
                                                    profileDeclineButton.setVisibility(View.INVISIBLE);
                                                    profileDeclineButton.setEnabled(false);
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
        }
        if(mCurrent_state.equals("friends")){
            mFriendDatabase.child(user.getUid()).child(model.getUserID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    mFriendDatabase.child(model.getUserID()).child(user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            profileActiveButton.setEnabled(true);
                            mCurrent_state = "not_friends";
                            profileActiveButton.setText("Send friend request");
                            profileDeclineButton.setVisibility(View.INVISIBLE);
                            profileDeclineButton.setEnabled(false);
                        }
                    });
                }
            });
        }
    }
}
