package ch.epilibre.epilibre.recyclers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;

import ch.epilibre.epilibre.Product;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.Utils;
import ch.epilibre.epilibre.dialogs.AddToBasketDialog;

public class RecyclerViewAdapterProducts extends RecyclerView.Adapter<RecyclerViewAdapterProducts.ViewHolder> implements Filterable {

    public class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout layout;
        ImageView image;
        TextView tvName;
        TextView tvCategory;
        TextView tvPrice;
        TextView tvStock;
        ImageButton btnAdd;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.recyclerProductsLayout);
            image = itemView.findViewById(R.id.recyclerProductsImage);
            tvName = itemView.findViewById(R.id.recyclerProductsTvName);
            tvCategory = itemView.findViewById(R.id.recyclerProductsTvCategory);
            tvPrice= itemView.findViewById(R.id.recyclerProductsTvPrice);
            tvStock= itemView.findViewById(R.id.recyclerProductsTvStock);
            btnAdd = itemView.findViewById(R.id.recyclerProductsBtnAdd);
        }
    }


    private Context context;
    private ViewGroup layout;
    private ArrayList<Product> products; // Products that will be displayed
    private ArrayList<Product> productsAll; // All products (used for filtering)

    public RecyclerViewAdapterProducts(Context context, ViewGroup layout, ArrayList<Product> products) {
        this.context = context;
        this.layout = layout;
        this.products = products;
        this.productsAll = new ArrayList<>(this.products);
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public Context getContext() {
        return context;
    }

    public ViewGroup getLayout() {
        return layout;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_recycler_products, parent, false);
        RecyclerViewAdapterProducts.ViewHolder holder = new RecyclerViewAdapterProducts.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        String unit = products.get(position).getUnit();

        byte[] imageBytes = Base64.decode(products.get(position).getImage(), Base64.DEFAULT);
        Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        holder.image.setImageBitmap(decodedImage);
        holder.tvName.setText(products.get(position).getName());
        holder.tvCategory.setText(products.get(position).getCategory());
        holder.tvPrice.setText(Utils.decimalFormat.format(products.get(position).getPrice()) + " CHF / " + unit);
        holder.tvStock.setText(products.get(position).getStock() + " " + unit + " en stock");
        holder.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddToBasketDialog addToBasketDialog = new AddToBasketDialog(products.get(position));
                addToBasketDialog.show(((FragmentActivity)context).getSupportFragmentManager(), "product_add_dialog");
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            // Run on background thread -> Filter logic
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {

                ArrayList<Product> filteredProducts = new ArrayList<>();
                // Empty search text -> All products
                if(charSequence.toString().isEmpty()){
                    filteredProducts.addAll(productsAll);
                }
                // Search by product name
                else{
                    for(Product product : productsAll){
                        if(product.getName().toLowerCase().contains(charSequence.toString().toLowerCase())){
                            filteredProducts.add(product);
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredProducts;

                return filterResults;
            }

            // Run on UI thread
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                products.clear();
                products.addAll((Collection<? extends Product>) filterResults.values);
                notifyDataSetChanged();
            }
        };
    }

}
