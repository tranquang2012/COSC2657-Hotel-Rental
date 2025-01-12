package com.example.hotelrentala3;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class PaymentActivity extends AppCompatActivity {

    private EditText editTextCardNumber, editTextValidFrom, editTextExpirationDate, editTextCVC;
    private Spinner spinnerCardType;
    private Button buttonPay;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private String selectedHotelId;
    private double totalPrice;
    private String selectedCardType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editTextCardNumber = findViewById(R.id.editTextCardNumber);
        editTextValidFrom = findViewById(R.id.editTextCardValidFrom);
        editTextExpirationDate = findViewById(R.id.editTextCardExpiry);
        editTextCVC = findViewById(R.id.editTextCVC);
        spinnerCardType = findViewById(R.id.spinnerCardType);
        buttonPay = findViewById(R.id.buttonMakePayment);

        selectedHotelId = getIntent().getStringExtra("selectedHotelId");
        totalPrice = getIntent().getDoubleExtra("totalPrice", 0.0);

        if (selectedHotelId == null || totalPrice <= 0.0) {
            showToast("Invalid booking details. Please try again.");
            finish();
        }

        spinnerCardType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCardType = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCardType = null;
            }
        });

        buttonPay.setOnClickListener(v -> processPayment());
    }

    private void processPayment() {
        String cardNumber = editTextCardNumber.getText().toString();
        String validFrom = editTextValidFrom.getText().toString();
        String expirationDate = editTextExpirationDate.getText().toString();
        String cvc = editTextCVC.getText().toString();

        // Validate input fields
        if (TextUtils.isEmpty(cardNumber) || TextUtils.isEmpty(validFrom) ||
                TextUtils.isEmpty(expirationDate) || TextUtils.isEmpty(cvc) ||
                TextUtils.isEmpty(selectedCardType)) {
            showToast("Please fill in all fields.");
            return;
        }

        // Fetch the credit card data from Firestore
        fetchCreditCardData(cardNumber, validFrom, expirationDate, cvc);
    }

    private void fetchCreditCardData(String cardNumber, String validFrom, String expirationDate, String cvc) {
        db.collection("CreditCards")
                .whereEqualTo("cardNumber", cardNumber)
                .whereEqualTo("validFrom", validFrom)
                .whereEqualTo("expirationDate", expirationDate)
                .whereEqualTo("cvc", cvc)
                .whereEqualTo("cardType", selectedCardType)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot cardDocument = queryDocumentSnapshots.getDocuments().get(0);
                        Double balance = cardDocument.getDouble("balance");

                        if (balance != null && balance >= totalPrice) {
                            // if enough money, proceed with the payment process
                            updateCardBalance(cardDocument.getId(), balance - totalPrice);
                        } else {
                            showToast("Insufficient balance.");
                        }
                    } else {
                        showToast("Credit card not found.");
                    }
                })
                .addOnFailureListener(e -> showToast("Error fetching credit card data: " + e.getMessage()));
    }

    private void updateCardBalance(String cardDocumentId, double newCardBalance) {
        db.collection("CreditCards")
                .document(cardDocumentId)
                .update("balance", newCardBalance)
                .addOnSuccessListener(aVoid -> {
                    // if card balance updated, record the transaction
                    recordTransaction();
                })
                .addOnFailureListener(e -> showToast("Error updating card balance: " + e.getMessage()));
    }

    private void recordTransaction() {
        String userId = mAuth.getCurrentUser().getUid();

        db.collection("Users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
            String fullName = documentSnapshot.getString("fullName");

            Map<String, Object> transaction = new HashMap<>();
            transaction.put("userId", userId);
            transaction.put("fullName", fullName);
            transaction.put("hotelId", selectedHotelId);
            transaction.put("selectedRoomType", getIntent().getStringExtra("selectedRoomType"));
            transaction.put("checkInDate", getIntent().getStringExtra("checkInDate"));
            transaction.put("numberOfNights", getIntent().getIntExtra("numberOfNights", 0));
            transaction.put("totalPrice", totalPrice);
            transaction.put("timestamp", System.currentTimeMillis());
            transaction.put("status", "Completed");

            db.collection("BookingHistory")
                    .add(transaction)
                    .addOnSuccessListener(aVoid -> {
                        showToast("Payment successful!");
                        updateHotelAvailability();
                    })
                    .addOnFailureListener(e -> showToast("Error recording transaction: " + e.getMessage()));
        }).addOnFailureListener(e -> showToast("Error fetching user details: " + e.getMessage()));
    }

    private void updateHotelAvailability() {
        db.collection("TestHotel")
                .document(selectedHotelId)
                .update("availability", FieldValue.increment(-1))
                .addOnSuccessListener(aVoid -> showToast("Hotel availability updated."))
                .addOnFailureListener(e -> showToast("Error updating hotel availability: " + e.getMessage()));
    }

    private void showToast(String message) {
        Toast.makeText(PaymentActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
