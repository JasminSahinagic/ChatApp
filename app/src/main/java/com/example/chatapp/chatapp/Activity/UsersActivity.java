package com.example.chatapp.chatapp.Activity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.example.chatapp.chatapp.Adapter.UsersAdapter;
import com.example.chatapp.chatapp.Model.UserModel;
import com.example.chatapp.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private FirebaseUser user;
    private RecyclerView recyclerView;
    private DatabaseReference databaseReference;
    private RecyclerView.Adapter adapter;
    private List<UserModel> userModelList;
    private List<UserModel> tempList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        setUpUsers();
    }

    public void setUpUsers(){
        user = FirebaseAuth.getInstance().getCurrentUser();
        userModelList = new ArrayList<>();
        tempList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewUsers);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        toolbar = (Toolbar) findViewById(R.id.users_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("All users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
    }

    @Override
    protected void onStart() {
        super.onStart();
        databaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                UserModel model = dataSnapshot.getValue(UserModel.class);
                if(!model.getUserID().equals(user.getUid())){
                    userModelList.add(model);
                }
                HashSet<UserModel> uniqueValues = new HashSet<>(userModelList);
                for (UserModel value : uniqueValues) {
                    tempList.add(value);
                }
                userModelList.clear();
                Collections.reverse(tempList);
                adapter = new UsersAdapter(UsersActivity.this, tempList);
                recyclerView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
