package com.example.hotelrentala3;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private static final String TAG = "HistoryActivity";
    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private List<Booking> bookingList;
    private BookingAdapter adapter;
    private String userId = "RH0FWNGtLvWKeAl17vGNNSMjm9i2"; // Replace with dynamic user ID logic if needed.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.recyclerViewHistory);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookingList = new ArrayList<>();
        adapter = new BookingAdapter(bookingList, this::navigateToDetails);
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        fetchBookingHistory();
    }

    private void fetchBookingHistory() {
        db.collection("BookingHistory")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        QuerySnapshot snapshots = task.getResult();
                        if (!snapshots.isEmpty()) {
                            Log.d(TAG, "Fetched " + snapshots.size() + " bookings from Firestore.");
                            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                                Booking booking = doc.toObject(Booking.class);
                                if (booking != null) {
                                    bookingList.add(booking);
                                }
                            }
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "No bookings found in Firestore.");
                            Toast.makeText(this, "No booking history found", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Error fetching booking history: ", task.getException());
                        Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void navigateToDetails(Booking booking) {
        Intent intent = new Intent(this, DetailsActivity.class);
        intent.putExtra("booking", booking);
        startActivity(intent);
    }

    // Model class for Booking
    public static class Booking implements Serializable {
        private String checkInDate;
        private int finalPrice;
        private String fullName;
        private String hotelId;
        private int numberOfNights;
        private String selectedRoomType;
        private String status;

        public Booking() {
            // Empty constructor for Firestore
        }

        // Getters and Setters
        public String getCheckInDate() {
            return checkInDate;
        }

        public void setCheckInDate(String checkInDate) {
            this.checkInDate = checkInDate;
        }

        public int getFinalPrice() {
            return finalPrice;
        }

        public void setFinalPrice(int finalPrice) {
            this.finalPrice = finalPrice;
        }

        public String getFullName() {
            return fullName;
        }

        public void setFullName(String fullName) {
            this.fullName = fullName;
        }

        public String getHotelId() {
            return hotelId;
        }

        public void setHotelId(String hotelId) {
            this.hotelId = hotelId;
        }

        public int getNumberOfNights() {
            return numberOfNights;
        }

        public void setNumberOfNights(int numberOfNights) {
            this.numberOfNights = numberOfNights;
        }

        public String getSelectedRoomType() {
            return selectedRoomType;
        }

        public void setSelectedRoomType(String selectedRoomType) {
            this.selectedRoomType = selectedRoomType;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
