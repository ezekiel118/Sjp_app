package com.example.sjp_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

// The "implements" declaration is no longer needed with the lambda style
public class UserGradesActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private TextView tvStudentName;
    private ProgressBar progressBar;

    private EditText etProgramming1, etProgramming2, etDataStructures, etAlgorithms,
            etDatabaseManagement, etOperatingSystems, etComputerNetworks,
            etSoftwareEngineering, etWebDevelopment;

    private final DatabaseReference gradesDb = FirebaseDatabase.getInstance(
            "https://sjp-app-e38db-default-rtdb.asia-southeast1.firebasedatabase.app"
    ).getReference("grades");

    private final DatabaseReference usersDb = FirebaseDatabase.getInstance(
            "https://sjp-app-e38db-default-rtdb.asia-southeast1.firebasedatabase.app"
    ).getReference("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_grades);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // --- Corrected Navigation Logic using Lambda Style ---
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                startActivity(new Intent(UserGradesActivity.this, UserProfileActivity.class));
            } else if (id == R.id.nav_logout) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(UserGradesActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else if (id == R.id.nav_appointment) {
                startActivity(new Intent(UserGradesActivity.this, UserAppointmentActivity.class));
            } else if (id == R.id.nav_clearance) {
                startActivity(new Intent(UserGradesActivity.this, UserClearanceActivity.class));
            } else if (id == R.id.nav_grade) {
                Toast.makeText(this, "Already on Grades screen", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(UserGradesActivity.this, UserDashboard.class));
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        tvStudentName = findViewById(R.id.user_name);
        progressBar = findViewById(R.id.progress_bar);
        if (progressBar == null) {
            progressBar = new ProgressBar(this);
        }

        etProgramming1 = findViewById(R.id.et_programming1_user);
        etProgramming2 = findViewById(R.id.et_programming2_user);
        etDataStructures = findViewById(R.id.et_datastructures_user);
        etAlgorithms = findViewById(R.id.et_algorithms_user);
        etDatabaseManagement = findViewById(R.id.et_database_user);
        etOperatingSystems = findViewById(R.id.et_operatingsystems_user);
        etComputerNetworks = findViewById(R.id.et_computernetworks_user);
        etSoftwareEngineering = findViewById(R.id.et_softwareengineering_user);
        etWebDevelopment = findViewById(R.id.et_webdevelopment_user);

        makeReadOnly(etProgramming1);
        makeReadOnly(etProgramming2);
        makeReadOnly(etDataStructures);
        makeReadOnly(etAlgorithms);
        makeReadOnly(etDatabaseManagement);
        makeReadOnly(etOperatingSystems);
        makeReadOnly(etComputerNetworks);
        makeReadOnly(etSoftwareEngineering);
        makeReadOnly(etWebDevelopment);

        String uid = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (uid == null) {
            Toast.makeText(this, "Sign in required", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadGrades(uid);
        loadUserName(uid);
    }

    private void loadGrades(String uid) {
        progressBar.setVisibility(View.VISIBLE);
        gradesDb.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(View.GONE);

                etAlgorithms.setText(getStringValue(snapshot, "Algorithms"));
                etComputerNetworks.setText(getStringValue(snapshot, "Computer Networks"));
                etDataStructures.setText(getStringValue(snapshot, "Data Structures"));
                etDatabaseManagement.setText(getStringValue(snapshot, "Database Management"));
                etOperatingSystems.setText(getStringValue(snapshot, "Operating Systems"));
                etProgramming1.setText(getStringValue(snapshot, "Programming 1"));
                etProgramming2.setText(getStringValue(snapshot, "Programming 2"));
                etSoftwareEngineering.setText(getStringValue(snapshot, "Software Engineering"));
                etWebDevelopment.setText(getStringValue(snapshot, "Web Development"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UserGradesActivity.this, "Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserName(String uid) {
        usersDb.child(uid).child("fullName").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String fullName = snapshot.getValue(String.class);
                if (fullName != null) {
                    tvStudentName.setText(fullName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserGradesActivity.this, "Failed to load user name", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getStringValue(DataSnapshot snapshot, String key) {
        Object val = snapshot.child(key).getValue();
        return val != null ? String.valueOf(val) : "";
    }

    private void makeReadOnly(EditText et) {
        et.setFocusable(false);
        et.setFocusableInTouchMode(false);
        et.setClickable(false);
        et.setLongClickable(false);
        et.setCursorVisible(false);
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
