package com.example.quicksmart;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class YourRideActivity extends AppCompatActivity {

    RecyclerView rvMyRides;
    RideAdapter adapter;
    List<RideModel> rideList = new ArrayList<>();
    TextView tabBooked, tabOffered;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_ride);

        db = FirebaseFirestore.getInstance();

        // Bottom Navigation Setup
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.getMenu().findItem(R.id.nav_rides).setChecked(true);
        NavHelper.setupNavigation(this, bottomNav);

        // Initialize UI Components
        tabBooked = findViewById(R.id.tabBooked);
        tabOffered = findViewById(R.id.tabOffered);
        rvMyRides = findViewById(R.id.rvMyRides);

        // Initialize RecyclerView
        rvMyRides.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RideAdapter(rideList);
        rvMyRides.setAdapter(adapter);

        // Default State: Show Offered Rides
        updateTabUI(false);
        OfferedRidesShow();

        tabBooked.setOnClickListener(v -> {
            updateTabUI(true);
            BookedRidesShow();
        });

        tabOffered.setOnClickListener(v -> {
            updateTabUI(false);
            OfferedRidesShow();
        });
    }

    private void updateTabUI(boolean isBookedSelected) {
        if (isBookedSelected) {
            tabBooked.setBackgroundResource(R.drawable.bg_tab_selected);
            tabBooked.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
            tabOffered.setBackgroundResource(R.drawable.bg_tab_unselected);
            tabOffered.setTextColor(ContextCompat.getColor(this, R.color.input_hint));
        } else {
            tabOffered.setBackgroundResource(R.drawable.bg_tab_selected);
            tabOffered.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
            tabBooked.setBackgroundResource(R.drawable.bg_tab_unselected);
            tabBooked.setTextColor(ContextCompat.getColor(this, R.color.input_hint));
        }
    }

    private void OfferedRidesShow() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();

        db.collection("Rides")
                .whereEqualTo("driverId", uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snapshot -> {
                    rideList.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        RideModel ride = doc.toObject(RideModel.class);
                        if (ride != null) {
                            rideList.add(ride);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    if (rideList.isEmpty()) {
                        Toast.makeText(this, "No offered rides found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void BookedRidesShow() {

    }
}
