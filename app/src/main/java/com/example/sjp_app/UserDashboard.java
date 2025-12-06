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
    private TextView scheduleTextView;
    private TextView announcementTextView;
    private TextView billingTextView;
    private TextView nameTextView;
    private DatabaseReference usersRef;

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
                Intent intent = new Intent(UserDashboard.this, UserClearanceActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_grade) {
                Intent intent = new Intent(UserDashboard.this, UserGradesActivity.class);
                startActivity(intent);
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

        // Initialize FirebaseManager
        firebaseManager = new FirebaseManager();

        // Load real-time data from Firebase (read-only)
        firebaseManager.loadSchedules(scheduleTextView);
        firebaseManager.loadAnnouncements(announcementTextView);
        firebaseManager.loadBilling(billingTextView);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            usersRef = FirebaseDatabase.getInstance("https://sjp-app-e38db-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users");
            loadUserName(uid);
        }
    }

    private void loadUserName(String userId) {
        usersRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("fullName")) {
                    String fullName = snapshot.child("fullName").getValue(String.class);
                    nameTextView.setText(fullName);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserDashboard.this, "Failed to load user name.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
