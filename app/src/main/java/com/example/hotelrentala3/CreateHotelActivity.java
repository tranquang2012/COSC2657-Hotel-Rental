package com.example.hotelrentala3;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateHotelActivity extends AppCompatActivity {

    private EditText hotelNameEditText, hotelDescriptionEditText, hotelPriceEditText;
    private Button createHotelButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_hotel);

        hotelNameEditText = findViewById(R.id.editTextHotelName);
        hotelDescriptionEditText = findViewById(R.id.editTextDescription);
        hotelPriceEditText = findViewById(R.id.editTextPricePerNight);
        createHotelButton = findViewById(R.id.buttonSaveHotel);

        db = FirebaseFirestore.getInstance();

        createHotelButton.setOnClickListener(v -> createHotel());
    }

    private void createHotel() {
        String hotelName = hotelNameEditText.getText().toString().trim();
        String hotelDescription = hotelDescriptionEditText.getText().toString().trim();
        String hotelPriceString = hotelPriceEditText.getText().toString().trim();

        if (hotelName.isEmpty() || hotelDescription.isEmpty() || hotelPriceString.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
            return;
        }

        // convert price to double
        double hotelPrice = Double.parseDouble(hotelPriceString);

        Map<String, Object> hotelData = new HashMap<>();
        hotelData.put("hotelName", hotelName);
        hotelData.put("hotelDescription", hotelDescription);
        hotelData.put("hotelPrice", hotelPrice);

        db.collection("Hotels")
                .add(hotelData)  // This will auto-generate a document ID
                .addOnSuccessListener(documentReference -> {
                    String hotelId = documentReference.getId();
                    Toast.makeText(CreateHotelActivity.this, "Hotel created successfully with ID: " + hotelId, Toast.LENGTH_SHORT).show();
                    finish();
                    Map<String, Object> updateHotelId = new HashMap<>();
                    updateHotelId.put("hotelId", hotelId);

                    documentReference.update(updateHotelId);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(CreateHotelActivity.this, "Error creating hotel.", Toast.LENGTH_SHORT).show();
                });
    }
}
