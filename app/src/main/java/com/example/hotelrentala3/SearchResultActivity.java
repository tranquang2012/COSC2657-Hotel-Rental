package com.example.hotelrentala3;

import android.os.Bundle;
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
        hotelsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        hotelList = (List<Hotel>) getIntent().getSerializableExtra("availableHotels");
        if (hotelList == null) {
            hotelList = new ArrayList<>();
        }

        filteredList = new ArrayList<>(hotelList);

        hotelAdapter = new HotelAdapter(filteredList);
        hotelsRecyclerView.setAdapter(hotelAdapter);

        setupSearch();
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