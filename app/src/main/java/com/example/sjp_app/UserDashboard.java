package com.example.sjp_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserDashboard extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    // FirebaseManager instance for reading data
    private FirebaseManager firebaseManager;

    // TextViews for displaying data
    private TextView scheduleTextView, announcementTextView, billingTextView, nameTextView, courseIdTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_dashboard);

        // Initialize views
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);

        // Bind TextViews
        scheduleTextView = findViewById(R.id.schedule_item);
        announcementTextView = findViewById(R.id.announcments_item);
        billingTextView = findViewById(R.id.billing_item);
        nameTextView = findViewById(R.id.name);
        courseIdTextView = findViewById(R.id.course_id);

        // Set toolbar as action bar
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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

        // Handle menu item clicks
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                Intent intent = new Intent(UserDashboard.this, UserProfileActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_logout) {
                Intent intent = new Intent(UserDashboard.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else if (id == R.id.nav_appointment) {
                Toast.makeText(this, "Appointment clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_clearance) {
                Toast.makeText(this, "Clearance clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_grade) {
                Toast.makeText(this, "Grade clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_home) {
                Toast.makeText(this, "Home clicked", Toast.LENGTH_SHORT).show();
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

        // Modern back press handler
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });

        // Initialize FirebaseManager for public data
        firebaseManager = new FirebaseManager();
        firebaseManager.loadSchedules(scheduleTextView);
        firebaseManager.loadAnnouncements(announcementTextView);
        firebaseManager.loadBilling(billingTextView);

        // Load the specific user's profile info
        loadUserProfile();
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            // Not logged in, can't load a profile
            return;
        }
        String uid = currentUser.getUid();
        DatabaseReference userRef = FirebaseDatabase.getInstance("https://sjp-app-e38db-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fullName = snapshot.child("fullName").getValue(String.class);
                    String courseId = snapshot.child("course_id").getValue(String.class);

                    if (nameTextView != null && fullName != null) {
                        nameTextView.setText(fullName);
                    }

                    // Check if the course ID TextView exists before trying to set its text
                    if (courseIdTextView != null && courseId != null) {
                        courseIdTextView.setText(courseId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserDashboard.this, "Failed to load user profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
