package com.example.sjp_app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import admin.AdminProfileActivity;

public class AdminClearanceActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private DatabaseReference database;
    private final Map<String, OfficeAdminViews> officeViewsMap = new HashMap<>();
    private ValueEventListener clearanceListener;
    private DatabaseReference clearanceRef;

    private static class OfficeAdminViews {
        CheckBox checkBox;
        EditText editText;

        OfficeAdminViews(CheckBox checkBox, EditText editText) {
            this.checkBox = checkBox;
            this.editText = editText;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_clearance);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view_admin);
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

        navigationView.setNavigationItemSelectedListener(this);

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

        database = FirebaseDatabase.getInstance("https://sjp-app-e38db-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference();

        initializeOfficeViews();
        showUserSelectionDialog();
    }

    private void initializeOfficeViews() {
        officeViewsMap.put("Accounting Office", new OfficeAdminViews(findViewById(R.id.cb_accounting), findViewById(R.id.et_accounting)));
        officeViewsMap.put("Admission Office", new OfficeAdminViews(findViewById(R.id.cb_admission), findViewById(R.id.et_admission)));
        officeViewsMap.put("AVP-ACAD", new OfficeAdminViews(findViewById(R.id.cb_avp_acad), findViewById(R.id.et_avp_acad)));
        officeViewsMap.put("Campus Ministry", new OfficeAdminViews(findViewById(R.id.cb_campus_ministry), findViewById(R.id.et_campus_ministry)));
        officeViewsMap.put("Computer Laboratory", new OfficeAdminViews(findViewById(R.id.cb_computer_lab), findViewById(R.id.et_computer_lab)));
        officeViewsMap.put("Digital Laboratory", new OfficeAdminViews(findViewById(R.id.cb_digital_lab), findViewById(R.id.et_digital_lab)));
        officeViewsMap.put("Guidance Office", new OfficeAdminViews(findViewById(R.id.cb_guidance), findViewById(R.id.et_guidance)));
        officeViewsMap.put("Library", new OfficeAdminViews(findViewById(R.id.cb_library), findViewById(R.id.et_library)));
        officeViewsMap.put("Office of Student Affairs", new OfficeAdminViews(findViewById(R.id.cb_osa), findViewById(R.id.et_osa)));
        officeViewsMap.put("Program Heads", new OfficeAdminViews(findViewById(R.id.cb_program_heads), findViewById(R.id.et_program_heads)));
        officeViewsMap.put("Property Custodian", new OfficeAdminViews(findViewById(R.id.cb_property_custodian), findViewById(R.id.et_property_custodian)));
        officeViewsMap.put("Quality Management Office", new OfficeAdminViews(findViewById(R.id.cb_qmo), findViewById(R.id.et_qmo)));
        officeViewsMap.put("Scholarship and Grants Office", new OfficeAdminViews(findViewById(R.id.cb_sgo), findViewById(R.id.et_sgo)));
        officeViewsMap.put("Science Laboratory", new OfficeAdminViews(findViewById(R.id.cb_science_lab), findViewById(R.id.et_science_lab)));
        officeViewsMap.put("Supreme Student Council", new OfficeAdminViews(findViewById(R.id.cb_ssc), findViewById(R.id.et_ssc)));
    }

    private void showUserSelectionDialog() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading");
        progressDialog.setMessage("Fetching users...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        database.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (isFinishing()) return; 
                progressDialog.dismiss();

                final ArrayList<String> userNames = new ArrayList<>();
                final ArrayList<String> userUids = new ArrayList<>();

                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    if ("user".equals(userSnapshot.child("role").getValue(String.class))) {
                        String name = userSnapshot.child("fullName").getValue(String.class);
                        userNames.add(name != null ? name : "Unnamed User");
                        userUids.add(userSnapshot.getKey());
                    }
                }

                if (userNames.isEmpty()) {
                    new AlertDialog.Builder(AdminClearanceActivity.this)
                            .setTitle("No Users Found")
                            .setMessage("Could not find any accounts with the role 'user'. Please check your Firebase database.")
                            .setPositiveButton("OK", (d, w) -> finish())
                            .show();
                } else {
                    new AlertDialog.Builder(AdminClearanceActivity.this)
                            .setTitle("Select User to Edit")
                            .setItems(userNames.toArray(new String[0]), (dialog, which) -> {
                                String selectedUserId = userUids.get(which);
                                loadClearanceForUser(selectedUserId);
                            })
                            .setCancelable(false)
                            .show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                if (isFinishing()) return; 
                progressDialog.dismiss();

                new AlertDialog.Builder(AdminClearanceActivity.this)
                        .setTitle("Database Error")
                        .setMessage("Could not read user list. This is likely a Firebase Rules permission error. Ensure your admin account has read access to the '/users' path.\n\nError: " + databaseError.getMessage())
                        .setPositiveButton("OK", (d, w) -> finish())
                        .show();
            }
        });
    }

    private void loadClearanceForUser(String userId) {
        if (clearanceRef != null && clearanceListener != null) {
            clearanceRef.removeEventListener(clearanceListener);
        }

        clearanceRef = database.child("clearance").child(userId);
        clearanceListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (Map.Entry<String, OfficeAdminViews> entry : officeViewsMap.entrySet()) {
                    String officeName = entry.getKey();
                    OfficeAdminViews views = entry.getValue();

                    views.checkBox.setOnCheckedChangeListener(null);
                    views.editText.setOnFocusChangeListener(null);

                    if (snapshot.hasChild(officeName)) {
                        ClearanceItem item = snapshot.child(officeName).getValue(ClearanceItem.class);
                        views.checkBox.setChecked(item != null && item.cleared);
                        views.editText.setText(item != null ? item.remarks : "");
                    } else {
                        views.checkBox.setChecked(false);
                        views.editText.setText("");
                    }

                    views.checkBox.setOnCheckedChangeListener((buttonView, isChecked) ->
                            clearanceRef.child(officeName).child("cleared").setValue(isChecked));

                    views.editText.setOnFocusChangeListener((v, hasFocus) -> {
                        if (!hasFocus) {
                            clearanceRef.child(officeName).child("remarks").setValue(views.editText.getText().toString());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if(isFinishing()) return;
                Toast.makeText(AdminClearanceActivity.this, "Failed to load clearance data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        clearanceRef.addValueEventListener(clearanceListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (clearanceRef != null && clearanceListener != null) {
            clearanceRef.removeEventListener(clearanceListener);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            startActivity(new Intent(this, AdminDashboard.class));
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, AdminProfileActivity.class));
        } else if (id == R.id.nav_appointment) {
            startActivity(new Intent(this, AdminAppointmentActivity.class));
        } else if (id == R.id.nav_clearance) {
            Toast.makeText(this, "Already on Clearance screen", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_grade) {
            startActivity(new Intent(this, AdminGradesActivity.class));
        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}
