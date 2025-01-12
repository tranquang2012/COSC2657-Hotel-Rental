package com.example.hotelrentala3;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hotelrentala3.Model.Room;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView checkInDate, checkOutDate, searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firestore and views
        db = FirebaseFirestore.getInstance();
        checkInDate = findViewById(R.id.checkin_date);
        checkOutDate = findViewById(R.id.checkout_date);
        searchButton = findViewById(R.id.search_button);

        // Set onClickListeners
        checkInDate.setOnClickListener(view -> openDatePicker("checkin"));
        checkOutDate.setOnClickListener(view -> openDatePicker("checkout"));
        searchButton.setOnClickListener(view -> {
            String selectedCheckIn = checkInDate.getText().toString();
            String selectedCheckOut = checkOutDate.getText().toString();

            if (selectedCheckIn.equals("Select a date") || selectedCheckOut.equals("Select a date")) {
                Toast.makeText(this, "Please select both check-in and check-out dates", Toast.LENGTH_SHORT).show();
            } else {
                searchAvailableRooms(selectedCheckIn, selectedCheckOut);
            }
        });
    }

    // Opens the date picker for check-in or check-out
    private void openDatePicker(String dateType) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String selectedDate = selectedYear + "-" + (selectedMonth + 1) + "-" + selectedDay;
                    if (dateType.equals("checkin")) {
                        checkInDate.setText(selectedDate);
                    } else {
                        checkOutDate.setText(selectedDate);
                    }
                }, year, month, day);

        datePickerDialog.show();
    }

    // Searches available rooms based on the selected dates
    private void searchAvailableRooms(String selectedCheckIn, String selectedCheckOut) {
        db.collection("hotels")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Room> availableRooms = new ArrayList<>();

                    for (DocumentSnapshot hotelSnapshot : queryDocumentSnapshots.getDocuments()) {
                        String hotelName = hotelSnapshot.getString("name");
                        String location = hotelSnapshot.getString("location");

                        Map<String, Map<String, Object>> rooms = (Map<String, Map<String, Object>>) hotelSnapshot.get("rooms");
                        for (Map.Entry<String, Map<String, Object>> roomEntry : rooms.entrySet()) {
                            Map<String, Object> roomDetails = roomEntry.getValue();

                            String roomType = (String) roomDetails.get("type");
                            String capacity = (String) roomDetails.get("capacity");
                            long price = (long) roomDetails.get("price");
                            List<Map<String, String>> bookings = (List<Map<String, String>>) roomDetails.get("bookings");

                            if (isRoomAvailable(selectedCheckIn, selectedCheckOut, bookings)) {
                                availableRooms.add(new Room(hotelName, location, roomType, capacity, price));
                            }
                        }
                    }

                    if (availableRooms.isEmpty()) {
                        Toast.makeText(this, "No rooms available for the selected dates", Toast.LENGTH_SHORT).show();
                    } else {
                        showAvailableRooms(availableRooms);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to search rooms: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Checks if a room is available for the selected dates
    private boolean isRoomAvailable(String selectedCheckIn, String selectedCheckOut, List<Map<String, String>> bookings) {
        LocalDate selectedIn = LocalDate.parse(selectedCheckIn);
        LocalDate selectedOut = LocalDate.parse(selectedCheckOut);

        for (Map<String, String> booking : bookings) {
            LocalDate bookedIn = LocalDate.parse(booking.get("checkin_date"));
            LocalDate bookedOut = LocalDate.parse(booking.get("checkout_date"));

            // Check if the selected dates overlap with booked dates
            if (!(selectedOut.isBefore(bookedIn) || selectedIn.isAfter(bookedOut))) {
                return false;
            }
        }

        return true;
    }

    // Launches the ResultActivity with the list of available rooms
    private void showAvailableRooms(List<Room> availableRooms) {
        Intent intent = new Intent(this, SearchResultActivity.class);
        intent.putExtra("availableRooms", (Serializable) availableRooms);
        startActivity(intent);
    }

}