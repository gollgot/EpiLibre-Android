package ch.epilibre.epilibre.recyclers;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ch.epilibre.epilibre.Config;
import ch.epilibre.epilibre.Models.User;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.http.HttpRequest;
import ch.epilibre.epilibre.http.RequestCallback;

public class RecyclerViewAdapterUsers extends RecyclerView.Adapter<RecyclerViewAdapterUsers.ViewHolder> {


    public class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout layout;
        TextView tvFirstname;
        TextView tvLastname;
        TextView tvEmail;
        TextView tvRole;
        ImageButton btnEdit;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.recyclerUsersLayout);
            tvEmail = itemView.findViewById(R.id.recyclerUsersTvEmail);
            tvFirstname = itemView.findViewById(R.id.recyclerUsersTvFirstname);
            tvLastname = itemView.findViewById(R.id.recyclerUsersTvLastname);
            btnEdit = itemView.findViewById(R.id.recyclerUsersBtnEdit);
            tvRole = itemView.findViewById(R.id.recyclerUsersTvRole);
        }
    }


    private Context context;
    private ViewGroup layout;
    private ArrayList<User> users = new ArrayList<>();

    public RecyclerViewAdapterUsers(Context context, ViewGroup layout, ArrayList<User> users) {
        this.context = context;
        this.layout = layout;
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_recycler_users, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.tvEmail.setText(users.get(position).getEmail());
        holder.tvFirstname.setText(users.get(position).getFirstname());
        holder.tvLastname.setText(users.get(position).getLastname());
        holder.tvRole.setText(users.get(position).getRolePretty());
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

}
