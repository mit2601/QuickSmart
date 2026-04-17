package com.example.quicksmart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingActivity extends AppCompatActivity {
    TextView tvFrom, tvTo, tvDriver, tvDate, tvPrice, tvAvailableSeats;
    Spinner spinnerSeats;
    String from, to, driverName, date, driverid, route, rideId;
    int pricePerSeat, totalAvailableSeats, selectedSeatsCount = 1;
    long timestamp;
    MaterialButton btnbook;
    TextView btncancel;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        db = FirebaseFirestore.getInstance();

        // Initialize Views
        tvFrom = findViewById(R.id.txtFrom);
        tvTo = findViewById(R.id.txtTo);
        tvDriver = findViewById(R.id.txtDriver);
        tvDate = findViewById(R.id.txtDate);
        tvPrice = findViewById(R.id.txtPrice);
        tvAvailableSeats = findViewById(R.id.txtAvailableSeats);
        spinnerSeats = findViewById(R.id.spinnerSeats);
        btnbook = findViewById(R.id.btnbook);
        btncancel = findViewById(R.id.btncancel);

        // Get data from Intent
        Intent intent = getIntent();
        if (intent == null) {
            Toast.makeText(this, "FAILED TO GET DATA", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        from = intent.getStringExtra("from");
        to = intent.getStringExtra("to");
        driverName = intent.getStringExtra("driverName");
        date = intent.getStringExtra("date");
        pricePerSeat = intent.getIntExtra("price", 0);
        totalAvailableSeats = intent.getIntExtra("seats", 0);
        driverid = intent.getStringExtra("driverid");
        route = intent.getStringExtra("route");
        timestamp = intent.getLongExtra("timestamp", 0);
        rideId = intent.getStringExtra("rideId");

        // Set Basic Text
        tvFrom.setText(from);
        tvTo.setText(to);
        tvDriver.setText(driverName);
        tvDate.setText(date);
        tvAvailableSeats.setText(totalAvailableSeats + " available");

        // Setup Seat Spinner
        setupSeatSpinner();

        btnbook.setOnClickListener(v -> bookRide());
        btncancel.setOnClickListener(v -> finish());
    }

    private void setupSeatSpinner() {
        List<Integer> seatOptions = new ArrayList<>();
        for (int i = 1; i <= totalAvailableSeats; i++) {
            seatOptions.add(i);
        }

        ArrayAdapter<Integer> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, seatOptions);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSeats.setAdapter(adapter);

        spinnerSeats.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSeatsCount = (int) parent.getItemAtPosition(position);
                // Update Total Price
                tvPrice.setText("₹" + (pricePerSeat * selectedSeatsCount));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void bookRide() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Please Login First", Toast.LENGTH_SHORT).show();
            return;
        }

        if (totalAvailableSeats < selectedSeatsCount) {
            Toast.makeText(this, "Not enough seats available", Toast.LENGTH_SHORT).show();
            return;
        }

        SharedPreferences sp = getSharedPreferences(ConstantSp.PREF, MODE_PRIVATE);
        String passengerName = sp.getString(ConstantSp.NAME, "Passenger");
        String passengerId = currentUser.getUid();

        Map<String, Object> booking = new HashMap<>();
        booking.put("rideId", rideId);
        booking.put("passengerId", passengerId);
        booking.put("passengerName", passengerName);
        booking.put("driverId", driverid);
        booking.put("driverName", driverName);
        booking.put("from", from);
        booking.put("to", to);
        booking.put("date", date);
        booking.put("bookedSeats", selectedSeatsCount);
        booking.put("totalPrice", pricePerSeat * selectedSeatsCount);
        booking.put("timestamp", timestamp);
        booking.put("bookingTime", System.currentTimeMillis());
        booking.put("status", "Booked");

        db.collection("Bookings").add(booking)
                .addOnSuccessListener(doc -> updateRideSeats())
                .addOnFailureListener(e -> Toast.makeText(this, "Booking Failed", Toast.LENGTH_SHORT).show());
    }

    private void updateRideSeats() {
        if (rideId == null) {
            Toast.makeText(this, "Booking Confirmed", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        int remainingSeats = totalAvailableSeats - selectedSeatsCount;

        db.collection("Rides").document(rideId)
                .update("seats", remainingSeats)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Booking Confirmed!", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(BookingActivity.this, homeActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Seat update failed", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }
}
