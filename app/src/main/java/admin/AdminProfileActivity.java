package admin;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.sjp_app.MainActivity;
import com.example.sjp_app.R;
import com.example.sjp_app.User;
import com.example.sjp_app.UserClearanceActivity;
import com.example.sjp_app.UserDashboard;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import adapters.UserListAdapter;

public class AdminProfileActivity extends AppCompatActivity implements UserListAdapter.OnUserClickListener {

    private ListView studentList;
    private SearchView searchView;
    private UserListAdapter adapter;
    private List<User> usersList = new ArrayList<>();

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_profile);

        studentList = findViewById(R.id.student_list);
        searchView = findViewById(R.id.searchlist);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.navigation_view);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_profile) {
                Toast.makeText(this, "Already on Profile", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.nav_logout) {
                Intent intent = new Intent(AdminProfileActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else if (id == R.id.nav_home) {
                startActivity(new Intent(AdminProfileActivity.this, UserDashboard.class));
            } else if (id == R.id.nav_clearance) {
                startActivity(new Intent(AdminProfileActivity.this, UserClearanceActivity.class));
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

        adapter = new UserListAdapter(this, usersList, this);
        studentList.setAdapter(adapter);

        checkUserRoleAndLoadData();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    @Override
    public void onUserClick(User user) {
        showEditUserDialog(user);
    }

    private void showEditUserDialog(User user) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit User");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_user, null);
        builder.setView(view);

        EditText etAcademicStanding = view.findViewById(R.id.et_academic_standing);
        EditText etContact = view.findViewById(R.id.et_contact);

        // Set existing values
        etAcademicStanding.setText(user.getAcademicStanding());
        etContact.setText(user.getContact());

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newAcademicStanding = etAcademicStanding.getText().toString().trim();
            String newContact = etContact.getText().toString().trim();

            if(newAcademicStanding.isEmpty() || newContact.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            updateUserInFirebase(user.getUid(), newAcademicStanding, newContact);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    private void updateUserInFirebase(String userId, String academicStanding, String contact) {
        DatabaseReference userRef = FirebaseDatabase.getInstance("https://sjp-app-e38db-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users").child(userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("academicStanding", academicStanding);
        updates.put("contact", contact);

        userRef.updateChildren(updates)
                .addOnSuccessListener(aVoid -> Toast.makeText(AdminProfileActivity.this, "User updated successfully", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(AdminProfileActivity.this, "Failed to update user", Toast.LENGTH_SHORT).show());
    }

    private void checkUserRoleAndLoadData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Not logged in. Cannot load data.", Toast.LENGTH_LONG).show();
            return;
        }

        DatabaseReference userRoleRef = FirebaseDatabase.getInstance("https://sjp-app-e38db-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users").child(currentUser.getUid()).child("role");

        userRoleRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String role = snapshot.getValue(String.class);
                if ("admin".equals(role)) {
                    loadUsersFromFirebase();
                } else {
                    Toast.makeText(AdminProfileActivity.this, "Access Denied: Role is '" + (role != null ? role : "null") + "'", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(AdminProfileActivity.this, "Error checking user role.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void loadUsersFromFirebase() {
        DatabaseReference ref = FirebaseDatabase.getInstance("https://sjp-app-e38db-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User u = ds.getValue(User.class);
                    if (u != null) {
                        u.setUid(ds.getKey());
                        usersList.add(u);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }
}
