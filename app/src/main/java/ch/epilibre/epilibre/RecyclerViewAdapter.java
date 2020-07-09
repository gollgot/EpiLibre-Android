package ch.epilibre.epilibre;

import android.content.Context;
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
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ch.epilibre.epilibre.http.HttpRequest;
import ch.epilibre.epilibre.http.RequestCallback;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private Context context;
    private ViewGroup layout;
    private ArrayList<String> emails = new ArrayList<>();
    private ArrayList<String> firstnames = new ArrayList<>();
    private ArrayList<String> lastnames = new ArrayList<>();
    private ArrayList<Integer> ids = new ArrayList<>();

    private TextView tvTitle;
    private TextView tvNoData;

    public RecyclerViewAdapter(Context context, ViewGroup layout, ArrayList<String> emails, ArrayList<String> firstnames, ArrayList<String> lastnames, ArrayList<Integer> ids, TextView tvTitle, TextView tvNoData) {
        this.context = context;
        this.layout = layout;
        this.emails = emails;
        this.firstnames = firstnames;
        this.lastnames = lastnames;
        this.ids = ids;
        this.tvTitle = tvTitle;
        this.tvNoData = tvNoData;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_recycler_users_pending, parent, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        holder.tvEmail.setText(emails.get(position));
        holder.tvFirstname.setText(firstnames.get(position));
        holder.tvLastname.setText(lastnames.get(position));

        // Confirm the user
        holder.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final HttpRequest httpConfirmUserRequest = new HttpRequest(context, layout, Config.API_BASE_URL + Config.API_USERS_CONFIRM(ids.get(position)), Request.Method.PATCH);
                httpConfirmUserRequest.addBearerToken();
                httpConfirmUserRequest.executeRequest(new RequestCallback() {
                    @Override
                    public void getResponse(String response) {
                        JSONObject jsonObjectResource = httpConfirmUserRequest.getJSONObjectResource(response);
                        try {
                            String firstname = jsonObjectResource.getString("firstname");
                            String lastname = jsonObjectResource.getString("lastname");
                            Snackbar.make(layout, firstname + " " + lastname + " à bien été accepté", Snackbar.LENGTH_LONG).show();
                            updateData(position);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void getError400(NetworkResponse networkResponse) {
                        displayErrorAppear();
                    }

                    @Override
                    public void getErrorNoInternet() {}
                });
            }
        });

        // Unconfirm the user
        holder.btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final HttpRequest httpUnconfirmUserRequest = new HttpRequest(context, layout, Config.API_BASE_URL + Config.API_USERS_UNCONFIRM(ids.get(position)), Request.Method.PATCH);
                httpUnconfirmUserRequest.addBearerToken();
                httpUnconfirmUserRequest.executeRequest(new RequestCallback() {
                    @Override
                    public void getResponse(String response) {
                        JSONObject jsonObjectResource = httpUnconfirmUserRequest.getJSONObjectResource(response);
                        try {
                            String firstname = jsonObjectResource.getString("firstname");
                            String lastname = jsonObjectResource.getString("lastname");
                            Snackbar.make(layout, firstname + " " + lastname + " à bien été refusé", Snackbar.LENGTH_LONG).show();
                            updateData(position);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void getError400(NetworkResponse networkResponse) {
                        displayErrorAppear();
                    }

                    @Override
                    public void getErrorNoInternet() {}
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return emails.size();
    }

    /**
     * Update all data, remove item at position and notify adapter
     * Also update text views in activity
     * @param position The position for which the item has changed
     */
    private void updateData(int position){
        emails.remove(position);
        firstnames.remove(position);
        lastnames.remove(position);
        ids.remove(position);

        notifyItemRemoved(position);
        notifyItemRangeChanged(position, emails.size());

        String usersPendingTitle = emails.size() > 1
                ? context.getResources().getString(R.string.users_pending_title_plural)
                : context.getResources().getString(R.string.users_pending_title_singular);

        tvTitle.setText(emails.size() + " " + usersPendingTitle);
        if(emails.size() >= 1){
            tvNoData.setVisibility(View.GONE);
        }else{
            tvTitle.setText(context.getResources().getString(R.string.users_pending_main_title));
            tvNoData.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Display error occurred
     * It can be possible if we try to confirm or unconfirm a user that have been confirmed or unconfirmed
     * by another SUPER_ADMIN before us.
     */
    private void displayErrorAppear(){
        Snackbar.make(layout, "Une erreur est survenue, veuillez rafraîchir la page", Snackbar.LENGTH_LONG).show();
    }




    public class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout layout;
        TextView tvEmail;
        TextView tvFirstname;
        TextView tvLastname;
        ImageButton btnAdd;
        ImageButton btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.recyclerUsersPendingLayout);
            tvEmail = itemView.findViewById(R.id.recyclerUsersPendingTvEmail);
            tvFirstname = itemView.findViewById(R.id.recyclerUsersPendingTvFirstname);
            tvLastname = itemView.findViewById(R.id.recyclerUsersPendingTvLastname);
            btnAdd = itemView.findViewById(R.id.recyclerUsersPendingBtnAdd);
            btnRemove = itemView.findViewById(R.id.recyclerUsersPendingBtnRemove);
        }
    }

}
