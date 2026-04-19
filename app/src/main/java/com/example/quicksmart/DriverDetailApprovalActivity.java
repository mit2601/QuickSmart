package com.example.quicksmart;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.FirebaseFirestore;

public class DriverDetailApprovalActivity extends AppCompatActivity {

    ImageView ivProfilePic, ivIdProof, btnBack;
    TextView tvName, tvEmail, tvPhone;
    MaterialButton btnApprove, btnReject;
    String uid;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_detail_approval);

        db = FirebaseFirestore.getInstance();
        uid = getIntent().getStringExtra("uid");

        initViews();
        loadDriverData();

        btnBack.setOnClickListener(v -> finish());
        btnApprove.setOnClickListener(v -> updateDriverStatus("verified"));
        btnReject.setOnClickListener(v -> updateDriverStatus("rejected"));
    }

    private void initViews() {
        ivProfilePic = findViewById(R.id.ivProfilePic);
        ivIdProof = findViewById(R.id.ivIdProof);
        btnBack = findViewById(R.id.btnBack);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        btnApprove = findViewById(R.id.btnApprove);
        btnReject = findViewById(R.id.btnReject);
    }

    private void loadDriverData() {
        if (uid == null) return;

        db.collection("Users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        usermodel user = documentSnapshot.toObject(usermodel.class);
                        if (user != null) {
                            tvName.setText(user.getName());
                            tvEmail.setText(user.getEmail());
                            tvPhone.setText(user.getPhn_no());

                            // Load Profile Pic
                            if (user.getProfilePic() != null && !user.getProfilePic().isEmpty()) {
                                decodeAndLoadImage(user.getProfilePic(), ivProfilePic, true);
                            }

                            // Load ID Proof
                            if (user.getIdProofBase64() != null && !user.getIdProofBase64().isEmpty()) {
                                decodeAndLoadImage(user.getIdProofBase64(), ivIdProof, false);
                            } else {
                                Toast.makeText(this, "ID Proof not found", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
    }

    private void decodeAndLoadImage(String base64Str, ImageView imageView, boolean circleCrop) {
        try {
            byte[] decodedString = Base64.decode(base64Str, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            if (circleCrop) {
                Glide.with(this).load(decodedByte).circleCrop().into(imageView);
            } else {
                Glide.with(this).load(decodedByte).into(imageView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateDriverStatus(String status) {
        db.collection("Users").document(uid)
                .update("driverStatus", status)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Driver " + status, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
