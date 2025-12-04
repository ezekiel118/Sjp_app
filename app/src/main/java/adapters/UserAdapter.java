// app/src/main/java/com/example/sjp_app/adapters/UserAdapter.java
package adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.database.FirebaseDatabase;
import com.example.sjp_app.R;



import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import admin.AdminProfileActivity;
import models.User;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.VH> {
    private final Context ctx;
    private final List<User> orig = new ArrayList<>();
    private final List<User> filtered = new ArrayList<>();

    public UserAdapter(Context ctx, List<User> data) {
        this.ctx = ctx;
        if (data != null) {
            orig.addAll(data);
            filtered.addAll(data);
        }
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(ctx).inflate(R.layout.item_user, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        User u = filtered.get(position);
        holder.tvName.setText(u.getFullName() != null ? u.getFullName() : "(no name)");
        holder.tvContact.setText(u.getContact() != null ? u.getContact() : "");
        holder.tvRole.setText(u.getRole() != null ? u.getRole() : "user");
        holder.itemView.setOnClickListener(v -> {
            Intent i = new Intent(ctx, AdminProfileActivity.class);
            i.putExtra("targetUid", u.getUid());
            ctx.startActivity(i);
        });
    }

    @Override
    public int getItemCount() { return filtered.size(); }

    public void update(List<User> newData) {
        orig.clear();
        orig.addAll(newData);
        filter("");
    }

    public void filter(String q) {
        filtered.clear();
        if (q == null || q.trim().isEmpty()) {
            filtered.addAll(orig);
        } else {
            String s = q.toLowerCase(Locale.ROOT);
            for (User u : orig) {
                if ((u.getFullName() != null && u.getFullName().toLowerCase(Locale.ROOT).contains(s))
                        || (u.getContact() != null && u.getContact().toLowerCase(Locale.ROOT).contains(s))
                        || (u.getUid() != null && u.getUid().toLowerCase(Locale.ROOT).contains(s))) {
                    filtered.add(u);
                }
            }
        }
        notifyDataSetChanged();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName, tvContact, tvRole;
        VH(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvUserName);
            tvContact = itemView.findViewById(R.id.tvUserContact);
            tvRole = itemView.findViewById(R.id.tvUserRole);
        }
    }
}
