package ch.epilibre.epilibre.recyclers;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ch.epilibre.epilibre.Models.BasketLine;
import ch.epilibre.epilibre.Models.Order;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.Utils;
import ch.epilibre.epilibre.activities.OrderDetails;

public class RecyclerViewAdapterOrderDetails extends RecyclerView.Adapter<RecyclerViewAdapterOrderDetails.ViewHolder> {


    public class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout layout;
        TextView tvProduct;
        TextView tvQuantity;
        TextView tvPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.recyclerOrderDetailsLayout);
            tvProduct = itemView.findViewById(R.id.recyclerOrderDetailsTvProduct);
            tvQuantity = itemView.findViewById(R.id.recyclerOrderDetailsTvQuantity);
            tvPrice = itemView.findViewById(R.id.recyclerOrderDetailsTvPrice);
        }
    }


    private Context context;
    private ViewGroup layout;
    private ArrayList<BasketLine> basketLines;

    public RecyclerViewAdapterOrderDetails(Context context, ViewGroup layout, ArrayList<BasketLine> basketLines) {
        this.context = context;
        this.layout = layout;
        this.basketLines = basketLines;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_recycler_order_details, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        BasketLine basketLine = basketLines.get(position);

        holder.tvProduct.setText(basketLine.getProduct().getName());
        holder.tvQuantity.setText("x " + basketLine.getQuantity() + " " + basketLine.getProduct().getUnit());
        holder.tvPrice.setText(Utils.decimalFormat.format(basketLine.getPrice()) + " CHF");
    }

    @Override
    public int getItemCount() {
        return basketLines.size();
    }

}
