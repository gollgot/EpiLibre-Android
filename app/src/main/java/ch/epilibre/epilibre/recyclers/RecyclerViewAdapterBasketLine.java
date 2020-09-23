package ch.epilibre.epilibre.recyclers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ch.epilibre.epilibre.Models.BasketLine;
import ch.epilibre.epilibre.Models.Discount;
import ch.epilibre.epilibre.Models.DiscountLine;
import ch.epilibre.epilibre.Models.Product;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.Utils;

public class RecyclerViewAdapterBasketLine extends RecyclerView.Adapter<RecyclerViewAdapterBasketLine.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout layout;
        TextView tvProduct;
        TextView tvQuantity;
        TextView tvPrice;
        ImageButton btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.recyclerBasketLinesLayout);
            tvProduct = itemView.findViewById(R.id.recyclerBasketLinesProduct);
            tvQuantity = itemView.findViewById(R.id.recyclerBasketLinesQuantity);
            tvPrice = itemView.findViewById(R.id.recyclerBasketLinesPrice);
            btnRemove = itemView.findViewById(R.id.recyclerBasketLinesBtnRemove);
        }
    }


    private Context context;
    private ViewGroup layout;
    private RecyclerView recyclerView;
    private ArrayList<BasketLine> basketLines;
    private TextView tvTotalPrice;
    private Button btnCheckout;

    public RecyclerViewAdapterBasketLine(RecyclerView recyclerView, Context context, ViewGroup layout, ArrayList<BasketLine> basketLines, TextView tvTotalPrice, Button btnCheckout) {
       this.recyclerView = recyclerView;
        this.context = context;
        this.layout = layout;
        this.basketLines = basketLines;
        this.tvTotalPrice = tvTotalPrice;
        this.btnCheckout = btnCheckout;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_recycler_basket_lines, parent, false);
        RecyclerViewAdapterBasketLine.ViewHolder holder = new RecyclerViewAdapterBasketLine.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        // Use getMainInfo() and getDetails() as polymorphic liaison to display the right info (from BasketLine or DiscountLine)
        holder.tvProduct.setText(basketLines.get(position).getMainInfo());
        holder.tvQuantity.setText(basketLines.get(position).getDetails());
        holder.tvPrice.setText(Utils.decimalFormat.format(basketLines.get(position).getPrice()) + " CHF");

        holder.btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Update the list
                basketLines.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, basketLines.size());

                // Hide the checkout button if the basket is empty
                if(basketLines.size() == 0){
                    btnCheckout.setVisibility(View.GONE);
                }

                // Update total price
                Utils.updateTotalPrice(basketLines, tvTotalPrice);
            }
        });
    }

    @Override
    public int getItemCount() {
        return basketLines.size();
    }


}
