package com.example.hotelrentala3;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.firebase.firestore.*;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Load Map Fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapFragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Set default camera to Sydney
        LatLng sydney = new LatLng(-33.8688, 151.2093); // Sydney coordinates
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sydney, 12));

        // Fetch hotel data from Firebase
        db.collection("TestHotel").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();

                for (QueryDocumentSnapshot document : task.getResult()) {
                    if (document.contains("latitude") && document.contains("longitude") &&
                            document.contains("name") && document.contains("price") &&
                            document.contains("availability") && document.contains("rating")) {

                        double lat = document.getDouble("latitude");
                        double lng = document.getDouble("longitude");
                        String name = document.getString("name");
                        long price = document.getLong("price");
                        double rating = document.getDouble("rating");
                        boolean availability = document.getBoolean("availability");

                        // Create a marker for each hotel
                        LatLng location = new LatLng(lat, lng);
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(location)
                                .title(name)
                                .snippet(
                                        "Price: " + price + " VND\n" +
                                                "Rating: " + rating + "\n" +
                                                "Available: " + (availability ? "Yes" : "No")
                                )
                                .icon(BitmapDescriptorFactory.defaultMarker(
                                        availability ? BitmapDescriptorFactory.HUE_GREEN : BitmapDescriptorFactory.HUE_RED
                                ));

                        mMap.addMarker(markerOptions);
                        boundsBuilder.include(location);
                    }
                }

                // Adjust camera to fit all markers if any exist
                if (!task.getResult().isEmpty()) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));
                }
            } else {
                // Log any errors
                task.getException().printStackTrace();
            }
        });

        // Enable marker click listener for interactivity
        mMap.setOnMarkerClickListener(marker -> {
            showHotelDialog(marker.getTitle(), marker.getSnippet());
            return true;
        });
    }

    private void showHotelDialog(String name, String details) {
        // Inflate the dialog layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_hotel_info, null);

        // Set hotel details in the dialog
        TextView nameTextView = dialogView.findViewById(R.id.hotelName);
        TextView detailsTextView = dialogView.findViewById(R.id.hotelPrice);

        nameTextView.setText(name);
        detailsTextView.setText(details);

        // Show the dialog
        new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("Close", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
