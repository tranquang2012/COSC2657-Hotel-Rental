package com.example.hotelrentala3;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;

public class RoomSelectionActivity extends AppCompatActivity {

    private EditText editTextCheckInDate, editTextNumberOfNights, editTextCouponCode;
    private Spinner spinnerRoomType;
    private Button buttonNext, buttonApplyCoupon;
    private TextView textViewPrice;
    private FirebaseFirestore db;

    private String selectedHotelId;
    private double hotelPrice;
    private String selectedRoomType;
    private int numberOfNights = 0;
    private String checkInDate;
    private double discount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_selection);

        db = FirebaseFirestore.getInstance();

        editTextCheckInDate = findViewById(R.id.editTextCheckInDate);
        editTextNumberOfNights = findViewById(R.id.editTextNumberOfNights);
        editTextCouponCode = findViewById(R.id.editTextCouponCode);
        spinnerRoomType = findViewById(R.id.spinnerRoomType);
        buttonNext = findViewById(R.id.buttonNext);
        buttonApplyCoupon = findViewById(R.id.buttonApplyCoupon);
        textViewPrice = findViewById(R.id.textViewPrice);

        selectedHotelId = getIntent().getStringExtra("selectedHotelId");
        if (selectedHotelId == null) {
            showToast("Hotel not selected. Please try again.");
            finish();
        } else {
            fetchHotelData(selectedHotelId);
        }

        editTextCheckInDate.setOnClickListener(v -> openDatePicker());

        spinnerRoomType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRoomType = parent.getItemAtPosition(position).toString();
                updatePrice();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedRoomType = null;
            }
        });

        editTextNumberOfNights.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePrice();
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        buttonApplyCoupon.setOnClickListener(v -> applyCoupon());

        buttonNext.setOnClickListener(v -> processRoomSelection());
    }

    private void fetchHotelData(String hotelId) {
        db.collection("TestHotel")
                .document(hotelId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        hotelPrice = documentSnapshot.getDouble("price");
                        updatePrice();
                    } else {
                        showToast("Hotel not found.");
                    }
                })
                .addOnFailureListener(e -> showToast("Error fetching hotel data: " + e.getMessage()));
    }

    private void openDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year1, month1, dayOfMonth) -> {
                    checkInDate = (month1 + 1) + "/" + dayOfMonth + "/" + year1;
                    editTextCheckInDate.setText(checkInDate);
                }, year, month, day);
        datePickerDialog.show();
    }

    private void updatePrice() {
        String nightsStr = editTextNumberOfNights.getText().toString();
        if (!TextUtils.isEmpty(nightsStr) && hotelPrice > 0) {
            numberOfNights = Integer.parseInt(nightsStr);

            double roomTypeMultiplier = getRoomTypeMultiplier(selectedRoomType);

            double totalPriceBeforeDiscount = hotelPrice * numberOfNights * roomTypeMultiplier;
            double finalPrice = totalPriceBeforeDiscount - discount;

            if (finalPrice < 0) {
                finalPrice = 0;
            }

            textViewPrice.setText(String.format("Total Price: $%.2f", finalPrice));
        }
    }


    private double getRoomTypeMultiplier(String roomType) {
        if ("Single Room".equalsIgnoreCase(roomType)) {
            return 1.0;
        } else if ("Double Room".equalsIgnoreCase(roomType)) {
            return 1.5;
        } else if ("Suite".equalsIgnoreCase(roomType)) {
            return 2.0;
        }
        return 0.0;
    }

    private void applyCoupon() {
        String couponCode = editTextCouponCode.getText().toString().trim();
        if (TextUtils.isEmpty(couponCode)) {

            discount = 0;
            updatePrice();
            showToast("No coupon applied.");
            return;
        }

        // fetch coupon details from Firestore
        db.collection("coupons")
                .whereEqualTo("code", couponCode)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String discountString = queryDocumentSnapshots.getDocuments().get(0).getString("discount");
                        try {
                            double couponDiscount = Double.parseDouble(discountString);
                            applyDiscountLogic(couponDiscount);
                            showToast("Coupon applied successfully!");
                        } catch (NumberFormatException e) {
                            showToast("Invalid discount value in coupon.");
                            discount = 0;
                            updatePrice();
                        }
                    } else {
                        discount = 0;
                        updatePrice();
                        showToast("Invalid coupon code.");
                    }
                })
                .addOnFailureListener(e -> {
                    discount = 0;
                    updatePrice();
                    showToast("Error fetching coupon data: " + e.getMessage());
                });
    }

    private void applyDiscountLogic(double couponDiscount) {
        double totalPriceBeforeDiscount = hotelPrice * numberOfNights * getRoomTypeMultiplier(selectedRoomType);

        if (couponDiscount >= totalPriceBeforeDiscount) {
            // if the discount is greater than or equal to total price, apply a 50% discount instead
            discount = totalPriceBeforeDiscount / 2;
        } else {
            // full discount
            discount = couponDiscount;
        }
        updatePrice();
    }


    private void processRoomSelection() {
        String nightsStr = editTextNumberOfNights.getText().toString();
        if (TextUtils.isEmpty(nightsStr)) {
            showToast("Please enter the number of nights.");
            return;
        }

        numberOfNights = Integer.parseInt(nightsStr);

        if (TextUtils.isEmpty(selectedRoomType)) {
            showToast("Please select a room type.");
            return;
        }

        if (TextUtils.isEmpty(checkInDate)) {
            showToast("Please select a check-in date.");
            return;
        }

        double roomTypeMultiplier = getRoomTypeMultiplier(selectedRoomType);
        double totalPriceBeforeDiscount = hotelPrice * numberOfNights * roomTypeMultiplier;
        double finalPrice = totalPriceBeforeDiscount - discount;

        // make sure finalPrice is non-negative
        if (finalPrice < 0) {
            finalPrice = 0;
        }

        Intent intent = new Intent(this, PaymentActivity.class);
        intent.putExtra("selectedHotelId", selectedHotelId);
        intent.putExtra("selectedRoomType", selectedRoomType);
        intent.putExtra("checkInDate", checkInDate);
        intent.putExtra("numberOfNights", numberOfNights);
        intent.putExtra("finalPrice", finalPrice);
        startActivity(intent);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
