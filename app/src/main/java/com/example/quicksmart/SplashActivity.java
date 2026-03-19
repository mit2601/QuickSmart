package com.example.quicksmart;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.os.Handler;
import android.os.Looper;



import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    ImageView splash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        splash=findViewById(R.id.splashgif);
        Glide.with(SplashActivity.this).asGif().load(R.drawable.sedan).into(splash);

        mAuth = FirebaseAuth.getInstance();


        new Handler().postDelayed(this::checkUserSession, 2500);
    }

    private void checkUserSession() {

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            user.getIdToken(true).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {

                    goToHome();
                } else {
                    mAuth.signOut();
                    goToLogin();
                }

            });

        } else {
            goToLogin();
        }
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