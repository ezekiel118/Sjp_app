package com.example.sjp_app;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

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
            if (id == R.id.nav_appointment) {
                Toast.makeText(this, "Appointment clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_clearance) {
                Toast.makeText(this, "Clearance clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_grade) {
                Toast.makeText(this, "Grade clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_home) {
                Toast.makeText(this, "Home clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_profile) {
                Toast.makeText(this, "Profile clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_logout) {
                Toast.makeText(this, "Logout clicked", Toast.LENGTH_SHORT).show();
            }
            drawerLayout.closeDrawer(GravityCompat.END);
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
    }
}
