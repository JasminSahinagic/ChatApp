package com.example.chatapp.chatapp.Application;

import android.app.Application;
import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

public class ChatApp extends Application {

    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private FirebaseUser user;

    @Override
    public void onCreate() {
        super.onCreate();
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if(user !=null){
            databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(dataSnapshot != null) {
                        databaseReference.child("online").onDisconnect().setValue(ServerValue.TIMESTAMP);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }
}
