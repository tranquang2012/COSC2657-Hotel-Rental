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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.hotelrentala3.Model.Hotel;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    private Button btnBookNow;
    private LatLng selectedHotelLocation;
    private String regionName;
    private List<Hotel> filteredHotels = new ArrayList<>();
    private String checkInDate, checkOutDate, roomDetails;
    private Hotel selectedHotel;


    // Predefined coordinates
    private static final LatLng HA_NOI = new LatLng(21.028511, 105.804817);
    private static final LatLng HO_CHI_MINH = new LatLng(10.823099, 106.629662);
    private static final LatLng DA_NANG = new LatLng(16.047079, 108.206230);

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
        btnBookNow = findViewById(R.id.btnBookNow);

        bottomSheet.setNestedScrollingEnabled(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheetBehavior.setPeekHeight(getResources().getDimensionPixelSize(R.dimen.bottom_sheet_peek_height));
        bottomSheetBehavior.setHideable(false);

        Intent intent = getIntent();
        if (intent != null) {
            filteredHotels = (List<Hotel>) intent.getSerializableExtra("availableHotels");
            checkInDate = intent.getStringExtra("checkInDate");
            checkOutDate = intent.getStringExtra("checkOutDate");
            roomDetails = intent.getStringExtra("roomDetails");
        }

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
        btnBookNow.setOnClickListener(v -> {
            if (selectedHotel != null) {
                Intent i = new Intent(this, RoomSelectionActivity.class);
                i.putExtra("selectedHotel", selectedHotel);
                i.putExtra("checkInDate", checkInDate);
                i.putExtra("checkOutDate", checkOutDate);
                i.putExtra("roomDetails", roomDetails);
                startActivity(i);
            } else {
                Log.e(TAG, "Hotel not selected. Please try again.");
            }
        });

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Parse region name and trim extra spaces
        regionName = getIntent().getStringExtra("location");
        if (regionName != null) {
            Log.d(TAG, "Received location: " + regionName.trim());
            regionName = regionName.trim();
        } else {
            Log.w(TAG, "Region name is null.");
        }

        // Predefined regions with coordinates
        LatLng hanoi = new LatLng(21.028511, 105.804817);
        LatLng hoChiMinh = new LatLng(10.823099, 106.629662);
        LatLng daNang = new LatLng(16.054407, 108.202167);

        // Match the parsed location string with predefined choices
        if (regionName != null) {
            switch (regionName.toLowerCase().trim()) {
                case "hanoi":
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hanoi, 12f));
                    break;
                case "ho chi minh":
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(hoChiMinh, 12f));
                    break;
                case "da nang":
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(daNang, 12f));
                    break;
                default:
                    Log.w(TAG, "Region name does not match predefined choices. Defaulting to Sydney.");
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.8688, 151.2093), 12f));
                    break;
            }
        } else {
            Log.w(TAG, "Region name is null. Defaulting to Sydney.");
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(-33.8688, 151.2093), 12f));
        }

        fetchHotelData();

        mMap.setOnMarkerClickListener(marker -> {
            String name = marker.getTitle();
            String details = markerDetailsMap.get(marker);
            selectedHotelLocation = markerLocationMap.get(marker);

            Log.d(TAG, "Marker clicked: " + name);

            if (name != null && details != null) {
                showHotelDetails(name, details);

                selectedHotel = null; // Reset before searching
                for (Hotel hotel : filteredHotels) {
                    if (hotel.getName() != null && hotel.getName().trim().equalsIgnoreCase(name.trim())) {
                        selectedHotel = hotel;
                        Log.d(TAG, "Selected Hotel: " + hotel.getName());
                        break;
                    }
                }

                if (selectedHotel == null) {
                    Log.e(TAG, "No matching hotel found for marker: " + name);
                }
            } else {
                Log.e(TAG, "Marker title or details are null.");
            }
            return true;
        });
    }

    private void fetchHotelData() {
        if (filteredHotels == null || filteredHotels.isEmpty()) {
            Log.w(TAG, "No hotels to display on the map.");
            return;
        }

        for (Hotel hotel : filteredHotels) {
            Double latitude = hotel.getLatitude();
            Double longitude = hotel.getLongitude();
            String name = hotel.getName();
            Double price = hotel.getPrice();
            Double rating = hotel.getRating();

            if (latitude != null && longitude != null && name != null) {
                LatLng location = new LatLng(latitude, longitude);
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(location)
                        .title(name)
                        .icon(BitmapDescriptorFactory.fromBitmap(createCustomMarker(String.valueOf(price)))));

                if (marker != null) {
                    String hotelDetails = "Price: $" + price;
                    if (rating != null) {
                        hotelDetails += "\nRating: " + rating + " ⭐";
                    }
                    markerDetailsMap.put(marker, hotelDetails);
                    markerNamesMap.put(marker, name);
                    markerLocationMap.put(marker, location);
                }
            }
        }
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
            boolean isVisible = marker.getTitle().toLowerCase().contains(query.toLowerCase());
            marker.setVisible(isVisible);

            Log.d(TAG, "Marker " + marker.getTitle() + " visible: " + isVisible);
        }
    }
}