package com.example.hotelrentala3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HotelSelectionActivity extends AppCompatActivity {

    private ListView listViewHotels;
    private Button buttonBack;
    private FirebaseFirestore db;
    private List<String> hotelList;
    private ArrayAdapter<String> adapter;
    private List<DocumentSnapshot> hotelsData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_selection);

        listViewHotels = findViewById(R.id.listViewHotels);
        buttonBack = findViewById(R.id.buttonBack);
        db = FirebaseFirestore.getInstance();

        hotelList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, hotelList);
        listViewHotels.setAdapter(adapter);

        // fetch hotel data from Firebase
        fetchHotelData();

        // go back to HomeActivity
        buttonBack.setOnClickListener(v -> {
            Intent intent = new Intent(HotelSelectionActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        listViewHotels.setOnItemClickListener((parent, view, position, id) -> {
            DocumentSnapshot selectedHotel = hotelsData.get(position);
            String hotelName = selectedHotel.getString("hotelName");
            String hotelId = selectedHotel.getId();

            // proceed to payment with selected hotel details
            Intent intent = new Intent(HotelSelectionActivity.this, PaymentActivity.class);
            intent.putExtra("selectedHotelId", hotelId);
            intent.putExtra("hotelName", hotelName);
            startActivity(intent);
        });
    }

    private void fetchHotelData() {
        db.collection("Hotels")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        hotelsData = queryDocumentSnapshots.getDocuments();
                        for (DocumentSnapshot documentSnapshot : hotelsData) {
                            String hotelName = documentSnapshot.getString("hotelName");
                            hotelList.add(hotelName);
                        }
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(HotelSelectionActivity.this, "No hotels found.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(HotelSelectionActivity.this, "Error fetching data.", Toast.LENGTH_SHORT).show());
    }
}
