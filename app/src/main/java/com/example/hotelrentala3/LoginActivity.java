package com.example.hotelrentala3;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail, editTextPassword;
    private Button buttonLogin, buttonGoToRegister;
    private com.google.android.gms.common.SignInButton buttonSignInWithGoogle;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Check if the user is already logged in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            if(currentUser.getUid().equals("MmZ9jBxplcbv6Ybu7A6EMS39Sfs1")) {
                navigateToAdminActivity();
            } else {
                navigateToHome();
            }
        }

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonGoToRegister = findViewById(R.id.buttonGoToRegister);
        buttonSignInWithGoogle = findViewById(R.id.buttonSignInWithGoogle);

        // Login button (email & password)
        buttonLogin.setOnClickListener(v -> {
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                showToast("Please enter email and password.");
                return;
            }

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if(user.getUid().equals("MmZ9jBxplcbv6Ybu7A6EMS39Sfs1")) {
                                navigateToAdminActivity();
                                showToast("Login successful!");
                            } else {
                                showToast("Login successful!");
                                navigateToHome();
                            }
                        } else {
                            showToast("Login failed: " + task.getException().getMessage());
                        }
                    });
        });

        // Google Sign-In button
        buttonSignInWithGoogle.setOnClickListener(v -> signInWithGoogle());

        // Go to RegisterActivity
        buttonGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    // Google Sign-In method
    private void signInWithGoogle() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                showToast("Google Sign-In failed: " + e.getMessage());
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        mAuth.signInWithCredential(GoogleAuthProvider.getCredential(account.getIdToken(), null))
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user);
                        }
                    } else {
                        showToast("Authentication Failed.");
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser user) {
        String email = user.getEmail();
        String fullName = user.getDisplayName();
        String uid = user.getUid();
        if(uid.equals("MmZ9jBxplcbv6Ybu7A6EMS39Sfs1")) {
            navigateToAdminActivity();
        }
//        final String role;
//        if(uid.equals("MmZ9jBxplcbv6Ybu7A6EMS39Sfs1")) {
//            role = "admin";
//        } else {
//            role = "user";
//        }

        CollectionReference usersCollection = db.collection("Users");
        DocumentReference newUser = usersCollection.document(user.getUid());

        newUser.set(new User(email, fullName))
                .addOnSuccessListener(aVoid -> {
                    showToast("User data saved.");
                    navigateToHome();
                })
                .addOnFailureListener(e -> {
                    showToast("Error saving user data: " + e.getMessage());
                });
    }

    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, OldHomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToAdminActivity() {
        Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
        startActivity(intent);
        finish();
    }

    private void showToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private static class User {
        public String email;
        public String fullName;

        public User() {
        }

        public User(String email, String fullName) {
            this.email = email;
            this.fullName = fullName;
        }
    }
}
