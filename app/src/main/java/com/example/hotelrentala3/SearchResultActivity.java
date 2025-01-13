package com.example.hotelrentala3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelrentala3.Adapter.HotelAdapter;
import com.example.hotelrentala3.Model.Hotel;

import java.util.ArrayList;
import java.util.List;

public class SearchResultActivity extends AppCompatActivity {

    private RecyclerView hotelsRecyclerView;
    private HotelAdapter hotelAdapter;
    private SearchView svHotelSearch;
    private List<Hotel> hotelList;
    private List<Hotel> filteredList;
    private String location;
    private static final String TAG = "SearchResultActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search_result);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        hotelsRecyclerView = findViewById(R.id.rooms_recycler_view);
        svHotelSearch = findViewById(R.id.sv_hotel_search);
        LinearLayout btnOpenMap = findViewById(R.id.btn_open_map);

        hotelsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        try {
            hotelList = (List<Hotel>) getIntent().getSerializableExtra("availableHotels");
        } catch (Exception e) {
            Log.e(TAG, "Error parsing hotel list: ", e);
            hotelList = new ArrayList<>();
        }

        if (hotelList == null) {
            hotelList = new ArrayList<>();
        }

        // Parse location from the intent
        location = getIntent().getStringExtra("location");
        if (location == null || location.isEmpty()) {
            Log.w(TAG, "Location is null or empty, defaulting to 'Unknown Location'.");
            location = "Unknown Location";
        } else {
            Log.d(TAG, "Parsed location: " + location);
        }
        String checkInDate = getIntent().getStringExtra("checkInDate");
        String checkOutDate = getIntent().getStringExtra("checkOutDate");
        String roomDetails = getIntent().getStringExtra("roomDetails");

        Log.d(TAG, "Check-in Date: " + checkInDate);
        Log.d(TAG, "Check-out Date: " + checkOutDate);
        Log.d(TAG, "Room Details: " + roomDetails);

        filteredList = new ArrayList<>(hotelList);

        hotelAdapter = new HotelAdapter(filteredList);
        hotelsRecyclerView.setAdapter(hotelAdapter);

        setupSearch();

        btnOpenMap.setOnClickListener(view -> {
            Intent mapIntent = new Intent(this, MapsActivity.class);
            mapIntent.putExtra("availableHotels", new ArrayList<>(filteredList));
            mapIntent.putExtra("location", location.trim());
            mapIntent.putExtra("checkInDate", checkInDate);  // Forward check-in date
            mapIntent.putExtra("checkOutDate", checkOutDate); // Forward check-out date
            mapIntent.putExtra("roomDetails", roomDetails);
            startActivity(mapIntent);
        });

    }

    private void setupSearch() {
        svHotelSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform filtering when the user submits the query
                filterHotels(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Perform filtering as the user types
                filterHotels(newText);
                return true;
            }
        });
    }

    private void filterHotels(@NonNull String query) {
        filteredList.clear();
        if (query.isEmpty()) {
            filteredList.addAll(hotelList); // If query is empty, show all hotels
        } else {
            for (Hotel hotel : hotelList) {
                if (hotel.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(hotel); // Add hotels matching the query
                }
            }
        }
        hotelAdapter.notifyDataSetChanged(); // Notify the adapter about data changes
    }
}