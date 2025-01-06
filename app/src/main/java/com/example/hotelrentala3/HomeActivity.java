package com.example.hotelrentala3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mAuth = FirebaseAuth.getInstance();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Button buttonLogout = findViewById(R.id.buttonLogout);
        Button buttonChooseHotel = findViewById(R.id.buttonChooseHotel);
        Button buttonCreateHotel = findViewById(R.id.buttonCreateHotel);

        // Navigate to hotel selection activity when button is clicked
        buttonChooseHotel.setOnClickListener(v -> navigateToHotelSelection());

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

        // Navigate to CreateHotelActivity
        buttonCreateHotel.setOnClickListener(v -> navigateToCreateHotel());
    }

    // Navigate to HotelSelectionActivity
    private void navigateToHotelSelection() {
        Intent intent = new Intent(HomeActivity.this, HotelSelectionActivity.class);
        startActivity(intent);
    }

    // Navigate to CreateHotelActivity
    private void navigateToCreateHotel() {
        Intent intent = new Intent(HomeActivity.this, CreateHotelActivity.class);
        startActivity(intent);
    }

    // Navigate to LoginActivity
    private void navigateToLogin() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
