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

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private Context context;
    private ArrayList<String> emails = new ArrayList<>();
    private ArrayList<String> firstnames = new ArrayList<>();
    private ArrayList<String> lastnames = new ArrayList<>();

    private TextView tvTitle;
    private TextView tvNoData;

    public RecyclerViewAdapter(Context context, ArrayList<String> emails, ArrayList<String> firstnames, ArrayList<String> lastnames, TextView tvTitle, TextView tvNoData) {
        this.context = context;
        this.emails = emails;
        this.firstnames = firstnames;
        this.lastnames = lastnames;
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

        holder.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateData(position);
            }
        });

        holder.btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateData(position);
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
