package com.example.hotelrentala3;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.hotelrentala3.Model.Hotel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private GoogleSignInClient mGoogleSignInClient;
    private TextView checkInDate;
    private TextView checkOutDate;
    private TextView tvRoomGuestInfo;
    private Spinner locationSpinner;  // Spinner for location selection
    private int numberOfPersons = 1, numberOfRooms = 1;
    private String checkInDateString = null; // Initialize with null
    private String checkOutDateString = null; // Initialize with null

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        countPromotions();
        Button promotionsBtn = findViewById(R.id.promotions);
        promotionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, NotificationsActivity.class);
                startActivity(intent);
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        // Initialize Firestore and views
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        locationSpinner = findViewById(R.id.location_spinner);  // Spinner for city selection
        checkInDate = findViewById(R.id.checkin_date);
        checkOutDate = findViewById(R.id.checkout_date);
        TextView searchButton = findViewById(R.id.search_button);
        tvRoomGuestInfo = findViewById(R.id.tv_room_guest_info);
        Button historyButton = findViewById(R.id.btn_history);
        Button logoutButton = findViewById(R.id.btn_logout);
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

        historyButton.setOnClickListener(view -> {
            String userId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : null;
            if (userId != null) {
                Intent intent = new Intent(HomeActivity.this, HistoryActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
            } else {
                Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            }
        });

        logoutButton.setOnClickListener(v -> {
            FirebaseUser user = auth.getCurrentUser();

            if (user != null) {
                // Check if the user is signed in with Google
                if (user.getProviderData().stream().anyMatch(profile -> profile.getProviderId().equals("google.com"))) {
                    // Sign out from Google
                    mGoogleSignInClient.signOut()
                            .addOnCompleteListener(this, task -> {
                                // Sign out from Firebase
                                auth.signOut();
                                Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            });
                } else {
                    // If signed in with email/password, just sign out from Firebase
                    auth.signOut();
                    Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private void countPromotions() {
        FirebaseFirestore db2 = FirebaseFirestore.getInstance();
        Timestamp now = Timestamp.now();

        db2.collection("promotions")
                .whereLessThanOrEqualTo("from", now)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Log.d("onSuccessTriggered", "aaa");
                    List<Map<String, Object>> promotions = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        Timestamp until = document.getTimestamp("until");

                        if (until != null && until.compareTo(now) >= 0) {
                            promotions.add(document.getData());
                        }
                    }
                    int promotionCount = promotions.size();
                    Log.d("promotionCount", String.valueOf(promotionCount));
                    if (promotionCount > 0) {
                        showNotification(promotionCount);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to check promotions", Toast.LENGTH_SHORT).show();
                });
    }

    private void showNotification(int promotionCount) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT < 26) {
            Log.d("showNotif1", "aaa");
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Promotions")
                    .setContentText(promotionCount + " promotions are available. Check them out in the notifications section!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            notificationManager.notify(1, builder.build());
        } else {
            Log.d("showNotif2", "NotificationManager: " + (notificationManager != null ? "Not null" : "Null"));
            String channelId = "promotions_channel";
            String channelName = "Promotions notifications";
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle("Promotions")
                    .setContentText(promotionCount + " promotions are available. Check them out in the notifications section!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            notificationManager.notify(1, builder.build());
            Log.d("showNotif3", "ccc");
        }
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
                        checkInDateString = checkInDate.getText().toString();
                    } else {
                        checkOutDate.setText(selectedDate);
                        checkOutDateString = checkOutDate.getText().toString();
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
            String roomGuestInfo = tvPersonsCount.getText() + " Persons, "
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
                        String hotelID = hotelSnapshot.getId();
                        String name = hotelSnapshot.getString("name");
                        double latitude = hotelSnapshot.getDouble("latitude");
                        double longitude = hotelSnapshot.getDouble("longitude");
                        int availability = hotelSnapshot.getLong("availability").intValue();
                        double price = hotelSnapshot.getDouble("price");
                        double rating = hotelSnapshot.getDouble("rating");

                        // Check if the availability matches the number of persons
                        if (availability >= numberOfPersons) {
                            // Add the room to the list
                            availableHotels.add(new Hotel(hotelID, name, location, latitude, longitude, availability, price, rating));
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
        intent.putExtra("checkInDate", checkInDateString);
        intent.putExtra("checkOutDate", checkOutDateString);
        startActivity(intent);
    }
}
