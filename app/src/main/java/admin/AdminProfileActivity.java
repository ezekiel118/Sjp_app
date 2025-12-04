package admin;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sjp_app.R;
import com.example.sjp_app.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AdminProfileActivity extends AppCompatActivity {

    private ListView studentList;
    private SearchView searchView;
    private ArrayAdapter<String> adapter;
    private List<User> usersList = new ArrayList<>();
    private List<String> userNames = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_profile);

        studentList = findViewById(R.id.student_list);
        searchView = findViewById(R.id.searchlist);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, userNames);
        studentList.setAdapter(adapter);

        studentList.setOnItemClickListener((parent, view, position, id) -> {
            String clickedName = (String) parent.getItemAtPosition(position);
            User clickedUser = findUserByName(clickedName);
            if (clickedUser != null) {
                Toast.makeText(this, "Clicked on: " + clickedUser.fullName, Toast.LENGTH_SHORT).show();
            }
        });

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

    private void checkUserRoleAndLoadData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Not logged in. Cannot load data.", Toast.LENGTH_LONG).show();
            return;
        }

        // *** THIS IS THE IMPORTANT CHANGE ***
        // Show the UID of the currently logged-in user.
        Toast.makeText(this, "Auth UID: " + currentUser.getUid(), Toast.LENGTH_LONG).show();

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
                Log.e("FirebaseError", "Role check was cancelled: " + error.getMessage());
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
                userNames.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    User u = ds.getValue(User.class);
                    if (u != null) {
                        u.uid = ds.getKey();
                        usersList.add(u);
                        userNames.add(u.fullName != null ? u.fullName : "(no name)");
                    }
                }
                adapter.notifyDataSetChanged();
                if (!userNames.isEmpty()) {
                    Toast.makeText(AdminProfileActivity.this, "Successfully loaded " + userNames.size() + " users.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Reading user list was cancelled: " + error.getMessage());
            }
        });
    }

    private User findUserByName(String name) {
        for (User user : usersList) {
            if (user.fullName != null && user.fullName.equals(name)) {
                return user;
            }
        }
        return null;
    }
}
