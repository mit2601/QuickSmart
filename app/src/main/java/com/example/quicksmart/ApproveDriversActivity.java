package com.example.quicksmart;

import android.content.Intent;
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

public class ApproveDriversActivity extends AppCompatActivity {

    RecyclerView rvApproveDrivers;
    ImageView btnBack;
    FirebaseFirestore db;
    DriverApprovalAdapter adapter;
    List<usermodel> driverList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_approve_drivers);

        db = FirebaseFirestore.getInstance();
        rvApproveDrivers = findViewById(R.id.rvApproveDrivers);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());

        rvApproveDrivers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DriverApprovalAdapter(driverList, user -> {
            // When a driver is clicked, open detail view for approval
            Intent intent = new Intent(this, DriverDetailApprovalActivity.class);
            intent.putExtra("uid", user.getUid()); // Passing the correct UID
            startActivity(intent);
        });
        rvApproveDrivers.setAdapter(adapter);

        fetchPendingDrivers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchPendingDrivers(); // Refresh list when coming back
    }

    private void fetchPendingDrivers() {
        db.collection("Users")
                .whereEqualTo("driverStatus", "pending")
                .get()
                .addOnSuccessListener(snapshot -> {
                    driverList.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        usermodel user = doc.toObject(usermodel.class);
                        if (user != null) {
                            user.setUid(doc.getId()); // Store document ID as UID
                            driverList.add(user);
                        }
                    }
                    adapter.notifyDataSetChanged();
                    if (driverList.isEmpty()) {
                        Toast.makeText(this, "No pending driver requests", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
