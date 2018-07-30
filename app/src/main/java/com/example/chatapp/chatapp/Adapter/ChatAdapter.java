package com.example.chatapp.chatapp.Adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chatapp.chatapp.Model.ChatModel;
import com.example.chatapp.chatapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private Context context;
    private List<ChatModel> chatModelList;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference databaseReference;
    private DatabaseReference databaseReferenceMessage;
    private String image;

    public ChatAdapter(Context context, List<ChatModel> chatModelList, String image) {
        this.image = image;
        this.context = context;
        this.chatModelList = chatModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_row,parent,false);
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
        ChatModel model = chatModelList.get(position);
        final String from = model.getFrom();
        String type = model.getType();
        holder.chatBoxText.setText(model.getMessage());
        if(from.equals(user.getUid())) {
            holder.chatBoxText.setBackgroundResource(R.drawable.whiteshape);
            holder.chatBoxText.setTextColor(Color.BLACK);
        }
        else{
            holder.chatBoxImage.setVisibility(View.INVISIBLE);
            holder.chatBoxText.setBackgroundResource(R.drawable.shape);
            holder.chatBoxText.setTextColor(Color.WHITE);
        }
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(from.equals(user.getUid())) {
                Picasso.with(context).load(dataSnapshot.child("image").getValue().toString())
                        .into(holder.chatBoxImage);
                    holder.chatBoxSecImage.setVisibility(View.INVISIBLE);
                }else{
                    Picasso.with(context).load(image)
                            .into(holder.chatBoxSecImage);
                    holder.chatBoxImage.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return chatModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView chatBoxText;
        public CircleImageView chatBoxImage;
        public CircleImageView chatBoxSecImage;
        public ImageView imageView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            chatBoxText = (TextView) itemView.findViewById(R.id.chatBoxText);
            chatBoxImage = (CircleImageView) itemView.findViewById(R.id.chatBoxImage);
            chatBoxSecImage = (CircleImageView) itemView.findViewById(R.id.chatBoxSecImage);
        }
    }
}
