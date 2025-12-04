package admin;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sjp_app.R;
import com.example.sjp_app.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class EditUserActivity extends AppCompatActivity {

    private TextView tvAdminProfileName;
    private EditText etAdminAcademic, etAdminContact;
    private Button btnAdminSave;

    private DatabaseReference userRef;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_profile);

        tvAdminProfileName = findViewById(R.id.tvAdminProfileName);
        etAdminAcademic = findViewById(R.id.etAdminAcademic);
        etAdminContact = findViewById(R.id.etAdminContact);
        btnAdminSave = findViewById(R.id.btnAdminSave);

        String userUid = getIntent().getStringExtra("USER_UID");
        if (userUid == null) {
            Toast.makeText(this, "User UID is missing!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userRef = FirebaseDatabase.getInstance("https://sjp-app-e38db-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users").child(userUid);

        loadUserData();

        btnAdminSave.setOnClickListener(v -> saveUserData());
    }

    private void loadUserData() {
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    tvAdminProfileName.setText(user.fullName != null ? user.fullName : "(No Name)");
                    etAdminAcademic.setText(user.academicStanding != null ? user.academicStanding : "");
                    etAdminContact.setText(user.contact != null ? user.contact : "");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Failed to load user data for editing: " + error.getMessage());
            }
        });
    }

    private void saveUserData() {
        String academicStanding = etAdminAcademic.getText().toString().trim();
        String contact = etAdminContact.getText().toString().trim();

        Map<String, Object> updates = new HashMap<>();
        updates.put("academicStanding", academicStanding);
        updates.put("contact", contact);

        userRef.updateChildren(updates).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(EditUserActivity.this, "User data updated.", Toast.LENGTH_SHORT).show();
                finish(); // Go back to the user list
            } else {
                Toast.makeText(EditUserActivity.this, "Failed to update user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
