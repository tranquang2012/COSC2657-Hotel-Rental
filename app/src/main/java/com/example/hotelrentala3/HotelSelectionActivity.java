package com.example.hotelrentala3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class HotelSelectionActivity extends AppCompatActivity {

    private ListView listViewHotels;
    private FirebaseFirestore db;
    private List<DocumentSnapshot> hotelDocuments;
    private HotelAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_selection);

        listViewHotels = findViewById(R.id.listViewHotels);
        db = FirebaseFirestore.getInstance();

        hotelDocuments = new ArrayList<>();
        adapter = new HotelAdapter(this, hotelDocuments);
        listViewHotels.setAdapter(adapter);

        // fetch and display hotels
        fetchHotelData();

        listViewHotels.setOnItemClickListener((parent, view, position, id) -> {
            // Get the selected document and pass its ID to PaymentActivity
            DocumentSnapshot selectedHotel = hotelDocuments.get(position);
            String selectedHotelId = selectedHotel.getId();

            // proceed to payment with selected hotel details
            Intent intent = new Intent(HotelSelectionActivity.this, RoomSelectionActivity.class);
            intent.putExtra("selectedHotelId", selectedHotelId);
            startActivity(intent);
        });
    }

    private void fetchHotelData() {
        db.collection("TestHotel")
                .whereGreaterThan("availability", 0)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        hotelDocuments.clear();
                        hotelDocuments.addAll(queryDocumentSnapshots.getDocuments());
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(HotelSelectionActivity.this, "No available hotels found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(HotelSelectionActivity.this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
