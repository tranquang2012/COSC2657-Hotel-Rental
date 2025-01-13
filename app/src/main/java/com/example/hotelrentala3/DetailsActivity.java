package com.example.hotelrentala3;

import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class DetailsActivity extends AppCompatActivity {

    private TextView tvDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        tvDetails = findViewById(R.id.tvDetails);

        HistoryActivity.Booking booking = (HistoryActivity.Booking) getIntent().getSerializableExtra("booking");
        if (booking != null) {
            String details = "Full Name: " + booking.getFullName() + "\n"
                    + "Check-In Date: " + booking.getCheckInDate() + "\n"
                    + "Number of Nights: " + booking.getNumberOfNights() + "\n"
                    + "Room Type: " + booking.getSelectedRoomType() + "\n"
                    + "Final Price: $" + booking.getFinalPrice() + "\n"
                    + "Status: " + booking.getStatus();
            tvDetails.setText(details);
        }
    }
}
