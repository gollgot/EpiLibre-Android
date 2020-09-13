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
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ch.epilibre.epilibre.Models.BasketLine;
import ch.epilibre.epilibre.Models.DiscountLine;
import ch.epilibre.epilibre.Models.Product;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.Utils;

public class RecyclerViewAdapterBasketLine extends RecyclerView.Adapter<RecyclerViewAdapterBasketLine.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout layout;
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
    private Button btnDiscount;

    public RecyclerViewAdapterBasketLine(RecyclerView recyclerView, Context context, ViewGroup layout, ArrayList<BasketLine> basketLines, TextView tvTotalPrice, Button btnCheckout, Button btnDiscount) {
       this.recyclerView = recyclerView;
        this.context = context;
        this.layout = layout;
        this.basketLines = basketLines;
        this.tvTotalPrice = tvTotalPrice;
        this.btnCheckout = btnCheckout;
        this.btnDiscount = btnDiscount;
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

                // Removed a discount line -> Discount button can comeback again
                if(basketLines.get(position) instanceof DiscountLine){
                    btnDiscount.setVisibility(View.VISIBLE);
                }

                // Update the list
                basketLines.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, basketLines.size());


                // CAREFUL -> There is a discount ! Need to update it !
                if(basketLines.size() > 1){
                    int discountPosition = basketLines.size() - 1;
                    if(basketLines.get(discountPosition) instanceof DiscountLine){
                        ArrayList<BasketLine> basketLinesWithoutDiscountLine = new ArrayList<>(basketLines);
                        basketLinesWithoutDiscountLine.remove(discountPosition);
                        // Calculate the new discount (after the basketline remove and without the discount line that will falsify the total price)
                        final double totalPrice = Utils.getTotalPrice(basketLinesWithoutDiscountLine);
                        double discountPrice = Utils.calculateDiscount(totalPrice);
                        DiscountLine newDiscountLine = new DiscountLine(null, 0, Utils.DISCOUNT_PERCENT, discountPrice);

                        // Remove the old discount and add the new one
                        basketLines.remove(discountPosition);
                        basketLines.add(newDiscountLine);

                        // Fetch the RecyclerView's viewHolder that correspond to the line we want to update (the discount one)
                        // And setup its info
                        RecyclerViewAdapterBasketLine.ViewHolder viewHolder = (RecyclerViewAdapterBasketLine.ViewHolder) recyclerView.findViewHolderForAdapterPosition(discountPosition);
                        viewHolder.tvProduct.setText(basketLines.get(discountPosition).getMainInfo());
                        viewHolder.tvQuantity.setText(basketLines.get(discountPosition).getDetails());
                        viewHolder.tvPrice.setText(Utils.decimalFormat.format(basketLines.get(discountPosition).getPrice()) + " CHF");
                    }
                }

                // Hide the checkout button and discount if the basket is empty
                if(basketLines.size() == 0){
                    btnCheckout.setVisibility(View.GONE);
                    btnDiscount.setVisibility(View.GONE);
                }
                // There is only one discount line -> remove it
                else if(basketLines.size() == 1 && basketLines.get(0) instanceof DiscountLine){
                    basketLines.remove(0);
                    notifyItemRangeChanged(position, basketLines.size());
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
