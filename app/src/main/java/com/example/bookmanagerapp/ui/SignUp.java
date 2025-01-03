package com.example.bookmanagerapp.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookmanagerapp.R;
import com.example.bookmanagerapp.database.DB;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class SignUp extends AppCompatActivity {

    private EditText nameInput, surnameInput, emailInput, passwordInput;
    private Button signupButton;
    private TextView loginLink; // Reference to the login link
    private DB dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup); // Ensure the layout is activity_signup.xml

        // Initialize views
        nameInput = findViewById(R.id.nameInput);
        surnameInput = findViewById(R.id.surnameInput);
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        signupButton = findViewById(R.id.signupButton);
        loginLink = findViewById(R.id.loginLink); // Initialize the login link view

        // Initialize database helper
        dbHelper = new DB(this);

        // Handle signup button click
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSignUp();
            }
        });

        // Handle login link click
        loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Start Login Activity when the login link is clicked
                Intent intent = new Intent(SignUp.this, Login.class);
                startActivity(intent); // Open LoginActivity
            }
        });
    }

    private void handleSignUp() {
        // Get input values
        String name = nameInput.getText().toString().trim();
        String surname = surnameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        // Validate inputs
        if (TextUtils.isEmpty(name)) {
            nameInput.setError("Name is required");
            nameInput.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(surname)) {
            surnameInput.setError("Surname is required");
            surnameInput.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Invalid email format");
            emailInput.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        // Password validation
        if (password.length() < 8 || !password.matches(".*[0-9].*") || !password.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
            passwordInput.setError("Password must be at least 8 characters and include one number and one special character");
            passwordInput.requestFocus();
            return;
        }

        // Attempt to sign up with Firebase Auth
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.trim(), password.trim())
                .addOnSuccessListener(this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                        UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                .setDisplayName(name + " " + surname)
                                .build();
                        user.updateProfile(profileUpdate)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(SignUp.this, "Sign up successful!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(SignUp.this, Home.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                        finish(); // Close the sign-up page
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(SignUp.this, "Error during sign-up: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Check if the error is related to "email already exists"
                        if (e.getMessage().contains("The email address is already in use")) {
                            Toast.makeText(SignUp.this, "This email is already registered. Please try logging in or use a different email.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SignUp.this, "Error during sign-up. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
