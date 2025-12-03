package com.example.sjp_app;

import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseManager {

    private final DatabaseReference schedulesRef;
    private final DatabaseReference announcementsRef;
    private final DatabaseReference billingRef;

    public FirebaseManager() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://sjp-app-e38db-default-rtdb.asia-southeast1.firebasedatabase.app/");
        schedulesRef = database.getReference("schedules");
        announcementsRef = database.getReference("announcements");
        billingRef = database.getReference("billing");
    }

    // ===== CREATE / UPDATE =====
    public void addSchedule(String title, String time) {
        String id = schedulesRef.push().getKey();
        if (id != null) {
            schedulesRef.child(id).child("title").setValue(title);
            schedulesRef.child(id).child("time").setValue(time);
        }
    }

    public void addAnnouncement(String title, String description) {
        String id = announcementsRef.push().getKey();
        if (id != null) {
            announcementsRef.child(id).child("title").setValue(title);
            announcementsRef.child(id).child("description").setValue(description);
        }
    }

    public void addBilling(String name, int balance) {
        String id = billingRef.push().getKey();
        if (id != null) {
            billingRef.child(id).child("name").setValue(name);
            billingRef.child(id).child("balance").setValue(balance);
        }
    }

    // ===== DELETE =====
    public void deleteScheduleByTitle(String titleToDelete) {
        schedulesRef.orderByChild("title").equalTo(titleToDelete)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            child.getRef().removeValue();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseManager", "Failed to delete schedule by title.", error.toException());
                    }
                });
    }

    public void deleteAnnouncementByTitle(String titleToDelete) {
        announcementsRef.orderByChild("title").equalTo(titleToDelete)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            child.getRef().removeValue();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseManager", "Failed to delete announcement by title.", error.toException());
                    }
                });
    }

    public void deleteBillingByName(String nameToDelete) {
        billingRef.orderByChild("name").equalTo(nameToDelete)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot child : snapshot.getChildren()) {
                            child.getRef().removeValue();
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("FirebaseManager", "Failed to delete billing by name.", error.toException());
                    }
                });
    }

    // ===== READ (for Users & Admin) =====
    public void loadSchedules(View view) {
        TextView scheduleTextView = (TextView) view;
        schedulesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StringBuilder builder = new StringBuilder();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String title = child.child("title").getValue(String.class);
                    String time = child.child("time").getValue(String.class);
                    builder.append(title).append(" at ").append(time).append("\n");
                }
                scheduleTextView.setText(builder.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseManager", "Failed to read schedules.", error.toException());
            }
        });
    }

    public void loadAnnouncements(View view) {
        TextView announcementTextView = (TextView) view;
        announcementsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StringBuilder builder = new StringBuilder();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String title = child.child("title").getValue(String.class);
                    String desc = child.child("description").getValue(String.class);
                    builder.append(title).append(": ").append(desc).append("\n");
                }
                announcementTextView.setText(builder.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseManager", "Failed to read announcements.", error.toException());
            }
        });
    }

    public void loadBilling(View view) {
        TextView billingTextView = (TextView) view;
        billingRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                StringBuilder builder = new StringBuilder();
                for (DataSnapshot child : snapshot.getChildren()) {
                    String name = child.child("name").getValue(String.class);
                    Long balance = child.child("balance").getValue(Long.class);
                    builder.append(name).append(": Php ").append(balance).append("\n");
                }
                billingTextView.setText(builder.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseManager", "Failed to read billing.", error.toException());
            }
        });
    }
}
