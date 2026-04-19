package com.example.quicksmart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class DriverApprovalAdapter extends RecyclerView.Adapter<DriverApprovalAdapter.ViewHolder> {

    private List<usermodel> driverList;
    private OnDriverClickListener listener;

    public interface OnDriverClickListener {
        void onDriverClick(usermodel user);
    }

    public DriverApprovalAdapter(List<usermodel> driverList, OnDriverClickListener listener) {
        this.driverList = driverList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_driver_approval, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        usermodel user = driverList.get(position);
        holder.tvName.setText(user.getName());
        holder.tvEmail.setText(user.getEmail());

        // Load profile pic if exists
        if (user.getProfilePic() != null && !user.getProfilePic().isEmpty()) {
            // Check if it's a URL or Base64 (demo logic used Base64)
            if (user.getProfilePic().length() > 500) {
                // Likely Base64
                byte[] decodedString = android.util.Base64.decode(user.getProfilePic(), android.util.Base64.DEFAULT);
                android.graphics.Bitmap decodedByte = android.graphics.BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                Glide.with(holder.itemView.getContext()).load(decodedByte).circleCrop().into(holder.ivProfilePic);
            } else {
                Glide.with(holder.itemView.getContext()).load(user.getProfilePic()).circleCrop().into(holder.ivProfilePic);
            }
        }

        holder.itemView.setOnClickListener(v -> listener.onDriverClick(user));
    }

    @Override
    public int getItemCount() {
        return driverList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfilePic;
        TextView tvName, tvEmail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfilePic = itemView.findViewById(R.id.ivProfilePic);
            tvName = itemView.findViewById(R.id.tvName);
            tvEmail = itemView.findViewById(R.id.tvEmail);
        }
    }
}
