package com.example.quicksmart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class AdminDashboardActivity extends AppCompatActivity {

    TextView tvPendingRidesCount, tvTotalUsersCount;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = FirebaseFirestore.getInstance();

        tvPendingRidesCount = findViewById(R.id.tvPendingRidesCount);
        tvTotalUsersCount = findViewById(R.id.tvTotalUsersCount);

        fetchStats();

        findViewById(R.id.btnApproveRides).setOnClickListener(v -> {
            startActivity(new Intent(this, ApproveRidesActivity.class));
        });

        findViewById(R.id.btnApproveDrivers).setOnClickListener(v -> {
             startActivity(new Intent(this, ApproveDriversActivity.class));
        });

        findViewById(R.id.btnManageUsers).setOnClickListener(v -> {
            Toast.makeText(this, "User management coming soon", Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            SharedPreferences sp = getSharedPreferences(ConstantSp.PREF, MODE_PRIVATE);
            sp.edit().clear().apply();
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchStats(); 
    }

    private void fetchStats() {
        db.collection("Rides")
                .whereEqualTo(constants.FIELD_STATUS, constants.STATUS_PENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tvPendingRidesCount.setText(String.valueOf(queryDocumentSnapshots.size()));
                });

        db.collection("Users")
                .whereEqualTo(constants.FIELD_ROLE, constants.ROLE_USER)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    tvTotalUsersCount.setText(String.valueOf(queryDocumentSnapshots.size()));
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching stats", Toast.LENGTH_SHORT).show();
                });
    }
}
