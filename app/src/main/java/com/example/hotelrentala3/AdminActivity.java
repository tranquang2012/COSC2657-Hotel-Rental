package com.example.hotelrentala3;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.zip.Inflater;

public class AdminActivity extends AppCompatActivity {
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_admin);


        db = FirebaseFirestore.getInstance();

        Button addPromotionBtn = findViewById(R.id.addPromitionBtn);
        Button addCouponBtn = findViewById(R.id.addCouponBtn);

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

    public void showCouponWindow() {
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