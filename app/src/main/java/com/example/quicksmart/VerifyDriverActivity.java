package com.example.quicksmart;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class VerifyDriverActivity extends AppCompatActivity {

    ImageView ivIdPreview, btnBack;
    MaterialButton btnChooseFile, btnSubmitVerification;
    TextView txtUploadTitle, txtUploadDesc;
    
    FirebaseFirestore db;
    String encodedIdProof = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_driver);

        db = FirebaseFirestore.getInstance();

        initViews();

        btnBack.setOnClickListener(v -> finish());
        btnChooseFile.setOnClickListener(v -> pickIdPhoto());
        btnSubmitVerification.setOnClickListener(v -> submitForVerification());
    }

    private void initViews() {
        ivIdPreview = findViewById(R.id.ivIdPreview);
        btnBack = findViewById(R.id.btnBack);
        btnChooseFile = findViewById(R.id.btnChooseFile);
        btnSubmitVerification = findViewById(R.id.btnSubmitVerification);
        txtUploadTitle = findViewById(R.id.txtUploadTitle);
        txtUploadDesc = findViewById(R.id.txtUploadDesc);
    }

    private void pickIdPhoto() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, 102);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 102 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                
                // Show preview and update UI labels
                ivIdPreview.setImageBitmap(bitmap);
                ivIdPreview.setAlpha(1.0f);
                txtUploadTitle.setText("Document Selected");
                txtUploadDesc.setText("Click the button below to submit your verification request.");
                
                // Encode to Base64 (using 400px width for clarity)
                encodedIdProof = encodeImage(bitmap);
                
                // Enable submission
                btnSubmitVerification.setEnabled(true);
                
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 400;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private void submitForVerification() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        btnSubmitVerification.setEnabled(false);
        btnSubmitVerification.setText("Submitting...");

        Map<String, Object> updates = new HashMap<>();
        updates.put("driverStatus", "pending");
        updates.put("idProofBase64", encodedIdProof);

        db.collection("Users").document(uid).update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Verification Request Submitted Successfully!", Toast.LENGTH_LONG).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    btnSubmitVerification.setEnabled(true);
                    btnSubmitVerification.setText("Submit for Verification");
                    Toast.makeText(this, "Submission failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
