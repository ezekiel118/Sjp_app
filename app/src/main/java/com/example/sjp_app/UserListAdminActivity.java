package com.example.sjp_app;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UserListAdminActivity extends AppCompatActivity {

    private ListView listView;
    private SearchView searchView;
    private ArrayAdapter<String> adapter;
    private List<User> users = new ArrayList<>();
    private List<String> userNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_profile);

        listView = findViewById(R.id.student_list);
        searchView = findViewById(R.id.searchlist);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userNames);
        listView.setAdapter(adapter);

        loadUsers();

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

    private void loadUsers() {
        // Be specific with the database URL for consistency and to avoid errors
        DatabaseReference ref = FirebaseDatabase.getInstance("https://sjp-app-e38db-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("FirebaseDebug", "onDataChange in UserListAdminActivity. Children count: " + snapshot.getChildrenCount());

                users.clear();
                userNames.clear();

                for (DataSnapshot ds : snapshot.getChildren()) {
                    User u = ds.getValue(User.class);
                    if (u != null) {
                        // FIX 1: Set the public field directly, as there is no setter method.
                        u.uid = ds.getKey();
                        users.add(u);

                        // FIX 2: Access the public field directly, as there is no getter method.
                        String name = u.fullName;
                        userNames.add(name != null ? name : "(no name)");
                        Log.d("FirebaseDebug", "Loaded user: " + name);
                    } else {
                        Log.w("FirebaseDebug", "User object is null for key: " + ds.getKey() + ". Check for field name mismatches in User.java and your database.");
                    }
                }

                adapter.notifyDataSetChanged();
                Log.d("FirebaseDebug", "Adapter notified. Total users loaded: " + userNames.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // FIX 3: Always log cancellation errors. This is key for debugging rules issues.
                Log.e("FirebaseError", "User list read was cancelled: " + error.getMessage());
            }
        });
    }
}
