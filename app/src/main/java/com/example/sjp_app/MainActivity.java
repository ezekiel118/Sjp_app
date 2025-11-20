package com.example.sjp_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    EditText emailField, passwordField;
    Button loginButton;

    FirebaseAuth auth;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Connect UI IDs
        emailField = findViewById(R.id.emailField);
        passwordField = findViewById(R.id.passwordField);
        loginButton = findViewById(R.id.loginButton);

        // Firebase instances
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loginButton.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = emailField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = auth.getCurrentUser().getUid();

                    // Check role from Firestore
                    db.collection("users").document(uid).get()
                            .addOnSuccessListener(document -> {
                                if (document.exists()) {
                                    String role = document.getString("role");

                                    if ("admin".equals(role)) {
                                        Toast.makeText(this, "Admin Login Successful", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(MainActivity.this, AdminDashboard.class));
                                    } else if ("user".equals(role)) {
                                        Toast.makeText(this, "User Login Successful", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(MainActivity.this, UserDashboard.class));
                                    } else {
                                        Toast.makeText(this, "Unknown role in Firestore", Toast.LENGTH_SHORT).show();
                                    }

                                } else {
                                    Toast.makeText(this, "User document not found", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Login Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
