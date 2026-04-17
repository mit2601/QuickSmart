package com.example.quicksmart;

import android.app.Activity;
import android.content.Intent;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class NavHelper {

    public static void setupNavigation(Activity activity, BottomNavigationView bottomNav) {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            
            // Check if we are already on the selected activity to avoid re-launching it
            if (id == R.id.nav_search && activity instanceof homeActivity) return true;
            if (id == R.id.nav_add && activity instanceof AddRideActivity) return true;

            if (id == R.id.nav_profile && activity instanceof ProfileActivity) return true;
            if (id == R.id.nav_rides && activity instanceof YourRideActivity) return true;

            Intent intent = null;

            if (id == R.id.nav_search) {
                intent = new Intent(activity, homeActivity.class);
                activity.finish();
            } else if (id == R.id.nav_add) {
                intent = new Intent(activity, AddRideActivity.class);
                activity.finish();
            }  else if (id == R.id.nav_profile) {
                intent = new Intent(activity, ProfileActivity.class);
                activity.finish();
            } else if (id == R.id.nav_rides) {
                intent = new Intent(activity, YourRideActivity.class);
                activity.finish();
            }

            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                activity.startActivity(intent);
                activity.overridePendingTransition(0, 0);
                activity.finish(); // Optional: finish the current activity to clean up the backstack
                return true;
            }

            return false;
        });
    }
}
