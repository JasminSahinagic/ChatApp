package com.example.chatapp.chatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.chatapp.chatapp.Activity.ChatActivity;
import com.example.chatapp.chatapp.Application.UserOnlineStatus;
import com.example.chatapp.chatapp.Model.UserModel;
import com.example.chatapp.chatapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatFragmentAdapter extends RecyclerView.Adapter<ChatFragmentAdapter.ViewHolder> {


    private Context context;
    private List<UserModel> userModelList;
    private String temp;

    public ChatFragmentAdapter(Context context, List<UserModel> friendsAdapterList, String temp) {
        this.temp=temp;
        this.context = context;
        this.userModelList = friendsAdapterList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_row,parent,false);
        return new ViewHolder(view,context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel model = userModelList.get(position);
        UserModel friends = userModelList.get(position);
        holder.textViewName.setText(friends.getName());
        UserOnlineStatus onlineStatus = new UserOnlineStatus();
        holder.textViewStatus.setText(friends.getStatus());
        if(!friends.getImage().equals("default")){
            Picasso.with(context).load(friends.getImage()).into(holder.imageView);
        }
        if(temp.equals("true")){
            holder.imageViewUserOStatus.setVisibility(View.VISIBLE);
            holder.textViewStatus.setText("Online");
        }else{
            holder.imageViewUserOStatus.setVisibility(View.INVISIBLE);
            long longValue = Long.parseLong(temp);
            holder.textViewStatus.setText(onlineStatus.getTimeAgo(longValue,context).toString());
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
        public ImageView imageViewUserOStatus;

        public ViewHolder(@NonNull View itemView, final Context context) {
            super(itemView);
            imageViewUserOStatus = (ImageView) itemView.findViewById(R.id.imageViewOStatus);
            imageView = (CircleImageView) itemView.findViewById(R.id.usersImage);
            textViewName= (TextView) itemView.findViewById(R.id.textViewUsersName);
            textViewStatus= (TextView) itemView.findViewById(R.id.textViewUsersStatus);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UserModel model = userModelList.get(getAdapterPosition());
                    Intent intentSec = new Intent(context, ChatActivity.class);
                    intentSec.putExtra("userID", model);
                    context.startActivity(intentSec);
                    userModelList.clear();
                }
            });

        }
    }
}
