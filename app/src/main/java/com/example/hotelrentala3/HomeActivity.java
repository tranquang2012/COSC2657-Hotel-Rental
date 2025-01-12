package com.example.hotelrentala3;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hotelrentala3.Model.Hotel;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView checkInDate;
    private TextView checkOutDate;
    private TextView tvRoomGuestInfo;
    private Spinner locationSpinner;  // Spinner for location selection
    private int numberOfPersons = 1, numberOfRooms = 1;

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
        locationSpinner = findViewById(R.id.location_spinner);  // Spinner for city selection
        checkInDate = findViewById(R.id.checkin_date);
        checkOutDate = findViewById(R.id.checkout_date);
        TextView searchButton = findViewById(R.id.search_button);
        tvRoomGuestInfo = findViewById(R.id.tv_room_guest_info);

        String[] items = {"Ho Chi Minh", "Hanoi", "Da Nang"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(adapter);

        checkInDate.setOnClickListener(view -> openDatePicker("checkin"));
        checkOutDate.setOnClickListener(view -> openDatePicker("checkout"));
        tvRoomGuestInfo.setOnClickListener(view -> openRoomGuestDialog());
        searchButton.setOnClickListener(view -> {
            String selectedLocation = locationSpinner.getSelectedItem().toString();  // Get selected city
            String selectedCheckIn = checkInDate.getText().toString();
            String selectedCheckOut = checkOutDate.getText().toString();

            if (selectedLocation.isEmpty() || selectedCheckIn.equals("Select a date") || selectedCheckOut.equals("Select a date")) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else {
                searchAvailableRooms(selectedLocation, selectedCheckIn, selectedCheckOut);
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

    private void openRoomGuestDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.room_guest_dialog);

        // Get dialog views
        TextView tvPersonsCount = dialog.findViewById(R.id.tv_persons_count);
        TextView tvRoomsCount = dialog.findViewById(R.id.tv_rooms_count);
        Button btnDone = dialog.findViewById(R.id.btn_done);

        // Increment and decrement buttons for persons
        dialog.findViewById(R.id.btn_increment_persons).setOnClickListener(view -> {
            int count = Integer.parseInt(tvPersonsCount.getText().toString());
            tvPersonsCount.setText(String.valueOf(++count));
        });

        dialog.findViewById(R.id.btn_decrement_persons).setOnClickListener(view -> {
            int count = Integer.parseInt(tvPersonsCount.getText().toString());
            if (count > 1) {
                tvPersonsCount.setText(String.valueOf(--count));
            }
        });

        // Increment and decrement buttons for rooms
        dialog.findViewById(R.id.btn_increment_rooms).setOnClickListener(view -> {
            int count = Integer.parseInt(tvRoomsCount.getText().toString());
            tvRoomsCount.setText(String.valueOf(++count));
        });

        dialog.findViewById(R.id.btn_decrement_rooms).setOnClickListener(view -> {
            int count = Integer.parseInt(tvRoomsCount.getText().toString());
            if (count > 1) {
                tvRoomsCount.setText(String.valueOf(--count));
            }
        });

        // Done button action
        btnDone.setOnClickListener(view -> {
            String roomGuestInfo = tvPersonsCount.getText() + " Adults, "
                    + tvRoomsCount.getText() + " Rooms";
            tvRoomGuestInfo.setText(roomGuestInfo);
            dialog.dismiss();
            numberOfPersons = Integer.parseInt(tvPersonsCount.getText().toString());
            numberOfRooms = Integer.parseInt(tvRoomsCount.getText().toString());

            Toast.makeText(this, "Room and Guest info saved", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    // Searches available rooms based on the location, selected dates, number of guests
    private void searchAvailableRooms(String location, String selectedCheckIn, String selectedCheckOut) {
        db.collection("TestHotel")
                .whereEqualTo("location", location)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Hotel> availableHotels = new ArrayList<>();

                    for (DocumentSnapshot hotelSnapshot : queryDocumentSnapshots.getDocuments()) {
                        String name = hotelSnapshot.getString("name");
                        double latitude = hotelSnapshot.getDouble("latitude");
                        double longitude = hotelSnapshot.getDouble("longitude");
                        int availability = hotelSnapshot.getLong("availability").intValue();
                        double price = hotelSnapshot.getDouble("price");
                        double rating = hotelSnapshot.getDouble("rating");

                        // Check if the availability matches the number of persons
                        if (availability >= numberOfPersons) {
                            // Add the room to the list
                            availableHotels.add(new Hotel(name, location, latitude, longitude, availability, price, rating));
                        }
                    }

                    if (availableHotels.isEmpty()) {
                        Toast.makeText(this, "No rooms available for the selected criteria", Toast.LENGTH_SHORT).show();
                    } else {
                        showAvailableRooms(availableHotels);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to search rooms: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    // Launches the ResultActivity with the list of available rooms
    private void showAvailableRooms(List<Hotel> availableHotels) {
        Intent intent = new Intent(this, SearchResultActivity.class);
        intent.putExtra("availableHotels", (Serializable) availableHotels);
        intent.putExtra("location", locationSpinner.getSelectedItem().toString());  // Pass selected location
        startActivity(intent);
    }
}
