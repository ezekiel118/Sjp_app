package com.example.sjp_app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import admin.AdminProfileActivity;
import models.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminGradesActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private String selectedUserId;
    private ProgressBar progressBar;

    private EditText etProgramming1, etProgramming2, etDataStructures, etAlgorithms,
            etDatabaseManagement, etOperatingSystems, etComputerNetworks,
            etSoftwareEngineering, etWebDevelopment;

    private final DatabaseReference gradesDb = FirebaseDatabase.getInstance(
            "https://sjp-app-e38db-default-rtdb.asia-southeast1.firebasedatabase.app"
    ).getReference("grades");
    private final DatabaseReference usersDatabase = FirebaseDatabase.getInstance(
            "https://sjp-app-e38db-default-rtdb.asia-southeast1.firebasedatabase.app"
    ).getReference("users");
    private final DatabaseReference roleDatabase = FirebaseDatabase.getInstance(
            "https://sjp-app-e38db-default-rtdb.asia-southeast1.firebasedatabase.app"
    ).getReference("role");

    private TextView tvStudentName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_grades);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        tvStudentName = findViewById(R.id.tv_student_name);
        progressBar = findViewById(R.id.progress_bar);

        etProgramming1 = findViewById(R.id.et_grade_1);
        etProgramming2 = findViewById(R.id.et_grade_2);
        etDataStructures = findViewById(R.id.et_grade_3);
        etAlgorithms = findViewById(R.id.et_grade_4);
        etDatabaseManagement = findViewById(R.id.et_grade_5);
        etOperatingSystems = findViewById(R.id.et_grade_6);
        etComputerNetworks = findViewById(R.id.et_grade_7);
        etSoftwareEngineering = findViewById(R.id.et_grade_8);
        etWebDevelopment = findViewById(R.id.et_grade_9);

        Button btnSave = findViewById(R.id.btn_save);

        fetchUsersForSelection();

        btnSave.setOnClickListener(v -> saveGrades());
    }

    private void fetchUsersForSelection() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        usersDatabase.orderByChild("role").equalTo("user")
                .addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        progressBar.setVisibility(ProgressBar.GONE);

                        List<User> userList = new ArrayList<>();
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            User u = ds.getValue(User.class);
                            if (u != null) {
                                u.setUid(ds.getKey());
                                userList.add(u);
                            }
                        }

                        if (userList.isEmpty()) {
                            Toast.makeText(AdminGradesActivity.this, "No users found", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            showUserSelectionDialog(userList);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressBar.setVisibility(ProgressBar.GONE);
                        Toast.makeText(AdminGradesActivity.this, "Failed to load users", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    private void showUserSelectionDialog(List<User> users) {
        String[] names = new String[users.size()];
        for (int i = 0; i < users.size(); i++) {
            names[i] = users.get(i).getFullName();
        }

        new AlertDialog.Builder(this)
                .setTitle("Select a user")
                .setItems(names, (dialog, which) -> {
                    User selectedUser = users.get(which);
                    selectedUserId = selectedUser.getUid();
                    tvStudentName.setText(selectedUser.getFullName());
                    loadGrades();
                })
                .setCancelable(false)
                .show();
    }

    private void loadGrades() {
        if (selectedUserId == null) {
            Toast.makeText(this, "No user selected", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(ProgressBar.VISIBLE);
        gradesDb.child(selectedUserId).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                progressBar.setVisibility(ProgressBar.GONE);
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
                progressBar.setVisibility(ProgressBar.GONE);
                Toast.makeText(AdminGradesActivity.this, "Failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String getStringValue(DataSnapshot snapshot, String key) {
        Object val = snapshot.child(key).getValue();
        return val != null ? String.valueOf(val) : "";
    }

    private void saveGrades() {
        if (selectedUserId == null) return;

        Map<String, Object> updates = new HashMap<>();
        updates.put("Algorithms", safeText(etAlgorithms));
        updates.put("Computer Networks", safeText(etComputerNetworks));
        updates.put("Data Structures", safeText(etDataStructures));
        updates.put("Database Management", safeText(etDatabaseManagement));
        updates.put("Operating Systems", safeText(etOperatingSystems));
        updates.put("Programming 1", safeText(etProgramming1));
        updates.put("Programming 2", safeText(etProgramming2));
        updates.put("Software Engineering", safeText(etSoftwareEngineering));
        updates.put("Web Development", safeText(etWebDevelopment));

        progressBar.setVisibility(ProgressBar.VISIBLE);
        gradesDb.child(selectedUserId).updateChildren(updates, (error, ref) -> {
            progressBar.setVisibility(ProgressBar.GONE);
            if (error == null) {
                Toast.makeText(AdminGradesActivity.this, "Grades saved.", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(AdminGradesActivity.this, "Save failed: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private Object safeText(EditText et) {
        String s = et.getText().toString().trim();
        return TextUtils.isEmpty(s) ? "" : s;
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_profile) {
            Intent intent = new Intent(AdminGradesActivity.this, AdminProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            Intent intent = new Intent(AdminGradesActivity.this, MainActivity.class);
        } else if (id == R.id.nav_clearance) {
            Intent intent = new Intent(AdminGradesActivity.this, AdminClearanceActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_grade) {
            Intent intent = new Intent(AdminGradesActivity.this, AdminGradesActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_home) {
            Intent intent = new Intent(AdminGradesActivity.this, AdminDashboard.class);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show();
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
