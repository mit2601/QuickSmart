package com.example.quicksmart;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class homeActivity extends AppCompatActivity {

    EditText etFrom, etTo, etPassengers, etDate;
    RecyclerView rvRides, recyclerSuggestions;
    TextView tvResultsHeader;

    RideAdapter adapter;
    List<RideModel> rideList = new ArrayList<>();
    
    SuggestionAdapter suggestionAdapter;
    List<SuggestionModel> suggestionList = new ArrayList<>();

    FirebaseFirestore db;
    long selectedTimestamp = 0;
    
    private final OkHttpClient httpClient = new OkHttpClient();
    private Call currentCall;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private boolean isProgrammaticChange = false;
    private boolean isFromField = true;
    
    double fromLat = 0, fromLng = 0, toLat = 0, toLng = 0;
    FusedLocationProviderClient fusedLocationClient;

    String ORS_API_KEY = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6ImUxYWU0M2NiMjkzYTRiMGVhYmUyMTE4ZDI3YTA2MDhhIiwiaCI6Im11cm11cjY0In0=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Bottom Nav
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.getMenu().findItem(R.id.nav_search).setChecked(true);
        NavHelper.setupNavigation(this, bottomNav);

        // Init Views
        etFrom = findViewById(R.id.etFrom);
        etTo = findViewById(R.id.etTo);
        etPassengers = findViewById(R.id.etPassengers);
        etDate = findViewById(R.id.etDate);
        rvRides = findViewById(R.id.rvRides);
        recyclerSuggestions = findViewById(R.id.recyclerSuggestions);
        tvResultsHeader = findViewById(R.id.tvResultsHeader);

        // Main Ride List Setup
        rvRides.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RideAdapter(rideList);
        rvRides.setAdapter(adapter);

        // Suggestions List Setup
        recyclerSuggestions.setLayoutManager(new LinearLayoutManager(this));
        suggestionAdapter = new SuggestionAdapter(suggestionList, model -> {
            isProgrammaticChange = true;
            hideSuggestions(); // 🔥 Clear immediately upon selection
            
            if (model.isCurrentLocation()) {
                getCurrentLocation();
                return;
            }

            if (isFromField) {
                etFrom.setText(model.getName());
                fromLat = model.getLat();
                fromLng = model.getLng();
            } else {
                etTo.setText(model.getName());
                toLat = model.getLat();
                toLng = model.getLng();
            }
        });
        recyclerSuggestions.setAdapter(suggestionAdapter);

        setupSearch(etFrom, true);
        setupSearch(etTo, false);

        // Date Picker
        etDate.setOnClickListener(v -> {
            CalendarConstraints constraints = new CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointForward.now()).build();

            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Date").setCalendarConstraints(constraints).build();

            picker.show(getSupportFragmentManager(), "DATE");
            picker.addOnPositiveButtonClickListener(selection -> {
                selectedTimestamp = selection;
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                etDate.setText(sdf.format(new Date(selection)));
            });
        });

        findViewById(R.id.btnSearch).setOnClickListener(v -> searchRides());
    }

    private void hideSuggestions() {
        if (currentCall != null) currentCall.cancel(); // 🔥 Cancel active search
        recyclerSuggestions.setVisibility(View.GONE);
        suggestionList.clear();
        suggestionAdapter.notifyDataSetChanged();
    }

    private void setupSearch(EditText editText, boolean isFrom) {
        editText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) {
                if (isProgrammaticChange) {
                    isProgrammaticChange = false;
                    recyclerSuggestions.setVisibility(View.GONE); // 🔥 Extra safety
                    return;
                }

                isFromField = isFrom;
                if (isFrom) { fromLat = 0; fromLng = 0; } else { toLat = 0; toLng = 0; }
                
                if (searchRunnable != null) handler.removeCallbacks(searchRunnable);
                if (s.toString().trim().length() < 3) {
                    hideSuggestions();
                    return;
                }
                searchRunnable = () -> fetchSuggestions(s.toString());
                handler.postDelayed(searchRunnable, 300);
            }
        });

        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                handler.postDelayed(() -> {
                    if (!etFrom.hasFocus() && !etTo.hasFocus()) {
                        hideSuggestions();
                    }
                }, 200);
            }
        });
    }

    private void fetchSuggestions(String query) {
        if (currentCall != null) currentCall.cancel();
        new Thread(() -> {
            try {
                String url = "https://api.openrouteservice.org/geocode/search?api_key=" + ORS_API_KEY + "&text=" + query;
                Request request = new Request.Builder().url(url).build();
                currentCall = httpClient.newCall(request);
                Response response = currentCall.execute();
                if (response.body() == null) return;
                JSONObject json = new JSONObject(response.body().string());
                JSONArray features = json.getJSONArray("features");
                
                List<SuggestionModel> list = new ArrayList<>();
                list.add(new SuggestionModel("CURRENT_LOCATION"));
                for (int i = 0; i < features.length(); i++) {
                    JSONObject obj = features.getJSONObject(i);
                    String name = obj.getJSONObject("properties").getString("label");
                    JSONArray coord = obj.getJSONObject("geometry").getJSONArray("coordinates");
                    list.add(new SuggestionModel(name, coord.getDouble(1), coord.getDouble(0), 0));
                }

                runOnUiThread(() -> {
                    if (!isProgrammaticChange && ((isFromField && etFrom.hasFocus()) || (!isFromField && etTo.hasFocus()))) {
                        if (!list.isEmpty()) {
                            recyclerSuggestions.setVisibility(View.VISIBLE);
                            suggestionAdapter.updateList(list);
                        } else {
                            hideSuggestions();
                        }
                    }
                });
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        updateLocationFromCoordinates(location.getLatitude(), location.getLongitude());
                    } else {
                        Toast.makeText(this, "Location not found.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateLocationFromCoordinates(double lat, double lng) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                String addressName = (addresses != null && !addresses.isEmpty()) ? addresses.get(0).getAddressLine(0) : "Current Location";

                runOnUiThread(() -> {
                    isProgrammaticChange = true;
                    if (isFromField) { etFrom.setText(addressName); fromLat = lat; fromLng = lng; }
                    else { etTo.setText(addressName); toLat = lat; toLng = lng; }
                    hideSuggestions();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    isProgrammaticChange = true;
                    if (isFromField) { fromLat = lat; fromLng = lng; etFrom.setText("Current Location"); }
                    else { toLat = lat; toLng = lng; etTo.setText("Current Location"); }
                    hideSuggestions();
                });
            }
        }).start();
    }

    private void searchRides() {
        if (!validateInputs()) return;

        String fromInput = etFrom.getText().toString().toLowerCase().trim();
        String toInput = etTo.getText().toString().toLowerCase().trim();
        String passengersStr = etPassengers.getText().toString().trim();
        int requiredSeats = passengersStr.isEmpty() ? 1 : Integer.parseInt(passengersStr);

        db.collection("Rides")
                .whereEqualTo(constants.FIELD_STATUS, constants.STATUS_APPROVED)
                .get()
                .addOnSuccessListener(snapshot -> {
                    rideList.clear();
                    long now = System.currentTimeMillis();
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    String currentUid = (currentUser != null) ? currentUser.getUid() : "";

                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        RideModel ride = doc.toObject(RideModel.class);
                        if (ride == null || ride.driverId.equals(currentUid)) continue;

                        if (selectedTimestamp != 0) {
                            if (ride.timestamp < selectedTimestamp) continue;
                        } else if (ride.timestamp < (now - 3600000)) continue;

                        if (ride.seats < requiredSeats) continue;

                        boolean fromMatch = (ride.from != null && ride.from.toLowerCase().contains(fromInput)) || isNear(fromLat, fromLng, ride.fromLat, ride.fromLng);
                        boolean toMatch = (ride.to != null && ride.to.toLowerCase().contains(toInput)) || isNear(toLat, toLng, ride.toLat, ride.toLng);

                        if (fromMatch && toMatch) {
                            rideList.add(ride);
                        }
                    }

                    Collections.sort(rideList, (r1, r2) -> Long.compare(r1.timestamp, r2.timestamp));
                    adapter.notifyDataSetChanged();
                    
                    if (tvResultsHeader != null) tvResultsHeader.setVisibility(View.VISIBLE);
                    if (rideList.isEmpty()) Toast.makeText(this, "No matching rides found", Toast.LENGTH_SHORT).show();
                });
    }

    private boolean isNear(double lat1, double lon1, double lat2, double lon2) {
        if (lat1 == 0 || lat2 == 0) return false;
        float[] res = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, res);
        return res[0] < 15000; // 15 KM Radius
    }

    private boolean validateInputs() {
        if (etFrom.getText().toString().isEmpty()) { etFrom.setError("Origin Required"); return false; }
        if (etTo.getText().toString().isEmpty()) { etTo.setError("Destination Required"); return false; }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
    }
}
