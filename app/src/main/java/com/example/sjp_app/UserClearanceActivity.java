package com.example.sjp_app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.sjp_app.data.ClearanceItem;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class UserClearanceActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private DatabaseReference database;
    private DatabaseReference usersDatabase;
    private String currentUserId;
    private final Map<String, OfficeUserViews> officeViewsMap = new HashMap<>();

    private TextView userNameTextView;

    private static class OfficeUserViews {
        CheckBox checkBox;
        TextView remarksTextView;

        OfficeUserViews(CheckBox checkBox, TextView remarksTextView) {
            this.checkBox = checkBox;
            this.remarksTextView = remarksTextView;
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_clearance);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
                Intent intent = new Intent(UserClearanceActivity.this, UserProfileActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_logout) {
                Intent intent = new Intent(UserClearanceActivity.this, MainActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_appointment) {
                Toast.makeText(this, "Appointment clicked", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_clearance) {

            } else if (id == R.id.nav_grade) {
                Intent intent = new Intent(UserClearanceActivity.this, UserGradesActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_home) {
                Intent intent = new Intent(UserClearanceActivity.this, UserDashboard.class);
                startActivity(intent);
            }
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });

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

        userNameTextView = findViewById(R.id.user_name);
        database = FirebaseDatabase.getInstance("https://sjp-app-e38db-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("clearance");
        usersDatabase = FirebaseDatabase.getInstance("https://sjp-app-e38db-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users");
        initializeOfficeViews();

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            loadUserName(currentUserId);
            loadClearanceForUser(currentUserId);
        } else {
            Toast.makeText(this, "No user logged in.", Toast.LENGTH_LONG).show();
            finish(); // Close activity if no user is found
        }
    }

    private void loadUserName(String userId) {
        usersDatabase.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.hasChild("fullName")) {
                    String fullName = snapshot.child("fullName").getValue(String.class);
                    userNameTextView.setText(fullName);
                } else {
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser != null) {
                        // Fallback to email if name not found
                        userNameTextView.setText(currentUser.getEmail());
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserClearanceActivity.this, "Failed to load user name.", Toast.LENGTH_SHORT).show();
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser != null) {
                    // Fallback to email on error
                    userNameTextView.setText(currentUser.getEmail());
                }
            }
        });
    }
    

    private void initializeOfficeViews() {
        // This maps office names from Firebase to the IDs in your user_clearance.xml
        officeViewsMap.put("Accounting Office", new OfficeUserViews(findViewById(R.id.cb_accounting_user), findViewById(R.id.tv_accounting_remark)));
        officeViewsMap.put("Admission Office", new OfficeUserViews(findViewById(R.id.cb_admission_user), findViewById(R.id.tv_admission_remark)));
        officeViewsMap.put("AVP-ACAD", new OfficeUserViews(findViewById(R.id.cb_avp_acad_user), findViewById(R.id.tv_avp_acad_remark)));
        officeViewsMap.put("Campus Ministry", new OfficeUserViews(findViewById(R.id.cb_campus_ministry_user), findViewById(R.id.tv_campus_ministry_remark)));
        officeViewsMap.put("Computer Laboratory", new OfficeUserViews(findViewById(R.id.cb_computer_lab_user), findViewById(R.id.tv_computer_lab_remark)));
        officeViewsMap.put("Digital Laboratory", new OfficeUserViews(findViewById(R.id.cb_digital_lab_user), findViewById(R.id.tv_digital_lab_remark)));
        officeViewsMap.put("Guidance Office", new OfficeUserViews(findViewById(R.id.cb_guidance_user), findViewById(R.id.tv_guidance_remark)));
        officeViewsMap.put("Library", new OfficeUserViews(findViewById(R.id.cb_library_user), findViewById(R.id.tv_library_remark)));
        officeViewsMap.put("Office of Student Affairs", new OfficeUserViews(findViewById(R.id.cb_osa_user), findViewById(R.id.tv_osa_remark)));
        officeViewsMap.put("Program Heads", new OfficeUserViews(findViewById(R.id.cb_program_heads_user), findViewById(R.id.tv_program_heads_remark)));
        officeViewsMap.put("Property Custodian", new OfficeUserViews(findViewById(R.id.cb_property_custodian_user), findViewById(R.id.tv_property_custodian_remark)));
        officeViewsMap.put("Quality Management Office", new OfficeUserViews(findViewById(R.id.cb_qmo_user), findViewById(R.id.tv_qmo_remark)));
        officeViewsMap.put("Scholarship and Grants Office", new OfficeUserViews(findViewById(R.id.cb_scholarship_user), findViewById(R.id.tv_scholarship_remark)));
        officeViewsMap.put("Science Laboratory", new OfficeUserViews(findViewById(R.id.cb_science_lab_user), findViewById(R.id.tv_science_lab_remark)));
        officeViewsMap.put("Supreme Student Council", new OfficeUserViews(findViewById(R.id.cb_ssc_user), findViewById(R.id.tv_ssc_remark)));
    }

    private void loadClearanceForUser(String userId) {
        database.child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    for (OfficeUserViews views : officeViewsMap.values()) {
                        views.checkBox.setChecked(false);
                        views.remarksTextView.setText("(no remarks)");
                    }
                    return;
                }

                for (Map.Entry<String, OfficeUserViews> entry : officeViewsMap.entrySet()) {
                    String officeName = entry.getKey();
                    OfficeUserViews views = entry.getValue();

                    if (snapshot.hasChild(officeName)) {
                        ClearanceItem item = snapshot.child(officeName).getValue(ClearanceItem.class);
                        if (item != null) {
                            views.checkBox.setChecked(item.cleared);
                            if (item.remarks != null && !item.remarks.isEmpty()) {
                                views.remarksTextView.setText(item.remarks);
                            } else {
                                views.remarksTextView.setText("(cleared)");
                            }
                        } else {
                            views.checkBox.setChecked(false);
                            views.remarksTextView.setText("(not specified)");
                        }
                    } else {
                        views.checkBox.setChecked(false);
                        views.remarksTextView.setText("(not specified)");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserClearanceActivity.this, "Failed to load clearance data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
