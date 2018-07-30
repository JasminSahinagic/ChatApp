package com.example.chatapp.chatapp.Activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chatapp.chatapp.Adapter.ChatAdapter;
import com.example.chatapp.chatapp.Application.UserOnlineStatus;
import com.example.chatapp.chatapp.Model.ChatModel;
import com.example.chatapp.chatapp.Model.UserModel;
import com.example.chatapp.chatapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener{

    private UserModel userModel;
    private DatabaseReference databaseReference;
    private DatabaseReference databaseReferenceChat;
    private DatabaseReference databaseReferenceMessage;
    private Toolbar toolbar;
    private TextView chatName;
    private TextView chatOnlineStatus;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private ImageButton chatSend;
    private EditText chatMessage;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private  List<ChatModel> chatModelList;
    private static final int TOTAL_ITEMS_LOAD = 10;
    private int currentPage = 1;
    private SwipeRefreshLayout swipeRefreshLayout;
    private int itemCount = 0;
    private String lastKey="";
    private String prevKey="";
    private Uri chatImg;
    private static final int GALLERY_CODE=1;
    private StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_activty);
        setUpChat();
        chatSend.setOnClickListener(this);
    }


    public void setUpChat(){

        //---------Database
        storageReference = FirebaseStorage.getInstance().getReference();
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.chatSwipeLayout);
        userModel = (UserModel) getIntent().getSerializableExtra("userID");
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users")
                .child(userModel.getUserID());
        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        databaseReferenceChat = FirebaseDatabase.getInstance().getReference();
        databaseReferenceMessage = FirebaseDatabase.getInstance().getReference();
        //---------------------------
        //-----------ChatActivityOptions
        chatSend = (ImageButton) findViewById(R.id.chatSendButton);
        chatMessage = (EditText) findViewById(R.id.chatMessage);

        //-------------------- Toolbar
        toolbar = (Toolbar) findViewById(R.id.chatToolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.chat_custom_bar,null);
        actionBar.setCustomView(view);
        chatName = (TextView) findViewById(R.id.textViewDisplayName);
        chatOnlineStatus = (TextView) findViewById(R.id.textViewDisplayOnlineStatus);
        chatName.setText(userModel.getName());
        getOnlineStatus();
        //-------------------------------------------------------
        //-----RecyclerView
        chatModelList = new ArrayList<>();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerViewChat);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter(this, chatModelList, userModel.getImage());
        recyclerView.setAdapter(adapter);
        loadMessage();
        onRefresh();
        //--------------------
        createChat();
    }

    public void getOnlineStatus(){
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String online = dataSnapshot.child("online").getValue().toString();
                if(online.equals("true")){
                    chatOnlineStatus.setText("Online");
                }else{
                    UserOnlineStatus gatTimeAgo = new UserOnlineStatus();
                    long lastTime = Long.parseLong(online);
                    String lastSeen = gatTimeAgo.getTimeAgo(lastTime, ChatActivity.this);
                    chatOnlineStatus.setText(lastSeen);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void createChat(){
        databaseReferenceChat.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(userModel.getUserID())){
                    Map cahtAddMap = new HashMap();
                    cahtAddMap.put("seen",false);
                    cahtAddMap.put("timestamp", ServerValue.TIMESTAMP);
                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/"+user.getUid()+"/"+userModel.getUserID(), cahtAddMap);
                    chatUserMap.put("Chat/"+userModel.getUserID()+"/"+user.getUid(), cahtAddMap);

                    databaseReferenceChat.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if(databaseError != null){
                                Log.d("CHAT_LOG",databaseError.getMessage().toString());
                            }
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {

        String message = chatMessage.getText().toString();
        if(!TextUtils.isEmpty(message)){
            String currentUser = "messages/"+user.getUid()+"/"+userModel.getUserID()+'/';
            String chatUser = "messages/"+userModel.getUserID()+"/"+user.getUid()+'/';
            Map messageMap = new HashMap();
            messageMap.put("message",message);
            messageMap.put("seen",false);
            messageMap.put("type","text");
            messageMap.put("time",ServerValue.TIMESTAMP);
            messageMap.put("from",user.getUid());
            DatabaseReference referencePush = FirebaseDatabase.getInstance().getReference()
                    .child("messages").child(user.getUid()).child(userModel.getUserID()).push();
            String pushID = referencePush.getKey();
            Map messageUserMap = new HashMap();
            messageUserMap.put(currentUser+"/"+pushID,messageMap);
            messageUserMap.put(chatUser+"/"+pushID,messageMap);
            databaseReferenceMessage.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                    if(databaseError != null){
                        Log.d("CHAT_LOG",databaseError.getMessage().toString());
                    }
                }
            });
        }
    }

    private void loadMessage() {
        DatabaseReference messageRef = databaseReferenceMessage.child("messages")
                .child(user.getUid()).child(userModel.getUserID());
        Query messageQuery = messageRef.limitToLast(currentPage * TOTAL_ITEMS_LOAD);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ChatModel model = dataSnapshot.getValue(ChatModel.class);
                itemCount++;
                if(itemCount == 1){
                    lastKey = dataSnapshot.getKey();
                    prevKey = dataSnapshot.getKey();
                }
                chatModelList.add(model);
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(chatModelList.size()-1);
                swipeRefreshLayout.setRefreshing(false);
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

    public void loadMoreMessage(){
        DatabaseReference messageRef = databaseReferenceMessage.child("messages")
                .child(user.getUid()).child(userModel.getUserID());
        Query messageQuery = messageRef.orderByKey().endAt(lastKey).limitToLast(10);
        messageQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                ChatModel model = dataSnapshot.getValue(ChatModel.class);
                if(!prevKey.equals(dataSnapshot.getKey())){
                    chatModelList.add(itemCount++, model);
                }else{
                    prevKey = lastKey;
                }
                if(itemCount == 1){
                    lastKey = dataSnapshot.getKey().toString();
                }

                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(chatModelList.size()-1);
                swipeRefreshLayout.setRefreshing(false);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == GALLERY_CODE && resultCode  == RESULT_OK){
            chatImg = data.getData();
            final String currentUser = "messages/"+user.getUid()+"/"+userModel.getUserID()+'/';
            final String chatUser = "messages/"+userModel.getUserID()+"/"+user.getUid()+'/';
            DatabaseReference referencePush = FirebaseDatabase.getInstance().getReference()
                    .child("messages").child(user.getUid()).child(userModel.getUserID()).push();
            final String pushID = referencePush.getKey();
            final StorageReference filePath = storageReference.child("message_images").child(pushID+".jpg");
            filePath.putFile(chatImg).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Map hashMap = new HashMap();
                            hashMap.put("message",uri.toString());
                            hashMap.put("seen",false);
                            hashMap.put("type","image");
                            hashMap.put("time",ServerValue.TIMESTAMP);
                            hashMap.put("from",user.getUid());
                            Map messageUserMap = new HashMap();
                            messageUserMap.put(currentUser+"/"+pushID,hashMap);
                            messageUserMap.put(chatUser+"/"+pushID,hashMap);
                            databaseReferenceMessage.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                                    if(databaseError != null) {
                                        Log.d("CHAT_LOG", databaseError.getMessage().toString());
                                    }
                                }
                            });
                        }
                    });
                }
            });
        }
    }


    public  void onRefresh(){
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                currentPage++;
                itemCount=0;
                loadMoreMessage();
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.chatSendButton:
                sendMessage();
                chatMessage.setText("");
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode== KeyEvent.KEYCODE_BACK)
            Toast.makeText(getApplicationContext(), "Back button disabled",
                    Toast.LENGTH_LONG).show();

        return false;
    }
}






















