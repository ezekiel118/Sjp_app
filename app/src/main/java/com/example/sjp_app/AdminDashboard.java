package com.example.sjp_app;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import admin.AdminProfileActivity;

public class AdminDashboard extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private FirebaseManager firebaseManager;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_dashboard);

        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        Toolbar toolbar = findViewById(R.id.toolbar);

        // Set toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Drawer toggle
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Navigation menu clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                Intent intent = new Intent(AdminDashboard.this, UserListAdminActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_logout) {
                Intent intent = new Intent(AdminDashboard.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else if (id == R.id.nav_clearance) {
                Intent intent = new Intent(AdminDashboard.this, AdminClearanceActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_grade) {
                Intent intent = new Intent(AdminDashboard.this, AdminGradesActivity.class);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Clicked", Toast.LENGTH_SHORT).show();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Initialize FirebaseManager
        firebaseManager = new FirebaseManager();

        // Load real-time data into TextViews
        firebaseManager.loadSchedules(findViewById(R.id.schedule_item));
        firebaseManager.loadAnnouncements(findViewById(R.id.announcments_item));
        firebaseManager.loadBilling(findViewById(R.id.billing_item));

        // ===== Admin Add Buttons =====
        findViewById(R.id.add_S).setOnClickListener(v -> showAddScheduleDialog());
        findViewById(R.id.add_A).setOnClickListener(v -> showAddAnnouncementDialog());
        findViewById(R.id.add_B).setOnClickListener(v -> showAddBillingDialog());

        // ===== Admin Delete Buttons =====
        findViewById(R.id.del_S).setOnClickListener(v -> showDeleteScheduleDialog());
        findViewById(R.id.del_A).setOnClickListener(v -> showDeleteAnnouncementDialog());
        findViewById(R.id.del_B).setOnClickListener(v -> showDeleteBillingDialog());

        // Handle back press
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    if (isEnabled()) {
                        setEnabled(false);
                        getOnBackPressedDispatcher().onBackPressed();
                    }
                }
            }
        });
    }

    private void showAddScheduleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Schedule");

        EditText inputTitle = new EditText(this);
        inputTitle.setHint("Schedule Title");
        EditText inputTime = new EditText(this);
        inputTime.setHint("Time (e.g., 9:00 AM)");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        layout.addView(inputTitle);
        layout.addView(inputTime);
        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = inputTitle.getText().toString().trim();
            String time = inputTime.getText().toString().trim();
            if (!title.isEmpty() && !time.isEmpty()) {
                firebaseManager.addSchedule(title, time);
                Toast.makeText(this, "Schedule added", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showAddAnnouncementDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Announcement");

        EditText inputTitle = new EditText(this);
        inputTitle.setHint("Title");
        EditText inputDesc = new EditText(this);
        inputDesc.setHint("Description");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        layout.addView(inputTitle);
        layout.addView(inputDesc);
        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String title = inputTitle.getText().toString().trim();
            String desc = inputDesc.getText().toString().trim();
            if (!title.isEmpty() && !desc.isEmpty()) {
                firebaseManager.addAnnouncement(title, desc);
                Toast.makeText(this, "Announcement added", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showAddBillingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Billing");

        EditText inputName = new EditText(this);
        inputName.setHint("Name");
        EditText inputBalance = new EditText(this);
        inputBalance.setHint("Balance");
        inputBalance.setInputType(InputType.TYPE_CLASS_NUMBER);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 40, 50, 10);
        layout.addView(inputName);
        layout.addView(inputBalance);
        builder.setView(layout);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            String balanceStr = inputBalance.getText().toString().trim();
            if (!name.isEmpty() && !balanceStr.isEmpty()) {
                int balance = Integer.parseInt(balanceStr);
                firebaseManager.addBilling(name, balance);
                Toast.makeText(this, "Billing added", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showDeleteScheduleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Schedule");

        EditText inputTitle = new EditText(this);
        inputTitle.setHint("Enter Schedule Title to Delete");
        builder.setView(inputTitle);

        builder.setPositiveButton("Delete", (dialog, which) -> {
            String title = inputTitle.getText().toString().trim();
            if (!title.isEmpty()) {
                firebaseManager.deleteScheduleByTitle(title);
                Toast.makeText(this, "Schedule deleted", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showDeleteAnnouncementDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Announcement");

        EditText inputTitle = new EditText(this);
        inputTitle.setHint("Enter Announcement Title to Delete");
        builder.setView(inputTitle);

        builder.setPositiveButton("Delete", (dialog, which) -> {
            String title = inputTitle.getText().toString().trim();
            if (!title.isEmpty()) {
                firebaseManager.deleteAnnouncementByTitle(title);
                Toast.makeText(this, "Announcement deleted", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showDeleteBillingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Billing");

        EditText inputName = new EditText(this);
        inputName.setHint("Enter Name to Delete Billing");
        builder.setView(inputName);

        builder.setPositiveButton("Delete", (dialog, which) -> {
            String name = inputName.getText().toString().trim();
            if (!name.isEmpty()) {
                firebaseManager.deleteBillingByName(name);
                Toast.makeText(this, "Billing entry deleted", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }
}
