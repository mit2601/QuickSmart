package com.example.quicksmart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    ImageView splash;
    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        splash = findViewById(R.id.splashgif);
        Glide.with(SplashActivity.this).asGif().load(R.drawable.sedan).into(splash);

        mAuth = FirebaseAuth.getInstance();
        sp = getSharedPreferences(ConstantSp.PREF, MODE_PRIVATE);

        new Handler().postDelayed(this::checkUserSession, 2500);
    }

    private void checkUserSession() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            // Check the role saved during login
            String role = sp.getString(ConstantSp.ROLE, constants.ROLE_USER);

            if (constants.ROLE_ADMIN.equals(role)) {
                goToAdmin();
            } else {
                goToHome();
            }
        } else {
            goToLogin();
        }
    }

    private void goToAdmin() {
        Intent intent = new Intent(SplashActivity.this, AdminDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void goToHome() {
        Intent intent = new Intent(SplashActivity.this, homeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void goToLogin() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
