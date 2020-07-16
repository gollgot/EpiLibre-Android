package ch.epilibre.epilibre.recyclers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ch.epilibre.epilibre.Product;
import ch.epilibre.epilibre.R;

public class RecyclerViewAdapterProducts extends RecyclerView.Adapter<RecyclerViewAdapterProducts.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout layout;
        ImageView image;
        TextView tvName;
        TextView tvCategory;
        TextView tvPrice;
        TextView tvStock;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.recyclerProductsLayout);
            image = itemView.findViewById(R.id.recyclerProductsImage);
            tvName = itemView.findViewById(R.id.recyclerProductsTvName);
            tvCategory = itemView.findViewById(R.id.recyclerProductsTvCategory);
            tvPrice= itemView.findViewById(R.id.recyclerProductsTvPrice);
            tvStock= itemView.findViewById(R.id.recyclerProductsTvStock);
        }
    }


    private Context context;
    private ViewGroup layout;
    private ArrayList<Product> products = new ArrayList<>();

    public RecyclerViewAdapterProducts(Context context, ViewGroup layout, ArrayList<Product> products) {
        this.context = context;
        this.layout = layout;
        this.products = products;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_recycler_products, parent, false);
        RecyclerViewAdapterProducts.ViewHolder holder = new RecyclerViewAdapterProducts.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String unit = products.get(position).getUnit();

        byte[] imageBytes = Base64.decode(products.get(position).getImage(), Base64.DEFAULT);
        Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        holder.image.setImageBitmap(decodedImage);
        holder.tvName.setText(products.get(position).getName());
        holder.tvCategory.setText(products.get(position).getCategory());
        holder.tvPrice.setText(products.get(position).getPrice() + " CHF / " + unit);
        holder.tvStock.setText(products.get(position).getStock() + " " + unit + " en stock");
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

}
