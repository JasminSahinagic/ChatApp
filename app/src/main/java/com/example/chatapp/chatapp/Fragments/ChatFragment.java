package com.example.chatapp.chatapp.Fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.chatapp.chatapp.Adapter.ChatFragmentAdapter;
import com.example.chatapp.chatapp.Adapter.FriendsAdapter;
import com.example.chatapp.chatapp.Model.UserModel;
import com.example.chatapp.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFragment extends Fragment {

    private DatabaseReference databaseReference;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<UserModel> userModelList;
    private List<UserModel> tempList;
    private Context context;
    private View view;


    public ChatFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_chat, container, false);
        context = view.getContext();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        auth =FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        recyclerView = (RecyclerView) view.findViewById(R.id.recyclerViewChatFragment);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userModelList = new ArrayList<>();
        tempList = new ArrayList<>();
        return  view;
    }

    @Override
    public void onStart() {
        super.onStart();
        databaseReference.child("messages").child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot data : dataSnapshot.getChildren()) {
                    databaseReference.child("Users").child(data.getKey()).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            UserModel model = new UserModel();
                            model.setName(dataSnapshot.child("name").getValue().toString());
                            model.setImage(dataSnapshot.child("image").getValue().toString());
                            model.setStatus(dataSnapshot.child("status").getValue().toString());
                            model.setUserID(dataSnapshot.child("userID").getValue().toString());
                            String userOnline = dataSnapshot.child("online").getValue().toString();
                            userModelList.add(model);
                            HashSet<UserModel> uniqueValues = new HashSet<>(userModelList);
                            for (UserModel value : uniqueValues) {
                                tempList.add(value);
                            }
                            Collections.reverse(tempList);
                            adapter = new ChatFragmentAdapter(context, tempList, userOnline);
                            recyclerView.setAdapter(adapter);
                            adapter.notifyDataSetChanged();
                            userModelList.clear();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
