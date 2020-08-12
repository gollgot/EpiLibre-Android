package ch.epilibre.epilibre.recyclers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;

import ch.epilibre.epilibre.BasketLine;
import ch.epilibre.epilibre.Product;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.Utils;
import ch.epilibre.epilibre.dialogs.AddToBasketDialog;

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
    private ArrayList<BasketLine> basketLines;
    private TextView tvTotalPrice;
    private Button btnCheckout;

    public RecyclerViewAdapterBasketLine(Context context, ViewGroup layout, ArrayList<BasketLine> basketLines, TextView tvTotalPrice, Button btnCheckout) {
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
        Product product = basketLines.get(position).getProduct();
        String unit = product.getUnit();

        holder.tvProduct.setText(product.getName() + " (" + product.getPrice() + " CHF / " + unit + ")");
        holder.tvQuantity.setText("x " + basketLines.get(position).getQuantity() + " " + unit);
        holder.tvPrice.setText(Utils.decimalFormat.format(basketLines.get(position).getPrice()) + " CHF");

        holder.btnRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Update the list
                basketLines.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, basketLines.size());

                // Update total price
                Utils.updateTotalPrice(basketLines, tvTotalPrice);

                // Hide the checkout button if the basket is empty
                if(basketLines.size() == 0){
                    btnCheckout.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return basketLines.size();
    }


}
