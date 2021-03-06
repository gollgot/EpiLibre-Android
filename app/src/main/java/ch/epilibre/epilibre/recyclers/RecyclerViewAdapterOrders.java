package ch.epilibre.epilibre.recyclers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ch.epilibre.epilibre.Models.Order;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.Utils;
import ch.epilibre.epilibre.activities.OrderDetails;

public class RecyclerViewAdapterOrders extends RecyclerView.Adapter<RecyclerViewAdapterOrders.ViewHolder> {


    public class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout layout;
        TextView tvDate;
        TextView tvSeller;
        TextView tvTotalPrice;
        TextView tvNbProducts;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.recyclerOrdersLayout);
            tvDate = itemView.findViewById(R.id.recyclerOrdersTvDate);
            tvSeller = itemView.findViewById(R.id.recyclerOrdersTvSeller);
            tvTotalPrice = itemView.findViewById(R.id.recyclerOrdersTvTotalPrice);
            tvNbProducts = itemView.findViewById(R.id.recyclerOrdersTvNbProducts);
        }
    }


    private Context context;
    private ViewGroup layout;
    private ArrayList<Order> orders;

    public RecyclerViewAdapterOrders(Context context, ViewGroup layout, ArrayList<Order> orders) {
        this.context = context;
        this.layout = layout;
        this.orders = orders;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_recycler_orders, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        int nbProducts = orders.get(position).getBasketLines().size();

        if(orders.get(position).hasDiscount()){
            --nbProducts;
        }

        String itemsOrtho = nbProducts > 1 ? context.getString(R.string.main_items_plurial) : context.getString(R.string.main_items_singular);

        holder.tvDate.setText(orders.get(position).getDate());
        holder.tvSeller.setText(orders.get(position).getSeller());
        holder.tvTotalPrice.setText(Utils.decimalFormat.format(orders.get(position).getTotalPrice()) + " CHF");
        holder.tvNbProducts.setText(nbProducts + " " + itemsOrtho);
        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent orderDetailIntent = new Intent(context, OrderDetails.class);
                orderDetailIntent.putExtra("order", orders.get(position));
                context.startActivity(orderDetailIntent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

}
