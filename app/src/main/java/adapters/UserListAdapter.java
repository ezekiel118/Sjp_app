package adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sjp_app.R;
import com.example.sjp_app.User;

import java.util.List;

public class UserListAdapter extends ArrayAdapter<User> {

    // Listener interface for item clicks
    public interface OnUserClickListener {
        void onUserClick(User user);
    }

    private final OnUserClickListener clickListener;

    public UserListAdapter(@NonNull Context context, @NonNull List<User> users, OnUserClickListener listener) {
        super(context, 0, users);
        this.clickListener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_user, parent, false);
        }

        User user = getItem(position);

        TextView tvName = convertView.findViewById(R.id.tvUserName);
        TextView tvContact = convertView.findViewById(R.id.tvUserContact);
        TextView tvRole = convertView.findViewById(R.id.tvUserRole);

        if (user != null) {
            tvName.setText(user.getFullName());
            tvContact.setText(user.getContact());
            tvRole.setText(user.getRole());
        }

        // Set the click listener on the item view itself
        convertView.setOnClickListener(v -> {
            if (clickListener != null && user != null) {
                clickListener.onUserClick(user);
            }
        });

        return convertView;
    }
}
