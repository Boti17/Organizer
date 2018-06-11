package com.example.szabo.organizer;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import java.util.ArrayList;

public class SubscribedUsersAdapter extends RecyclerView.Adapter<SubscribedUsersAdapter.ViewHolder>{

    private Context context;
    private ArrayList<User> items;

    public SubscribedUsersAdapter(Context context, ArrayList<User> items) {
        this.context = context;
        this.items = items;
    }

    protected static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mProfilePictureView;
        public TextView mUserName;

        public ViewHolder(View itemView) {
            super(itemView);
            mProfilePictureView = (ImageView) itemView.findViewById(R.id.profilePictureView);
            mUserName = (TextView) itemView.findViewById(R.id.userName);
        }
    }

    @Override
    public SubscribedUsersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_user, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        final User item = items.get(position);
        Glide
                .with(context)
                .load(item.getPicture())
                .into(holder.mProfilePictureView);
        holder.mUserName.setText(item.getName());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileIntent = new Intent(context, ProfileActivity.class);
                profileIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                profileIntent.putExtra("userId", item.getUserId());
                context.startActivity(profileIntent);
            }
            });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
