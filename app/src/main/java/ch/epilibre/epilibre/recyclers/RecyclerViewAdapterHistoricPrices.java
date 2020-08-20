package ch.epilibre.epilibre.recyclers;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ch.epilibre.epilibre.Models.HistoricPrice;
import ch.epilibre.epilibre.Models.Order;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.Utils;
import ch.epilibre.epilibre.activities.OrderDetails;

public class RecyclerViewAdapterHistoricPrices extends RecyclerView.Adapter<RecyclerViewAdapterHistoricPrices.ViewHolder> {


    public class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout layout;
        TextView tvDate;
        TextView tvProductName;
        TextView tvCreatedBy;
        TextView tvOldPrice;
        TextView tvNewPrice;
        ImageView imgNew;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.recyclerHistoricPricesLayout);
            tvDate = itemView.findViewById(R.id.recyclerHistoricPricesTvDate);
            tvProductName = itemView.findViewById(R.id.recyclerHistoricPricesTvProductName);
            tvCreatedBy = itemView.findViewById(R.id.recyclerHistoricPricesTvCreatedBy);
            tvOldPrice = itemView.findViewById(R.id.recyclerHistoricPricesTvOldPrice);
            tvNewPrice = itemView.findViewById(R.id.recyclerHistoricPricesTvNewPrice);
            imgNew = itemView.findViewById(R.id.recyclerHistoricPricesImgNew);
        }
    }


    private Context context;
    private ViewGroup layout;
    private ArrayList<HistoricPrice> historicPrices;

    public RecyclerViewAdapterHistoricPrices(Context context, ViewGroup layout, ArrayList<HistoricPrice> historicPrices) {
        this.context = context;
        this.layout = layout;
        this.historicPrices = historicPrices;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_recycler_historic_prices, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        HistoricPrice historicPrice = historicPrices.get(position);
        holder.tvDate.setText(historicPrice.getCreatedAt());
        holder.tvProductName.setText(historicPrice.getProductName());
        holder.tvCreatedBy.setText(historicPrice.getCreatedBy());
        holder.tvOldPrice.setText(historicPrice.getOldPrice() + " CHF");
        holder.tvNewPrice.setText(historicPrice.getNewPrice() + " CHF");

        if(historicPrice.isSeen()){
            holder.imgNew.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return historicPrices.size();
    }

}
