package com.example.quicksmart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    EditText etUserName, etPhone;
    TextView tvUserStatus, tvEmail;
    ImageView ivVerifiedBadge, btnEditProfile, ivProfilePic;
    View btnChangePic, layoutAccountActions;
    MaterialButton btnLogout, btnVerifyDriver, btnMyHistory, btnSaveProfile;
    
    FirebaseFirestore db;
    SharedPreferences sp;
    boolean isEditMode = false;
    Uri imageUri;
    String encodedImage = null; // Stores Base64 string

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();
        sp = getSharedPreferences(ConstantSp.PREF, MODE_PRIVATE);

        initViews();
        setupBottomNav();
        loadUserData();

        btnEditProfile.setOnClickListener(v -> toggleEditMode());
        btnSaveProfile.setOnClickListener(v -> saveProfileData());
        btnChangePic.setOnClickListener(v -> pickImage());
        
        btnLogout.setOnClickListener(v -> performLogout());
        btnVerifyDriver.setOnClickListener(v -> {
             // For demo, we will use the same Base64 logic in VerifyDriverActivity later
             startActivity(new Intent(ProfileActivity.this, VerifyDriverActivity.class));
        });

        btnMyHistory.setOnClickListener(v -> {
            startActivity(new Intent(ProfileActivity.this, YourRideActivity.class));
        });
    }

    private void initViews() {
        etUserName = findViewById(R.id.etUserName);
        tvUserStatus = findViewById(R.id.tvUserStatus);
        tvEmail = findViewById(R.id.tvEmail);
        etPhone = findViewById(R.id.etPhone);
        ivVerifiedBadge = findViewById(R.id.ivVerifiedBadge);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        ivProfilePic = findViewById(R.id.ivProfilePic);
        btnChangePic = findViewById(R.id.btnChangePic);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        layoutAccountActions = findViewById(R.id.layoutAccountActions);
        
        btnLogout = findViewById(R.id.btnLogout);
        btnVerifyDriver = findViewById(R.id.btnVerifyDriver);
        btnMyHistory = findViewById(R.id.btnMyHistory);
    }

    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.getMenu().findItem(R.id.nav_profile).setChecked(true);
        NavHelper.setupNavigation(this, bottomNav);
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        db.collection("Users").document(user.getUid()).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        usermodel u = doc.toObject(usermodel.class);
                        if (u != null) {
                            etUserName.setText(u.getName());
                            tvEmail.setText(u.getEmail());
                            etPhone.setText(u.getPhn_no());
                            updateVerificationUI(u.getDriverStatus());
                            
                            // 🔥 Load Base64 Image string from Firestore
                            String profileData = u.getProfilePic();
                            if (profileData != null && !profileData.isEmpty()) {
                                byte[] decodedString = Base64.decode(profileData, Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                Glide.with(this).load(decodedByte).circleCrop().into(ivProfilePic);
                            }
                        }
                    }
                });
    }

    private void toggleEditMode() {
        isEditMode = !isEditMode;
        etUserName.setEnabled(isEditMode);
        etPhone.setEnabled(isEditMode);
        
        if (isEditMode) {
            etUserName.requestFocus();
            if (etUserName.getText() != null) {
                etUserName.setSelection(etUserName.getText().length());
            }
            btnEditProfile.setAlpha(0.5f);
        } else {
            btnEditProfile.setAlpha(1.0f);
        }

        btnChangePic.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        btnSaveProfile.setVisibility(isEditMode ? View.VISIBLE : View.GONE);
        layoutAccountActions.setVisibility(isEditMode ? View.GONE : View.VISIBLE);
    }

    private void pickImage() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            try {
                // Convert Uri to Base64 String
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                encodedImage = encodeImage(bitmap);
                
                // Show preview immediately
                Glide.with(this).load(bitmap).circleCrop().into(ivProfilePic);
                Toast.makeText(this, "Image selected locally", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Helper to convert bitmap to small Base64 string
    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private void saveProfileData() {
        String newName = etUserName.getText().toString().trim();
        String newPhone = etPhone.getText().toString().trim();

        if (newName.isEmpty() || newPhone.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", newName);
        updates.put("phn_no", newPhone);
        
        // 🔥 If a new image was selected, add the Base64 string to Firestore
        if (encodedImage != null) {
            updates.put("profilePic", encodedImage);
        }

        btnSaveProfile.setEnabled(false);
        btnSaveProfile.setText("Saving...");

        db.collection("Users").document(user.getUid()).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                    sp.edit().putString(ConstantSp.NAME, newName).apply();
                    sp.edit().putString(ConstantSp.PHN_NO, newPhone).apply();
                    btnSaveProfile.setEnabled(true);
                    btnSaveProfile.setText("Save Changes");
                    toggleEditMode();
                })
                .addOnFailureListener(e -> {
                    btnSaveProfile.setEnabled(true);
                    btnSaveProfile.setText("Save Changes");
                    Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateVerificationUI(String status) {
        if ("verified".equals(status)) {
            tvUserStatus.setText("Verified Driver");
            tvUserStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            ivVerifiedBadge.setVisibility(View.VISIBLE);
            btnVerifyDriver.setVisibility(View.GONE);
        } else if ("pending".equals(status)) {
            tvUserStatus.setText("Verification Pending");
            tvUserStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
            btnVerifyDriver.setEnabled(false);
            btnVerifyDriver.setText("Verification in Progress");
        } else {
            tvUserStatus.setText("Unverified Account");
            ivVerifiedBadge.setVisibility(View.GONE);
            btnVerifyDriver.setVisibility(View.VISIBLE);
            btnVerifyDriver.setText("Become a Verified Driver");
        }
    }

    private void performLogout() {
        FirebaseAuth.getInstance().signOut();
        sp.edit().clear().apply();
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
