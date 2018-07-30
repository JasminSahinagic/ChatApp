package com.example.chatapp.chatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.chatapp.chatapp.Activity.ProfileActivity;
import com.example.chatapp.chatapp.Model.UserModel;
import com.example.chatapp.chatapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.ViewHolder> {

    Context context;
    List<UserModel> modelList;

    public UsersAdapter(Context context, List<UserModel> modelList) {
        this.context = context;
        this.modelList = modelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.users_row,parent,false);
        return new ViewHolder(view,context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel model = modelList.get(position);
        holder.textViewName.setText(model.getName());
        holder.textViewStatus.setText(model.getStatus());
        if(!model.getImage().equals("default")){
            Picasso.with(context).load(model.getImage()).into(holder.imageView);
        }


    }

    @Override
    public int getItemCount() {
        return modelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public CircleImageView imageView;
        public TextView textViewName;
        public TextView textViewStatus;

        public ViewHolder(@NonNull View itemView, final Context context) {
            super(itemView);
            imageView = (CircleImageView) itemView.findViewById(R.id.usersImage);
            textViewName= (TextView) itemView.findViewById(R.id.textViewUsersName);
            textViewStatus= (TextView) itemView.findViewById(R.id.textViewUsersStatus);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UserModel model = modelList.get(getAdapterPosition());
                    Intent intent = new Intent(context, ProfileActivity.class);
                    intent.putExtra("UserData", model);
                    context.startActivity(intent);
                    modelList.clear();
                }
            });
        }
    }
}
