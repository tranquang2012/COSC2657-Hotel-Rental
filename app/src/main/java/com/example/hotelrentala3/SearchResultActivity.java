package com.example.hotelrentala3;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hotelrentala3.Adapter.RoomAdapter;
import com.example.hotelrentala3.Model.Room;

import java.util.List;

public class SearchResultActivity extends AppCompatActivity {

    private RecyclerView roomsRecyclerView;
    private RoomAdapter roomAdapter;

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

        roomsRecyclerView = findViewById(R.id.rooms_recycler_view);
        roomsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<Room> availableRooms = (List<Room>) getIntent().getSerializableExtra("availableRooms");
        roomAdapter = new RoomAdapter(availableRooms);
        roomsRecyclerView.setAdapter(roomAdapter);
    }
}