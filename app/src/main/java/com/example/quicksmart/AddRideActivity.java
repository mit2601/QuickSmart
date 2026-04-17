package com.example.quicksmart;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.*;
import android.text.*;
import android.view.View;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.*;

import com.google.android.gms.location.*;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.*;

import com.google.firebase.auth.*;
import com.google.firebase.firestore.*;

import org.json.*;
import java.text.SimpleDateFormat;
import java.util.*;

import okhttp3.*;

public class AddRideActivity extends AppCompatActivity {

    EditText Cleave, Cgoing, Cdate, Cpassengers, Cprice;
    TextView tvDistance;
    Button btncreate;
    RecyclerView recyclerSuggestions;

    SuggestionAdapter adapter;
    List<SuggestionModel> suggestionList = new ArrayList<>();

    Handler handler = new Handler(Looper.getMainLooper());
    Runnable searchRunnable;

    boolean isFromField = true;
    boolean isProgrammaticChange = false;

    double fromLat = 0, fromLng = 0, toLat = 0, toLng = 0;
    double distanceKm = 0;

    long selectedTimestamp = 0;

    FusedLocationProviderClient fusedLocationClient;
    
    private final OkHttpClient httpClient = new OkHttpClient();
    private Call currentCall;

    String ORS_API_KEY = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6ImUxYWU0M2NiMjkzYTRiMGVhYmUyMTE4ZDI3YTA2MDhhIiwiaCI6Im11cm11cjY0In0=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_ride);
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.getMenu().findItem(R.id.nav_add).setChecked(true);
        NavHelper.setupNavigation(this, bottomNav);

        Cleave = findViewById(R.id.Cleave);
        Cgoing = findViewById(R.id.Cgoing);
        Cdate = findViewById(R.id.Cdate);
        Cpassengers = findViewById(R.id.Cpassengers);
        Cprice = findViewById(R.id.Cprice);
        tvDistance = findViewById(R.id.tvDistance);
        btncreate = findViewById(R.id.btncreate);
        recyclerSuggestions = findViewById(R.id.recyclerSuggestions);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        recyclerSuggestions.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SuggestionAdapter(suggestionList, model -> {
            isProgrammaticChange = true;

            if (model.isCurrentLocation()) {
                getCurrentLocation();
                return;
            }

            if (isFromField) {
                Cleave.setText(model.getName());
                fromLat = model.getLat();
                fromLng = model.getLng();
            } else {
                Cgoing.setText(model.getName());
                toLat = model.getLat();
                toLng = model.getLng();
            }

            hideSuggestions();

            if (fromLat != 0 && toLat != 0) {
                calculateRouteDistance();
            }
        });

        recyclerSuggestions.setAdapter(adapter);

        setupSearch(Cleave, true);
        setupSearch(Cgoing, false);

        Cdate.setOnClickListener(v -> {
            CalendarConstraints constraints = new CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointForward.now())
                    .build();

            MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select Date")
                    .setCalendarConstraints(constraints)
                    .build();

            picker.show(getSupportFragmentManager(), "DATE");
            picker.addOnPositiveButtonClickListener(selection -> {
                selectedTimestamp = selection;
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                Cdate.setText(sdf.format(new Date(selection)));
            });
        });

        btncreate.setOnClickListener(v -> checkVerificationStatus());
    }

    private void checkVerificationStatus() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        // Perform validations before checking server status
        if (!validateInputs()) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users").document(user.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String status = documentSnapshot.getString("driverStatus");
                        if (status == null) status = "unverified";

                        switch (status) {
                            case "verified":
                                createRide();
                                break;
                            case "pending":
                                Toast.makeText(this, "Verification in progress. Please wait for admin approval.", Toast.LENGTH_LONG).show();
                                break;
                            default:
                                // Instead of automatic jump, show info or handle redirection
                                Toast.makeText(this, "Driver verification required.", Toast.LENGTH_SHORT).show();
                                // startActivity(new Intent(this, VerifyDriverActivity.class));
                                break;
                        }
                    }
                });
    }

    private boolean validateInputs() {
        String fromText = Cleave.getText().toString().trim();
        String toText = Cgoing.getText().toString().trim();
        String dateText = Cdate.getText().toString().trim();
        String seatsStr = Cpassengers.getText().toString().trim();
        String priceStr = Cprice.getText().toString().trim();

        if (fromText.isEmpty()) { Cleave.setError("Required"); return false; }
        if (toText.isEmpty()) { Cgoing.setError("Required"); return false; }
        if (dateText.isEmpty()) { Cdate.setError("Select Date"); return false; }
        
        if (seatsStr.isEmpty()) {
            Cpassengers.setError("Enter seats");
            return false;
        }
        int seats = Integer.parseInt(seatsStr);
        if (seats <= 0 || seats > 10) {
            Cpassengers.setError("Enter between 1-10 seats");
            return false;
        }

        if (priceStr.isEmpty()) {
            Cprice.setError("Enter price");
            return false;
        }
        int price = Integer.parseInt(priceStr);
        if (price <= 0) {
            Cprice.setError("Price must be positive");
            return false;
        }

        if (fromLat == 0 || toLat == 0) {
            Toast.makeText(this, "Please select locations from the suggestions", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void hideSuggestions() {
        recyclerSuggestions.setVisibility(View.GONE);
        suggestionList.clear();
        adapter.notifyDataSetChanged();
    }

    private void setupSearch(EditText editText, boolean isFrom) {
        editText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            public void afterTextChanged(Editable s) {
                if (isProgrammaticChange) {
                    isProgrammaticChange = false;
                    return;
                }

                isFromField = isFrom;
                
                if (isFrom) { fromLat = 0; fromLng = 0; }
                else { toLat = 0; toLng = 0; }

                if (searchRunnable != null) handler.removeCallbacks(searchRunnable);

                if (s.toString().trim().length() < 3) {
                    hideSuggestions();
                    return;
                }

                searchRunnable = () -> fetchSuggestions(s.toString());
                handler.postDelayed(searchRunnable, 250);
            }
        });
        
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                handler.postDelayed(() -> {
                    if (!Cleave.hasFocus() && !Cgoing.hasFocus()) {
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
                    if ((isFromField && Cleave.hasFocus()) || (!isFromField && Cgoing.hasFocus())) {
                        if (!list.isEmpty()) {
                            recyclerSuggestions.setVisibility(View.VISIBLE);
                            adapter.updateList(list);
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

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                updateLocationFromCoordinates(location.getLatitude(), location.getLongitude());
            } else {
                Toast.makeText(this, "Location not found.", Toast.LENGTH_SHORT).show();
                isProgrammaticChange = false;
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
                    if (isFromField) { Cleave.setText(addressName); fromLat = lat; fromLng = lng; }
                    else { Cgoing.setText(addressName); toLat = lat; toLng = lng; }
                    hideSuggestions();
                    if (fromLat != 0 && toLat != 0) calculateRouteDistance();
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    isProgrammaticChange = true;
                    if (isFromField) { fromLat = lat; fromLng = lng; Cleave.setText("Current Location"); }
                    else { toLat = lat; toLng = lng; Cgoing.setText("Current Location"); }
                    hideSuggestions();
                    if (fromLat != 0 && toLat != 0) calculateRouteDistance();
                });
            }
        }).start();
    }

    private void calculateRouteDistance() {
        if (fromLat == 0 || toLat == 0) return;
        float[] results = new float[1];
        Location.distanceBetween(fromLat, fromLng, toLat, toLng, results);
        distanceKm = results[0] / 1000.0;
        tvDistance.setText(String.format(Locale.getDefault(), "Distance: %.2f km", distanceKm));

        new Thread(() -> {
            try {
                String url = "https://api.openrouteservice.org/v2/directions/driving-car";
                JSONObject body = new JSONObject();
                JSONArray coords = new JSONArray();
                coords.put(new JSONArray().put(fromLng).put(fromLat));
                coords.put(new JSONArray().put(toLng).put(toLat));
                body.put("coordinates", coords);

                Request request = new Request.Builder().url(url)
                        .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
                        .addHeader("Authorization", ORS_API_KEY).build();

                Response response = httpClient.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    JSONObject res = new JSONObject(response.body().string());
                    double meters = res.getJSONArray("routes").getJSONObject(0).getJSONObject("summary").getDouble("distance");
                    distanceKm = meters / 1000.0;
                    runOnUiThread(() -> tvDistance.setText(String.format(Locale.getDefault(), "Distance: %.2f km", distanceKm)));
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    private void createRide() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        try {
            int seats = Integer.parseInt(Cpassengers.getText().toString());
            int price = Integer.parseInt(Cprice.getText().toString());

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference ref = db.collection("Rides").document();
            SharedPreferences sp = getSharedPreferences(ConstantSp.PREF, MODE_PRIVATE);

            Map<String, Object> ride = new HashMap<>();
            ride.put("rideId", ref.getId());
            ride.put("driverId", user.getUid());
            ride.put("driverName", sp.getString(ConstantSp.NAME, "Driver"));
            ride.put("from", Cleave.getText().toString());
            ride.put("to", Cgoing.getText().toString());
            ride.put("fromLat", fromLat); ride.put("fromLng", fromLng);
            ride.put("toLat", toLat); ride.put("toLng", toLng);
            ride.put("distanceKm", distanceKm);
            ride.put("date", Cdate.getText().toString());
            ride.put("timestamp", selectedTimestamp);
            ride.put("seats", seats);
            ride.put("price", price);
            ride.put("createdAt", System.currentTimeMillis());
            ride.put(constants.FIELD_STATUS, constants.STATUS_PENDING);

            ref.set(ride).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Ride Created", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, YourRideActivity.class));
                finish();
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error creating ride.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
    }
}
