package com.example.hotelrentala3;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.R.drawable;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PromotionActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_promotion);

        countPromotions();
        Button promotionsBtn = findViewById(R.id.promotions);
        promotionsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PromotionActivity.this, NotificationsActivity.class);
                startActivity(intent);
            }
        });


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mAuth = FirebaseAuth.getInstance();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
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

    // Navigate to LoginActivity
    private void navigateToLogin() {
        Intent intent = new Intent(PromotionActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void countPromotions() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Timestamp now = Timestamp.now();

        db.collection("promotions")
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
                    showToast("Failed to check promotions");
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
                    .setSmallIcon(drawable.ic_dialog_info)
                    .setContentTitle("Promotions")
                    .setContentText(promotionCount + " promotions are available. Check them out in the notifications section!")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);
            notificationManager.notify(1, builder.build());
            Log.d("showNotif3", "ccc");
        }
    }

    private void showToast(String message) {
        Toast.makeText(PromotionActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}
