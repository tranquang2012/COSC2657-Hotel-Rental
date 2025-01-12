package com.example.hotelrentala3;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelrentala3.Adapter.HotelAdapter;
import com.example.hotelrentala3.Model.Hotel;

import java.util.List;

public class SearchResultActivity extends AppCompatActivity {

    private RecyclerView hotelsRecyclerView;
    private HotelAdapter hotelAdapter;

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

        List<Hotel> availableHotels = (List<Hotel>) getIntent().getSerializableExtra("availableRooms");
        hotelAdapter = new HotelAdapter(availableHotels);
        hotelsRecyclerView.setAdapter(hotelAdapter);
    }
}