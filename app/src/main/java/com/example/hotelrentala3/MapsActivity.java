package com.example.hotelrentala3;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db;
    private final HashMap<Marker, String> markerDetailsMap = new HashMap<>();
    private final HashMap<Marker, String> markerNamesMap = new HashMap<>();
    private final HashMap<Marker, LatLng> markerLocationMap = new HashMap<>();
    private static final String TAG = "MapsActivity";

    private BottomSheetBehavior<LinearLayout> bottomSheetBehavior;
    private TextView hotelNameTextView;
    private TextView hotelDetailsTextView;
    private ImageButton btnGetDirections, buttonGoogleMaps;
    private LatLng selectedHotelLocation;
    private String regionName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        db = FirebaseFirestore.getInstance();

        LinearLayout bottomSheet = findViewById(R.id.bottomSheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        hotelNameTextView = findViewById(R.id.hotelName);
        hotelDetailsTextView = findViewById(R.id.hotelDetails);
        btnGetDirections = findViewById(R.id.btnGetDirections);
        buttonGoogleMaps = findViewById(R.id.buttonGoogleMaps);

        bottomSheet.setNestedScrollingEnabled(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setPeekHeight(getResources().getDimensionPixelSize(R.dimen.bottom_sheet_peek_height));
        bottomSheetBehavior.setHideable(false);

        hotelDetailsTextView.setMovementMethod(new android.text.method.ScrollingMovementMethod());
        hotelNameTextView.setOnClickListener(v -> {
            bottomSheetBehavior.setPeekHeight(getResources().getDimensionPixelSize(R.dimen.bottom_sheet_expanded_height));
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        EditText searchBar = findViewById(R.id.searchBar);
        regionName = getIntent().getStringExtra("location");
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMarkers(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnGetDirections.setOnClickListener(v -> {
            if (selectedHotelLocation != null) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + selectedHotelLocation.latitude + "," + selectedHotelLocation.longitude);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
            }
        });

        buttonGoogleMaps.setOnClickListener(v -> {
            if (selectedHotelLocation != null) {
                Uri gmmIntentUri = Uri.parse("geo:" + selectedHotelLocation.latitude + "," + selectedHotelLocation.longitude + "?q=" + selectedHotelLocation.latitude + "," + selectedHotelLocation.longitude);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent);
                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        fetchRegionCoordinates(regionName);

        fetchHotelData();

        mMap.setOnMarkerClickListener(marker -> {
            String details = markerDetailsMap.get(marker);
            String name = marker.getTitle();
            selectedHotelLocation = markerLocationMap.get(marker);
            if (details != null && name != null) {
                showHotelDetails(name, details);
            }
            return true;
        });
    }

    private void fetchRegionCoordinates(String regionName) {
        new Thread(() -> {
            try {
                // Use the Geocoding API to fetch the coordinates of the region
                String apiKey = getString(R.string.google_maps_key);
                String apiUrl = "https://maps.googleapis.com/maps/api/geocode/json?address=" + regionName + "&key=" + apiKey;

                java.net.URL url = new java.net.URL(apiUrl);
                java.net.HttpURLConnection connection = (java.net.HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    java.io.InputStream inputStream = connection.getInputStream();
                    java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    reader.close();
                    connection.disconnect();

                    // Parse the JSON response
                    org.json.JSONObject jsonResponse = new org.json.JSONObject(result.toString());
                    org.json.JSONArray results = jsonResponse.getJSONArray("results");
                    if (results.length() > 0) {
                        org.json.JSONObject location = results.getJSONObject(0).getJSONObject("geometry").getJSONObject("location");
                        double latitude = location.getDouble("lat");
                        double longitude = location.getDouble("lng");

                        // Move the map camera to the region's coordinates
                        runOnUiThread(() -> mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 12)));
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error fetching region coordinates: ", e);
            }
        }).start();
    }
    private void fetchHotelData() {
        db.collection("TestHotel")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Double availability = document.getDouble("availability");
                            if (availability != null && availability > 0) {
                                Double latitude = document.getDouble("latitude");
                                Double longitude = document.getDouble("longitude");
                                String name = document.getString("name");
                                Double price = document.getDouble("price");
                                Double rating = document.getDouble("rating");  // Fetch rating

                                if (latitude != null && longitude != null && name != null) {
                                    LatLng location = new LatLng(latitude, longitude);
                                    Marker marker = mMap.addMarker(new MarkerOptions()
                                            .position(location)
                                            .title(name)
                                            .icon(BitmapDescriptorFactory.fromBitmap(createCustomMarker(String.valueOf(price)))));

                                    if (marker != null) {
                                        String hotelDetails = "Price: " + price + "VND";
                                        if (rating != null) {
                                            hotelDetails += "\nRating: " + rating + " ⭐";  // Add rating to details
                                        }
                                        markerDetailsMap.put(marker, hotelDetails);
                                        markerNamesMap.put(marker, name);
                                        markerLocationMap.put(marker, location);
                                    }
                                }
                            }
                        }
                    } else {
                        Log.e(TAG, "Error fetching hotels: ", task.getException());
                    }
                });
    }

    private Bitmap createCustomMarker(String price) {
        View markerView = LayoutInflater.from(this).inflate(R.layout.custom_marker, null);
        TextView priceTextView = markerView.findViewById(R.id.marker_price);
        priceTextView.setText("$" + price);
        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        markerView.layout(0, 0, markerView.getMeasuredWidth(), markerView.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(markerView.getMeasuredWidth(), markerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        markerView.draw(canvas);
        return bitmap;
    }

    private void showHotelDetails(String name, String details) {
        hotelNameTextView.setText(name);
        hotelDetailsTextView.setText(details);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    private void filterMarkers(String query) {
        for (Marker marker : markerNamesMap.keySet()) {
            marker.setVisible(marker.getTitle().toLowerCase().contains(query.toLowerCase()));
        }
    }
}