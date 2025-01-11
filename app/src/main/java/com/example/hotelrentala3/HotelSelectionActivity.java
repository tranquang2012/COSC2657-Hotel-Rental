package com.example.hotelrentala3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
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
    private List<String> hotelList;
    private List<DocumentSnapshot> hotelDocuments; // Store documents to map to user selection
    private ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_selection);

        listViewHotels = findViewById(R.id.listViewHotels);
        db = FirebaseFirestore.getInstance();

        hotelList = new ArrayList<>();
        hotelDocuments = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, hotelList);
        listViewHotels.setAdapter(adapter);

        // fetch and display hotels
        fetchHotelData();

        listViewHotels.setOnItemClickListener((parent, view, position, id) -> {
            // Get the selected document and pass its ID to PaymentActivity
            DocumentSnapshot selectedHotel = hotelDocuments.get(position);
            String selectedHotelId = selectedHotel.getId();

            // proceed to payment with selected hotel details
            Intent intent = new Intent(HotelSelectionActivity.this, PaymentActivity.class);
            intent.putExtra("selectedHotelId", selectedHotelId);
            startActivity(intent);
        });
    }

    private void fetchHotelData() {
        db.collection("TestHotel")
                .whereEqualTo("availability", true) // Filter by availability
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        hotelList.clear();
                        hotelDocuments.clear();

                        // Populate the list and map documents
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String name = document.getString("name");
                            double price = document.getDouble("price");
                            double rating = document.getDouble("rating");

                            if (name != null) {
                                hotelList.add(name + " - Price: $" + price + " - Rating: " + rating + "★");
                                hotelDocuments.add(document); // Keep track of documents for selection
                            }
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(HotelSelectionActivity.this, "No available hotels found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(HotelSelectionActivity.this, "Error fetching data: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
