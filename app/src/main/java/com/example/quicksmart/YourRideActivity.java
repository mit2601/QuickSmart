package com.example.quicksmart;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class YourRideActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_your_ride);
        
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // First, set the correct item as selected WITHOUT triggering the listener
        bottomNav.getMenu().findItem(R.id.nav_rides).setChecked(true);

        // Then, set up the navigation listener
        NavHelper.setupNavigation(this, bottomNav);




    }
}
