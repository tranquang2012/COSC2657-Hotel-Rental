package com.example.hotelrentala3;

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

import com.example.hotelrentala3.Model.Hotel;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class RoomSelectionActivity extends AppCompatActivity {
    private EditText editTextCouponCode;
    private Spinner spinnerRoomType;
    private Button buttonNext, buttonApplyCoupon;
    private TextView textViewCheckInDate, textViewCheckOutDate, textViewNumberOfNights, textViewPrice;
    private FirebaseFirestore db;

    private Hotel selectedHotel;
    private String selectedRoomType;
    private int numberOfNights = 0;
    private String checkInDate;
    private String checkOutDate;
    private double hotelPrice;
    private double discount = 0;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_selection);

        db = FirebaseFirestore.getInstance();

        textViewCheckInDate = findViewById(R.id.textViewCheckInDate);
        textViewCheckOutDate = findViewById(R.id.textViewCheckOutDate);
        textViewNumberOfNights = findViewById(R.id.textViewNumberOfNights);
        textViewPrice = findViewById(R.id.textViewPrice);
        editTextCouponCode = findViewById(R.id.editTextCouponCode);
        spinnerRoomType = findViewById(R.id.spinnerRoomType);
        buttonNext = findViewById(R.id.buttonNext);
        buttonApplyCoupon = findViewById(R.id.buttonApplyCoupon);

        selectedHotel = (Hotel) getIntent().getSerializableExtra("selectedHotel");
        if (selectedHotel == null) {
            showToast("Hotel not selected. Please try again.");
            finish();
            return;
        }

        hotelPrice = selectedHotel.getPrice();

        // Retrieve dates from intent if passed
        checkInDate = getIntent().getStringExtra("checkInDate");
        checkOutDate = getIntent().getStringExtra("checkOutDate");

        // If dates are not passed, set default dates
        if (checkInDate == null || checkOutDate == null) {
            setDefaultDates();
        } else {
            // Display passed dates
            textViewCheckInDate.setText("Check-In Date: " + checkInDate);
            textViewCheckOutDate.setText("Check-Out Date: " + checkOutDate);

            // Calculate the number of nights based on passed dates
            calculateNumberOfNights();
        }
        spinnerRoomType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedRoomType = parent.getItemAtPosition(position).toString();
                updatePrice(discount);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedRoomType = null;
            }
        });

        buttonApplyCoupon.setOnClickListener(v -> applyCoupon());

        buttonNext.setOnClickListener(v -> processRoomSelection());
    }

    private void setDefaultDates() {
        Calendar calendar = Calendar.getInstance();
        checkInDate = dateFormat.format(calendar.getTime());
        textViewCheckInDate.setText("Check-In Date: " + checkInDate);

        // Set default check-out date (next day)
        calendar.add(Calendar.DAY_OF_MONTH, 1);
        checkOutDate = dateFormat.format(calendar.getTime());
        textViewCheckOutDate.setText("Check-Out Date: " + checkOutDate);

        // Calculate default number of nights
        calculateNumberOfNights();
    }

    private void calculateNumberOfNights() {
        try {
            Date checkIn = dateFormat.parse(checkInDate);
            Date checkOut = dateFormat.parse(checkOutDate);

            if (checkIn != null && checkOut != null) {
                long differenceInMillis = checkOut.getTime() - checkIn.getTime();
                numberOfNights = (int) (differenceInMillis / (1000 * 60 * 60 * 24));
                textViewNumberOfNights.setText("Number of Nights: " + numberOfNights);
                updatePrice(discount);
            }
        } catch (ParseException e) {
            showToast("Error calculating number of nights.");
        }
    }

    private void updatePrice(double discount) {
        if (numberOfNights > 0 && selectedHotel != null && selectedRoomType != null) {
            double roomTypeMultiplier = getRoomTypeMultiplier(selectedRoomType);
            double totalPrice = (selectedHotel.getPrice() * numberOfNights * roomTypeMultiplier) - discount;
            textViewPrice.setText(String.format(Locale.getDefault(), "Total Price: $%.2f", totalPrice));
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
            updatePrice(discount);
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
                            updatePrice(discount);
                        }
                    } else {
                        discount = 0;
                        updatePrice(discount);
                        showToast("Invalid coupon code.");
                    }
                })
                .addOnFailureListener(e -> {
                    discount = 0;
                    updatePrice(discount);
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
        updatePrice(discount);
    }


    private void processRoomSelection() {

        if (TextUtils.isEmpty(selectedRoomType)) {
            showToast("Please select a room type.");
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
        intent.putExtra("selectedHotelId", selectedHotel.getHotelId());
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
