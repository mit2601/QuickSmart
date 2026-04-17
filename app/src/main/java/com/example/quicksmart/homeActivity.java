package com.example.quicksmart;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class homeActivity extends AppCompatActivity {

    EditText etFrom, etTo, etPassengers, etDate;
    RecyclerView rvRides;

    RideAdapter adapter;
    List<RideModel> rideList = new ArrayList<>();

    FirebaseFirestore db;

    long selectedTimestamp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Bottom Nav
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.getMenu().findItem(R.id.nav_search).setChecked(true);
        NavHelper.setupNavigation(this, bottomNav);

        // Init Views
        etFrom = findViewById(R.id.etFrom);
        etTo = findViewById(R.id.etTo);
        etPassengers = findViewById(R.id.etPassengers);
        etDate = findViewById(R.id.etDate);
        rvRides = findViewById(R.id.rvRides);

        // RecyclerView
        rvRides.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RideAdapter(rideList);
        rvRides.setAdapter(adapter);

        // Firestore
        db = FirebaseFirestore.getInstance();

        // Date Picker
        etDate.setOnClickListener(v -> {
            // Constraints to allow only today and future dates
            CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
            constraintsBuilder.setValidator(DateValidatorPointForward.now());

            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Date")
                    .setCalendarConstraints(constraintsBuilder.build())
                    .build();

            picker.show(getSupportFragmentManager(), "DATE");

            picker.addOnPositiveButtonClickListener(selection -> {
                selectedTimestamp = selection;

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                etDate.setText(sdf.format(new Date(selection)));
            });
        });

        // Search Button
        findViewById(R.id.btnSearch).setOnClickListener(v -> searchRides());
    }

    private void searchRides() {
        String from = etFrom.getText().toString().trim();
        String to = etTo.getText().toString().trim();
        String passengers = etPassengers.getText().toString().trim();
        String dateStr = etDate.getText().toString().trim();

        if (from.isEmpty() || to.isEmpty()) {
            Toast.makeText(this, "Enter locations", Toast.LENGTH_SHORT).show();
            return;
        }

        int requiredSeats = passengers.isEmpty() ? 1 : Integer.parseInt(passengers);
        String route = from.toLowerCase().replace(" ", "") + "_" + to.toLowerCase().replace(" ", "");

        // Determine the start time for the filter: use selected date if available, otherwise "now"
        long currentFilterDate = (dateStr.isEmpty()) ? 0 : selectedTimestamp;

        // Get current user ID to exclude their own rides
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String currentUid = (currentUser != null) ? currentUser.getUid() : "";

        // Fetch rides by route from "Rides" collection
        db.collection("Rides")
                .whereEqualTo("route", route)
                .get()
                .addOnSuccessListener(snapshot -> {
                    rideList.clear();
                    long now = System.currentTimeMillis();

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        RideModel ride = doc.toObject(RideModel.class);
                        if (ride == null) continue;

                        // Exclude rides created by the current user
                        if (ride.driverId != null && !ride.driverId.equals(currentUid)) {
                            if (ride.seats >= requiredSeats) {
                                long effectiveStart = Math.max(currentFilterDate, now - 3600000);

                                if (ride.timestamp >= effectiveStart) {
                                    rideList.add(ride);
                                }
                            }
                        }
                    }

                    // Multi-level sorting: Date (Earliest first), Price (Cheapest first), Seats (Most available first)
                    Collections.sort(rideList, (r1, r2) -> {
                        int dateComp = Long.compare(r1.timestamp, r2.timestamp);
                        if (dateComp != 0) return dateComp;

                        int priceComp = Integer.compare(r1.price, r2.price);
                        if (priceComp != 0) return priceComp;

                        return Integer.compare(r2.seats, r1.seats);
                    });

                    adapter.notifyDataSetChanged();

                    if (rideList.isEmpty()) {
                        Toast.makeText(this, "No matching rides found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
