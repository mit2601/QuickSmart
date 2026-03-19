package com.example.quicksmart;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class InboxActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inbox);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // First, set the correct item as selected WITHOUT triggering the listener
        bottomNav.getMenu().findItem(R.id.nav_inbox).setChecked(true);

        // Then, set up the navigation listener
        NavHelper.setupNavigation(this, bottomNav);
    }
}
