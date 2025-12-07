package com.example.sjp_app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

import admin.AdminProfileActivity;

public class AdminAppointmentActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerViewUsers;
    private DatabaseReference appointmentsRef;
    private DatabaseReference usersRef;
    private List<UserItem> userList = new ArrayList<>();
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_appointment);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        recyclerViewUsers = findViewById(R.id.recyclerViewUsers);
        recyclerViewUsers.setLayoutManager(new LinearLayoutManager(this));

        appointmentsRef = FirebaseDatabase
                .getInstance("https://sjp-app-e38db-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("appointments");

        usersRef = FirebaseDatabase
                .getInstance("https://sjp-app-e38db-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users");

        userAdapter = new UserAdapter();
        recyclerViewUsers.setAdapter(userAdapter);

        loadUsersWithAppointments();
    }

    private void loadUsersWithAppointments() {
        appointmentsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                userList.clear();
                long totalUsers = task.getResult().getChildrenCount();
                if (totalUsers == 0) {
                    userAdapter.notifyDataSetChanged();
                    return;
                }

                for (DataSnapshot appointmentSnapshot : task.getResult().getChildren()) {
                    String userId = appointmentSnapshot.getKey();
                    if (userId == null) continue;

                    boolean hasUnread = false;
                    for (DataSnapshot appSnap : appointmentSnapshot.getChildren()) {
                        Boolean read = appSnap.child("readByAdmin").getValue(Boolean.class);
                        if (read == null || !read) {
                            hasUnread = true;
                            break;
                        }
                    }

                    boolean finalHasUnread = hasUnread;
                    usersRef.child(userId).get().addOnCompleteListener(userTask -> {
                        if (userTask.isSuccessful() && userTask.getResult() != null) {
                            User user = userTask.getResult().getValue(User.class);
                            if (user != null) {
                                userList.add(new UserItem(userId, user, finalHasUnread, appointmentSnapshot));
                            }
                        } else {
                            userList.add(new UserItem(userId, new User(userId, "Unknown User", ""), finalHasUnread, appointmentSnapshot));
                        }

                        if (userList.size() >= totalUsers) {
                            userAdapter.notifyDataSetChanged();
                        }
                    });
                }
            } else {
                Toast.makeText(AdminAppointmentActivity.this, "Failed to load appointments. Check security rules.", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_appointment) {
            Toast.makeText(this, "Already on Appointment screen", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_home) {
            startActivity(new Intent(this, AdminDashboard.class));
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, AdminProfileActivity.class));
        } else if (id == R.id.nav_clearance) {
            startActivity(new Intent(this, AdminClearanceActivity.class));
        } else if (id == R.id.nav_grade) {
            Intent intent = new Intent(this, AdminGradesActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
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

    static class UserItem {
        String userId;
        User user;
        boolean hasUnread;
        DataSnapshot appointmentSnapshot;

        UserItem(String userId, User user, boolean hasUnread, DataSnapshot appointmentSnapshot) {
            this.userId = userId;
            this.user = user;
            this.hasUnread = hasUnread;
            this.appointmentSnapshot = appointmentSnapshot;
        }
    }

    class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            UserItem userItem = userList.get(position);
            holder.tvUserName.setText(userItem.user.getFullName());
            holder.tvUserContact.setText(userItem.user.getContact());
            holder.tvUserRole.setText(userItem.user.getRole());
            holder.redDot.setVisibility(userItem.hasUnread ? View.VISIBLE : View.GONE);

            holder.itemView.setOnClickListener(v -> showAppointments(userItem));
        }

        @Override
        public int getItemCount() {
            return userList.size();
        }

        class UserViewHolder extends RecyclerView.ViewHolder {
            TextView tvUserName, tvUserContact, tvUserRole;
            View redDot;

            UserViewHolder(@NonNull View itemView) {
                super(itemView);
                tvUserName = itemView.findViewById(R.id.tvUserName);
                tvUserContact = itemView.findViewById(R.id.tvUserContact);
                tvUserRole = itemView.findViewById(R.id.tvUserRole);
                redDot = itemView.findViewById(R.id.redDot);
            }
        }
    }

    private void showAppointments(UserItem userItem) {
        StringBuilder message = new StringBuilder();
        int count = 0;
        for (DataSnapshot appSnap : userItem.appointmentSnapshot.getChildren()) {
            count++;
            String reason = appSnap.child("reason").getValue(String.class);
            String date = appSnap.child("date").getValue(String.class);
            message.append("Appointment ").append(count).append(":\n");
            message.append("Date: ").append(date).append("\n");
            message.append("Reason: ").append(reason).append("\n\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("Appointments for " + userItem.user.getFullName())
                .setMessage(message.toString())
                .setPositiveButton("Mark as Read", (dialog, which) -> {
                    for (DataSnapshot appSnap : userItem.appointmentSnapshot.getChildren()) {
                        appSnap.getRef().child("readByAdmin").setValue(true);
                    }
                    loadUsersWithAppointments();
                    Toast.makeText(this, "Marked as read", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Delete All", (dialog, which) -> {
                    userItem.appointmentSnapshot.getRef().removeValue()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(AdminAppointmentActivity.this, "Appointments deleted.", Toast.LENGTH_SHORT).show();
                                loadUsersWithAppointments();
                            })
                            .addOnFailureListener(e -> Toast.makeText(AdminAppointmentActivity.this, "Delete failed. Check rules.", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
