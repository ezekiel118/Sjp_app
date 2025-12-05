package com.example.sjp_app;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sjp_app.data.ClearanceItem;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AdminClearanceActivity extends AppCompatActivity {

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

        database = FirebaseDatabase.getInstance("https://sjp-app-e38db-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference();

        initializeOfficeViews();
        showUserSelectionDialog(); // Simplified to a single, more robust method
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
                if (isFinishing()) return; // Safety check
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
                if (isFinishing()) return; // Safety check
                progressDialog.dismiss();

                // Show a more descriptive error dialog
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
}
