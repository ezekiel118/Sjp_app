// app/src/main/java/com/example/sjp_app/user/UserProfileActivity.java
package com.example.sjp_app;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.FirebaseDatabase;
import com.example.sjp_app.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

public class UserProfileActivity extends AppCompatActivity {
    private TextView tvName, tvAcademicItem, tvContactItem;
    private DatabaseReference usersRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_profile);

        tvName = findViewById(R.id.name);
        tvAcademicItem = findViewById(R.id.academicS_item);
        tvContactItem = findViewById(R.id.contact_item);

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }

        usersRef = FirebaseDatabase.getInstance().getReference("users");
        loadMyInfo();
    }

    private void loadMyInfo() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                tvName.setText(snapshot.child("fullName").getValue(String.class));
                tvAcademicItem.setText(snapshot.child("academicStanding").getValue(String.class));
                tvContactItem.setText(snapshot.child("contact").getValue(String.class));
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
