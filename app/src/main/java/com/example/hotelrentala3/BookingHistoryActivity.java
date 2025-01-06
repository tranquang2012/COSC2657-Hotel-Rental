package com.example.hotelrentala3;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BookingHistoryActivity extends AppCompatActivity {

    private ListView listViewBookingHistory;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private List<String> bookingHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_history);

        listViewBookingHistory = findViewById(R.id.listViewBookingHistory);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        bookingHistory = new ArrayList<>();

        fetchBookingHistory();
    }

    private void fetchBookingHistory() {
        db.collection("BookingHistory")
                .whereEqualTo("userId", mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (Map<String, Object> document : queryDocumentSnapshots.toObjects(Map.class)) {
                        bookingHistory.add("Amount: $" + document.get("amount") + ", Status: " + document.get("status") + ", Date: " + document.get("timestamp"));
                    }
                    updateListView();
                })
                .addOnFailureListener(e -> {
                    bookingHistory.add("Error fetching history: " + e.getMessage());
                    updateListView();
                });
    }

    private void updateListView() {
        listViewBookingHistory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, bookingHistory));
    }
}
