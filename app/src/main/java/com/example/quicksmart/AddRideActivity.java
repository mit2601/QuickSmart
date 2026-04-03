package com.example.quicksmart;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddRideActivity extends AppCompatActivity {
    EditText Cleave, Cgoing, Cdate, Cpassengers, Cprice;
    Button btncreate;
    long selectedTimestamp = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ride);
        Cleave = findViewById(R.id.Cleave);
        Cgoing = findViewById(R.id.Cgoing);
        Cdate = findViewById(R.id.Cdate);
        Cpassengers = findViewById(R.id.Cpassengers);
        Cprice = findViewById(R.id.Cprice);
        btncreate = findViewById(R.id.btncreate);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.getMenu().findItem(R.id.nav_add).setChecked(true);
        NavHelper.setupNavigation(this, bottomNav);

        Cdate.setOnClickListener(v -> {
            // Constraints to allow only today and future dates
            CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
            constraintsBuilder.setValidator(DateValidatorPointForward.now());

            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Date")
                    .setCalendarConstraints(constraintsBuilder.build())
                    .build();

            datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
            datePicker.addOnPositiveButtonClickListener(selection -> {
                selectedTimestamp = selection;
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                String formattedDate = sdf.format(new Date(selection));
                Cdate.setText(formattedDate);
            });
        });

        btncreate.setOnClickListener(v -> createRide());
    }

    private void createRide() {
        String fromText = Cleave.getText().toString().trim();
        String toText = Cgoing.getText().toString().trim();
        String dateText = Cdate.getText().toString().trim();
        String seatsText = Cpassengers.getText().toString().trim();
        String priceText = Cprice.getText().toString().trim();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        if (fromText.isEmpty() || toText.isEmpty() || dateText.isEmpty() || seatsText.isEmpty() || priceText.isEmpty()) {
            Toast.makeText(this, "Fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            int seats = Integer.parseInt(seatsText);
            int price = Integer.parseInt(priceText);

            String route = fromText.toLowerCase().replace(" ", "") + "_" +
                    toText.toLowerCase().replace(" ", "");

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // FIX: Use DocumentReference instead of String for the reference
            DocumentReference rideref = db.collection("Rides").document();
            String rideId = rideref.getId();

            // Get driver details from SharedPreferences
            SharedPreferences sp = getSharedPreferences(ConstantSp.PREF, MODE_PRIVATE);
            String driverName = sp.getString(ConstantSp.NAME, "Driver");

            // Create a new ride object
            Map<String, Object> ride = new HashMap<>();
            ride.put("rideId", rideId);
            ride.put("driverId", user.getUid());
            ride.put("driverName", driverName);
            ride.put("from", fromText);
            ride.put("to", toText);
            ride.put("route", route);
            ride.put("date", dateText);
            ride.put("timestamp", selectedTimestamp);
            ride.put("seats", seats);
            ride.put("price", price);
            ride.put("createdAt", System.currentTimeMillis());

            rideref.set(ride).addOnSuccessListener(aVoid -> {
                Toast.makeText(AddRideActivity.this, "Ride Created Successfully!", Toast.LENGTH_SHORT).show();
                finish(); // Go back after success
            }).addOnFailureListener(e -> {
                Toast.makeText(AddRideActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Enter valid numbers for seats and price", Toast.LENGTH_SHORT).show();
        }
    }
}
