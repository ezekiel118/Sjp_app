package com.example.sjp_app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import adapters.UserAdapter;
import models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class AdminSelectUserActivity extends AppCompatActivity implements UserAdapter.OnUserClickListener {

    private ProgressBar progressBar;
    private TextView tvEmpty;
    private UserAdapter adapter;
    private final List<User> userList = new ArrayList<>();

    // Use your provided DB URL
    private final DatabaseReference usersDb = FirebaseDatabase.getInstance(
            "https://sjp-app-e38db-default-rtdb.asia-southeast1.firebasedatabase.app"
    ).getReference("users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_select_user);

        RecyclerView rvUsers = findViewById(R.id.rv_users);
        progressBar = findViewById(R.id.progress_bar);
        tvEmpty = findViewById(R.id.tv_empty);

        adapter = new UserAdapter(this, userList, this);
        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(adapter);

        loadUsers();
    }

    private void loadUsers() {
        progressBar.setVisibility(View.VISIBLE);
        usersDb.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int initialSize = userList.size();
                userList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User u = ds.getValue(User.class);
                    if (u == null) continue;
                    u.setUid(ds.getKey());
                    if ("user".equalsIgnoreCase(u.getRole())) {
                        userList.add(u);
                    }
                }
                progressBar.setVisibility(View.GONE);
                adapter.notifyItemRangeInserted(initialSize, userList.size() - initialSize);
                tvEmpty.setVisibility(userList.isEmpty() ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AdminSelectUserActivity.this, "Load failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onUserClick(User user) {
        Intent i = new Intent(this, AdminGradesActivity.class);
        i.putExtra("selectedUserId", user.getUid());
        i.putExtra("selectedUserName", user.getFullName());
        startActivity(i);
    }
}
