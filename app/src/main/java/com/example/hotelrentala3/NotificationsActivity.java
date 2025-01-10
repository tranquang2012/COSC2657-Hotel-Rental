package com.example.hotelrentala3;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NotificationsActivity extends AppCompatActivity {

    private LinearLayout promotionsLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        promotionsLayout = findViewById(R.id.promotionsLayout);
        loadPromotions();
    }

    private void loadPromotions() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Timestamp now = Timestamp.now();

        db.collection("promotions")
                .whereLessThanOrEqualTo("from", now)
                .whereGreaterThanOrEqualTo("until", now)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Map<String, Object>> promotions = new ArrayList<>();
                    for(DocumentSnapshot document : queryDocumentSnapshots) {
                        promotions.add(document.getData());
                    }
                    showPromotions(promotions);
                }).addOnFailureListener(e -> {
                    showToast("Failed to get promotions.");
                });
    }

    private void showPromotions(List<Map<String, Object>> promotions) {
        promotionsLayout.removeAllViews();
        for(Map<String, Object> promotion: promotions) {
            String title = (String) promotion.get("title");
            String content = (String) promotion.get("content");
            Timestamp startDate = (Timestamp) promotion.get("from");
            Timestamp endDate = (Timestamp) promotion.get("until");

            LinearLayout promotionLayout = new LinearLayout(this);
            promotionLayout.setOrientation(LinearLayout.VERTICAL);
            promotionLayout.setPadding(10, 10, 10, 10);

            TextView titleText = new TextView(this);
            titleText.setText(title);
            titleText.setTextSize(15);

            TextView contentText = new TextView(this);
            contentText.setText(content);
            contentText.setTextSize(15);

            TextView dateText = new TextView(this);
            dateText.setText("Date: " + (startDate.toDate().toString()) + " ~ " + (endDate.toDate().toString()));
            dateText.setTextSize(15);

            View line = new View(this);
            LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 2);
            line.setLayoutParams(lineParams);

            promotionLayout.addView(titleText);
            promotionLayout.addView(contentText);
            promotionLayout.addView(dateText);
            promotionLayout.addView(line);

            promotionsLayout.addView(promotionLayout);
        }
    }

    private void showToast(String message) {
        Toast.makeText(NotificationsActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}