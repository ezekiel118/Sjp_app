package com.example.sjp_app;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class UserAppointmentActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private TextInputLayout reasonInputLayout;
    private TextInputEditText reasonEditText;
    private Button btnSelectDate, btnGetPriority, btnSend;
    private String selectedDate = "";
    private int priorityNumber = 0;

    private DatabaseReference appointmentsRef;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_appointment);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        appointmentsRef = FirebaseDatabase
                .getInstance("https://sjp-app-e38db-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("appointments")
                .child(currentUserId);

        reasonInputLayout = findViewById(R.id.reason_input_layout);
        reasonEditText = findViewById(R.id.et_reason);
        btnSelectDate = findViewById(R.id.button);
        btnGetPriority = findViewById(R.id.button2);
        btnSend = findViewById(R.id.button3);

        btnSelectDate.setOnClickListener(v -> openDatePicker());

        btnGetPriority.setOnClickListener(v -> {
            priorityNumber = generatePriorityNumber();
            Toast.makeText(this, "Your Priority Number: " + priorityNumber, Toast.LENGTH_SHORT).show();
        });

        btnSend.setOnClickListener(v -> sendAppointment());
    }

    private void openDatePicker() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, month1, dayOfMonth) -> {
                    month1 += 1;
                    selectedDate = year1 + "-" + month1 + "-" + dayOfMonth;
                    Toast.makeText(this, "Selected Date: " + selectedDate, Toast.LENGTH_SHORT).show();
                },
                year, month, day
        );
        datePickerDialog.show();
    }

    private int generatePriorityNumber() {
        return (int) (Math.random() * 100) + 1;
    }

    private void sendAppointment() {
        if (reasonEditText == null) {
            Toast.makeText(this, "Reason input not found", Toast.LENGTH_SHORT).show();
            return;
        }
        String reason = reasonEditText.getText().toString().trim();
        if (reason.isEmpty()) {
            reasonInputLayout.setError("Please enter a reason");
            return;
        } else {
            reasonInputLayout.setError(null);
        }

        if (selectedDate.isEmpty()) {
            Toast.makeText(this, "Please select a date", Toast.LENGTH_SHORT).show();
            return;
        }

        if (priorityNumber == 0) {
            Toast.makeText(this, "Please get a priority number", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> appointment = new HashMap<>();
        appointment.put("reason", reason);
        appointment.put("date", selectedDate);
        appointment.put("priorityNumber", priorityNumber);
        appointment.put("status", "pending");

        appointmentsRef.push().setValue(appointment)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Appointment saved successfully", Toast.LENGTH_SHORT).show();
                    reasonEditText.setText("");
                    selectedDate = "";
                    priorityNumber = 0;
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to save appointment: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_appointment) {
            // Already here
        } else if (id == R.id.nav_home) {
            startActivity(new Intent(this, UserDashboard.class));
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, UserProfileActivity.class));
        } else if (id == R.id.nav_clearance) {
            startActivity(new Intent(this, UserClearanceActivity.class));
        } else if (id == R.id.nav_grade) {
            startActivity(new Intent(this, UserGradesActivity.class));
        } else if (id == R.id.nav_logout) {
            startActivity(new Intent(this, MainActivity.class));
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
