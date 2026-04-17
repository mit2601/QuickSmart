package com.example.quicksmart;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ApproveRidesActivity extends AppCompatActivity {

    RecyclerView rvApproveRides;
    RideAdapter adapter;
    List<RideModel> rideList = new ArrayList<>();
    FirebaseFirestore db;
    ImageView btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_rides);

        db = FirebaseFirestore.getInstance();
        rvApproveRides = findViewById(R.id.rvApproveRides);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        rvApproveRides.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RideAdapter(rideList);
        // 🔥 Set view type to admin_approval so RideDetailActivity knows to show Approve/Reject buttons
        adapter.setViewType("admin_approval");
        rvApproveRides.setAdapter(adapter);

        fetchPendingRides();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh the list whenever returning to this activity
        fetchPendingRides();
    }

    private void fetchPendingRides() {
        db.collection("Rides")
                .whereEqualTo(constants.FIELD_STATUS, constants.STATUS_PENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    rideList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        RideModel ride = doc.toObject(RideModel.class);
                        if (ride != null) {
                            rideList.add(ride);
                        }
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching rides: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
