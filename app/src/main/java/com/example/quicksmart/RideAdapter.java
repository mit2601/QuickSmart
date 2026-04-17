package com.example.quicksmart;

import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class RideAdapter extends RecyclerView.Adapter<RideAdapter.ViewHolder> {

    private List<RideModel> rideList;
    private String viewType = "search"; // Default type

    public RideAdapter(List<RideModel> rideList) {
        this.rideList = rideList;
    }

    public void setViewType(String viewType) {
        this.viewType = viewType;
    }

    // Method to update list from homeActivity search
    public void updateList(List<RideModel> newList) {
        this.rideList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ride, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RideModel ride = rideList.get(position);
        holder.tvFrom.setText(ride.from);
        holder.tvTo.setText(ride.to);
        holder.tvDate.setText(ride.date);
        holder.tvPrice.setText("₹" + ride.price);
        holder.tvSeats.setText(String.valueOf(ride.seats) + " seats");
        holder.tvDriverName.setText(ride.driverName);

        // 🔥 BIND RIDE STATUS WITH COLOR CODING
        if ("offered".equals(viewType) || "booked".equals(viewType)) {
            holder.txtRideStatus.setVisibility(View.VISIBLE);
            String status = ride.status != null ? ride.status : "pending";
            holder.txtRideStatus.setText(status.toUpperCase());

            if ("approved".equalsIgnoreCase(status)) {
                holder.txtRideStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
            } else if ("pending".equalsIgnoreCase(status)) {
                holder.txtRideStatus.setTextColor(Color.parseColor("#FF9800")); // Orange
            } else if ("rejected".equalsIgnoreCase(status)) {
                holder.txtRideStatus.setTextColor(Color.parseColor("#F44336")); // Red
            } else {
                holder.txtRideStatus.setTextColor(Color.GRAY);
            }
        } else {
            holder.txtRideStatus.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), RideDetailActivity.class);

            intent.putExtra("from", ride.from);
            intent.putExtra("to", ride.to);
            intent.putExtra("driverName", ride.driverName);
            intent.putExtra("date", ride.date);
            intent.putExtra("price", ride.price);
            intent.putExtra("seats", ride.seats);
            intent.putExtra("timestamp", ride.timestamp);
            intent.putExtra("driverid", ride.driverId);
            intent.putExtra("route", ride.route);
            intent.putExtra("rideId", ride.rideId);
            intent.putExtra("type", viewType); // Pass the current view type

            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return rideList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvFrom, tvTo, tvDate, tvPrice, tvSeats, tvDriverName, txtRideStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvFrom = itemView.findViewById(R.id.tvFrom);
            tvTo = itemView.findViewById(R.id.tvTo);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvSeats = itemView.findViewById(R.id.tvSeats);
            tvDriverName = itemView.findViewById(R.id.tvDriverName);
            txtRideStatus = itemView.findViewById(R.id.txtRideStatus);
        }
    }
}
