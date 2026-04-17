package com.example.quicksmart;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class RideDetailActivity extends AppCompatActivity {

    TextView tvFrom, tvTo, tvDriver, tvDate, tvPrice, tvSeats, cancel, tvPriceLabel;
    MaterialButton btnBook, btnApprove, btnReject;
    View contactDriverBox, layoutStandardActions, layoutAdminApprovalActions;
    String from, to, driverName, date, driverId, route, rideId, type;
    int price, seats;
    long timestamp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_detail);

        // Initialize Views
        tvFrom = findViewById(R.id.tvFrom);
        tvTo = findViewById(R.id.tvTo);
        tvDriver = findViewById(R.id.tvDriver);
        tvDate = findViewById(R.id.tvDate);
        tvPrice = findViewById(R.id.tvPrice);
        tvSeats = findViewById(R.id.tvSeats);
        btnBook = findViewById(R.id.btnBook);
        cancel = findViewById(R.id.cancel);
        contactDriverBox = findViewById(R.id.contactDriverBox);
        tvPriceLabel = findViewById(R.id.tvPriceLabel);

        // Admin Specific Views
        btnApprove = findViewById(R.id.btnApprove);
        btnReject = findViewById(R.id.btnReject);
        layoutStandardActions = findViewById(R.id.layoutStandardActions);
        layoutAdminApprovalActions = findViewById(R.id.layoutAdminApprovalActions);

        Intent intent = getIntent();
        if (intent == null) {
            Toast.makeText(this, "FAILED TO GET DATA", Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else {
            from = intent.getStringExtra("from");
            to = intent.getStringExtra("to");
            driverName = intent.getStringExtra("driverName");
            date = intent.getStringExtra("date");
            price = intent.getIntExtra("price", 0);
            seats = intent.getIntExtra("seats", 0);
            driverId = intent.getStringExtra("driverid");
            route = intent.getStringExtra("route");
            timestamp = intent.getLongExtra("timestamp", 0);
            rideId = intent.getStringExtra("rideId");
            type = intent.getStringExtra("type");

            tvFrom.setText(from);
            tvTo.setText(to);
            tvDriver.setText(driverName);
            tvDate.setText(date);
            tvPrice.setText("₹" + price);
            tvSeats.setText(seats + " Seats");
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUid = (currentUser != null) ? currentUser.getUid() : "";

        // UI Logic based on Type
        if ("admin_approval".equals(type)) {
            // ADMIN MODE: Approve/Reject a pending ride
            layoutStandardActions.setVisibility(View.GONE);
            layoutAdminApprovalActions.setVisibility(View.VISIBLE);
            tvSeats.setText(seats + " Seats Offered");
        } 
        else if ("booked".equals(type)) {
            // BOOKED MODE: View details of a joined ride
            tvSeats.setVisibility(View.GONE);
            layoutStandardActions.setVisibility(View.GONE);
            contactDriverBox.setVisibility(View.VISIBLE);
            if (tvPriceLabel != null) tvPriceLabel.setText("Total Paid");
        } 
        else if (currentUid.equals(driverId)) {
            // DRIVER MODE: Owner viewing their own offer
            btnBook.setVisibility(View.GONE);
            tvSeats.setText(seats + " Seats Available");
        } 
        else {
            // USER MODE: Standard booking view
            layoutStandardActions.setVisibility(View.VISIBLE);
            layoutAdminApprovalActions.setVisibility(View.GONE);
            tvSeats.setText(seats + " Seats Available");
        }

        // --- BUTTON CLICK LISTENERS ---

        btnBook.setOnClickListener(v -> {
            Intent bookingIntent = new Intent(RideDetailActivity.this, BookingActivity.class);
            bookingIntent.putExtra("from", from);
            bookingIntent.putExtra("to", to);
            bookingIntent.putExtra("driverName", driverName);
            bookingIntent.putExtra("date", date);
            bookingIntent.putExtra("price", price);
            bookingIntent.putExtra("seats", seats);
            bookingIntent.putExtra("driverid", driverId);
            bookingIntent.putExtra("route", route);
            bookingIntent.putExtra("timestamp", timestamp);
            bookingIntent.putExtra("rideId", rideId);
            startActivity(bookingIntent);
        });

        btnApprove.setOnClickListener(v -> updateRideStatus(constants.STATUS_APPROVED));
        btnReject.setOnClickListener(v -> updateRideStatus(constants.STATUS_REJECTED));

        cancel.setOnClickListener(v -> finish());
        
        contactDriverBox.setOnClickListener(v -> {
            Toast.makeText(this, "Opening contact options for " + driverName, Toast.LENGTH_SHORT).show();
        });
    }

    private void updateRideStatus(String status) {
        if (rideId == null) return;

        FirebaseFirestore.getInstance().collection("Rides").document(rideId)
                .update(constants.FIELD_STATUS, status)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Ride " + status, Toast.LENGTH_SHORT).show();
                    finish(); // Go back to the pending list
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
