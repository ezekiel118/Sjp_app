package com.example.sjp_app;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UserProfileActivity extends AppCompatActivity {
    private TextView tvName, tvAcademicItem, tvContactItem, tvCourseId;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        tvName = findViewById(R.id.name);
        tvAcademicItem = findViewById(R.id.academicS_item);
        tvContactItem = findViewById(R.id.contact_item);
        tvCourseId = findViewById(R.id.course_id); // Initialize the course ID TextView

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finish(); // Not logged in, so close the activity
            return;
        }

        // Initialize the database reference
        usersRef = FirebaseDatabase.getInstance("https://sjp-app-e38db-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");
        loadMyInfo(currentUser.getUid());
    }

    private void loadMyInfo(String uid) {
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Get the values from the database
                    String fullName = snapshot.child("fullName").getValue(String.class);
                    String academicStanding = snapshot.child("academicStanding").getValue(String.class);
                    String contact = snapshot.child("contact").getValue(String.class);
                    String courseId = snapshot.child("course_id").getValue(String.class);

                    // Set the text for each TextView, with null checks
                    if (tvName != null) {
                        tvName.setText(fullName != null ? fullName : "(No Name)");
                    }
                    if (tvAcademicItem != null) {
                        tvAcademicItem.setText(academicStanding != null ? academicStanding : "(Not set)");
                    }
                    if (tvContactItem != null) {
                        tvContactItem.setText(contact != null ? contact : "(Not set)");
                    }
                    if (tvCourseId != null) {
                        tvCourseId.setText(courseId != null ? courseId : "(Not set)");
                    }
                } else {
                    Toast.makeText(UserProfileActivity.this, "User data not found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "Failed to load profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
