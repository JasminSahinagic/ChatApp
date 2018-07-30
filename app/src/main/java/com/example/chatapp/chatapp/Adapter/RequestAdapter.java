package com.example.chatapp.chatapp.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.chatapp.chatapp.Activity.ChatActivity;
import com.example.chatapp.chatapp.Activity.ProfileActivity;
import com.example.chatapp.chatapp.Model.UserModel;
import com.example.chatapp.chatapp.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.ViewHolder> {

    private List<UserModel> userModelList;
    private Context context;
    private AlertDialog.Builder builder;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private DatabaseReference databaseReference;

    public RequestAdapter(List<UserModel> userModelList, Context context) {
        this.userModelList = userModelList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_row,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel model = userModelList.get(position);
        holder.textViewName.setText(model.getName());
        holder.textViewStatus.setText(model.getStatus());
        holder.textViewStatus.setTextColor(Color.RED);
        if(!model.getImage().equals("default")){
            Picasso.with(context).load(model.getImage()).into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return userModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView imageView;
        public TextView textViewName;
        public TextView textViewStatus;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView = (CircleImageView) itemView.findViewById(R.id.usersImage);
            textViewName= (TextView) itemView.findViewById(R.id.textViewUsersName);
            textViewStatus= (TextView) itemView.findViewById(R.id.textViewUsersStatus);


            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialogBuilder();
                }
            });

        }

        public void dialogBuilder(){
            final UserModel model = userModelList.get(getAdapterPosition());
            CharSequence options[]= new CharSequence[]{"Open Profile", "Decline request"};
            builder = new AlertDialog.Builder(context);
            builder.setTitle("Select Options");
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    switch (i) {
                        case 0:
                            Intent intent = new Intent(context, ProfileActivity.class);
                            intent.putExtra("UserData", model);
                            context.startActivity(intent);
                            userModelList.clear();
                            break;
                        case 1:
                            databaseReference = FirebaseDatabase.getInstance().getReference().child("Friend_req");
                            databaseReference.child(user.getUid()).child(model.getUserID()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    databaseReference.child(model.getUserID()).child(user.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {

                                        }
                                    });
                                }
                            });
                            userModelList.clear();
                            break;
                    }
                }
            });
            builder.show();
        }
    }
}
