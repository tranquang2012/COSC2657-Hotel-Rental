package com.example.hotelrentala3;

import android.content.Intent;
import android.content.pm.PackageManager;
//import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class AdminActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;
//    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_admin);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        Button addPromotionBtn = findViewById(R.id.addPromotionBtn);
        Button addCouponBtn = findViewById(R.id.addCouponBtn);
        Button addHotelBtn = findViewById(R.id.addHotelBtn);

        addPromotionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPromotionWindow();
            }
        });

        addCouponBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCouponWindow();
            }
        });

        addHotelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showHotelWindow();
            };
        });

        Button buttonLogout = findViewById(R.id.buttonLogout);

        buttonLogout.setOnClickListener(v -> {
            FirebaseUser user = mAuth.getCurrentUser();

            if (user != null) {
                // Check if the user is signed in with Google
                if (user.getProviderData().stream().anyMatch(profile -> profile.getProviderId().equals("google.com"))) {
                    // Sign out from Google
                    mGoogleSignInClient.signOut()
                            .addOnCompleteListener(this, task -> {
                                // Sign out from Firebase
                                mAuth.signOut();
                                navigateToLogin();
                            });
                } else {
                    // If signed in with email/password, just sign out from Firebase
                    mAuth.signOut();
                    navigateToLogin();
                }
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(AdminActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void showPromotionWindow() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.popup_promotion, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        EditText titleEditText = dialogView.findViewById(R.id.title);
        EditText contentEditText = dialogView.findViewById(R.id.content);
        EditText startDateEditText = dialogView.findViewById(R.id.promotionStartDate);
        EditText endDateEditText = dialogView.findViewById(R.id.promotionEndDate);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String title = titleEditText.getText().toString();
            String content = contentEditText.getText().toString();
            String startDate = startDateEditText.getText().toString();
            String endDate = endDateEditText.getText().toString();
            if(validateDate(startDate, endDate)) {
                if (!title.isEmpty() && !content.isEmpty() && !startDate.isEmpty() && !endDate.isEmpty()) {
                    addPromotion(title, content, startDate, endDate);
                } else {
                    showToast("One of the field is empty.");
                }
            } else {
                showToast("Date is provided in wrong format. the date should follow YYYY-MM-DD");
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean validateDate(String startDate, String endDate) {
        String datePattern = "^(\\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$";
        if(!startDate.matches(datePattern) || !endDate.matches(datePattern)) {
            return false;
        } else {
            return true;
        }
    }

    private void addPromotion(String title, String content, String startDate, String endDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date start = null;
        Date end = null;

        try {
            start = dateFormat.parse(startDate);
            end = dateFormat.parse(endDate);
        } catch(ParseException e) {
            e.printStackTrace();
            Log.d("ParseError", "An error occurred while parsing the date format");
            return;
        }

        Map<String, Object> promotion = new HashMap<>();
        promotion.put("title", title);
        promotion.put("content", content);
        promotion.put("from", new Timestamp(start));
        promotion.put("until", new Timestamp(end));

        db.collection("promotions").add(promotion).addOnSuccessListener(documentReference -> {
            showToast("Promotion added successfully");
        }).addOnFailureListener(e -> {
            showToast("Failed to add promotion.");
        });
    }

    private void showHotelWindow() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.popup_hotel, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        EditText nameEditText = dialogView.findViewById(R.id.hotelName);
        EditText descriptionEditText = dialogView.findViewById(R.id.hotelDescription);
        EditText priceEditText = dialogView.findViewById(R.id.hotelPrice);
        EditText availabilityEditText = dialogView.findViewById(R.id.hotelAvailability);
        EditText latitudeEditText = dialogView.findViewById(R.id.hotelLatitude);
        EditText longitudeEditText = dialogView.findViewById(R.id.hotelLongitude);


        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = nameEditText.getText().toString();
            String description = descriptionEditText.getText().toString();
            String price = priceEditText.getText().toString();
            String availability = availabilityEditText.getText().toString();
            String latitude = latitudeEditText.getText().toString();
            String longitude = longitudeEditText.getText().toString();


            if(!name.isEmpty() && !description.isEmpty() && !price.isEmpty() && !availability.isEmpty()) {
                try {
                    int intPrice = Integer.parseInt(price);
                    int intAvailability = Integer.parseInt(availability);
                    double doubleLatitude = Double.parseDouble(latitude);
                    double doubleLongitude = Double.parseDouble(longitude);
                    addHotel(name, description, intPrice, intAvailability, doubleLatitude, doubleLongitude);
                } catch(NumberFormatException e) {
                    showToast("Price must be a valid number");
                }
            } else {
                showToast("One of the field is empty.");
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void addHotel(String name, String description, int price, int availability, double latitude, double longitude) {
        Map<String, Object> hotel = new HashMap<>();
        hotel.put("availability", availability);
        hotel.put("name", name);
        hotel.put("description", description);
        hotel.put("latitude", latitude);
        hotel.put("longitude", longitude);
        hotel.put("price", price);
        hotel.put("rating", 0);

        db.collection("TestHotel").add(hotel).addOnSuccessListener(documentReference -> {
            showToast("Hotel added successfully.");
        }).addOnFailureListener(e -> {
            showToast("Failed to add hotel.");
        });
    }
    private void showCouponWindow() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.popup_coupon, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        EditText codeEditText = dialogView.findViewById(R.id.code);
        EditText discountEditText = dialogView.findViewById(R.id.discount);
        EditText startDateEditText = dialogView.findViewById(R.id.couponStartDate);
        EditText endDateEditText = dialogView.findViewById(R.id.couponEndDate);

        builder.setPositiveButton("Submit", (dialog, which) -> {
            String code = codeEditText.getText().toString();
            String discount = discountEditText.getText().toString();
            String startDate = startDateEditText.getText().toString();
            String endDate = endDateEditText.getText().toString();
            if(validateDate(startDate, endDate)) {
                if (!code.isEmpty() && !discount.isEmpty() && !startDate.isEmpty() && !endDate.isEmpty()) {
                    addCoupon(code, discount, startDate, endDate);
                } else {
                    showToast("One of the field is empty.");
                }
            } else {
                showToast("Date is provided in wrong format. the date should follow YYYY-MM-DD");
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void addCoupon(String code, String discount, String startDate, String endDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Date start = null;
        Date end = null;

        try {
            start = dateFormat.parse(startDate);
            end = dateFormat.parse(endDate);
        } catch(ParseException e) {
            e.printStackTrace();
            Log.d("ParseError", "An error occurred while parsing the date format");
            return;
        }

        Map<String, Object> coupon = new HashMap<>();
        coupon.put("code", code);
        coupon.put("discount", discount);
        coupon.put("from", new Timestamp(start));
        coupon.put("until", new Timestamp(end));

        db.collection("coupons").add(coupon).addOnSuccessListener(documentReference -> {
            showToast("Coupon added successfully");
        }).addOnFailureListener(e -> {
            showToast("Failed to add Coupon.");
        });
    }

    private void showToast(String message) {
        Toast.makeText(AdminActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}