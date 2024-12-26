package com.example.bookmanagerapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.mindrot.jbcrypt.BCrypt; // Import BCrypt

public class SignUp extends AppCompatActivity {

    private EditText nameInput, surnameInput, emailInput, passwordInput;
    private Button signupButton;
    private DB dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup); // Ensure the layout is activity_signup.xml

        // Initialize views
        nameInput = findViewById(R.id.nameInput);
        surnameInput = findViewById(R.id.surnameInput); // Ensure this is correctly hooked up in your XML
        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        signupButton = findViewById(R.id.signupButton);

        // Initialize database helper
        dbHelper = new DB(this);

        // Handle signup button click
        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleSignUp();
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

        // Check if the email already exists
        if (dbHelper.checkEmailExists(email)) {
            emailInput.setError("Email already exists");
            emailInput.requestFocus();
            return;
        }

        // Hash the password before storing it
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Add the new user to the database
        long userId = dbHelper.addUser(name, surname, email, hashedPassword);
        if (userId > 0) {
            Toast.makeText(this, "Sign up successful!", Toast.LENGTH_SHORT).show();
            dbHelper.addSession(true);
            Intent intent = new Intent(SignUp.this, Home.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish(); // Close the sign-up page
        } else {
            Toast.makeText(this, "Error during sign-up. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

}
