package ch.epilibre.epilibre.recyclers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;

import ch.epilibre.epilibre.Models.Product;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.Utils;
import ch.epilibre.epilibre.dialogs.AddToBasketDialog;

public class RecyclerViewAdapterProductsStock extends RecyclerView.Adapter<RecyclerViewAdapterProductsStock.ViewHolder> implements Filterable {

    public class ViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout layout;
        TextView tvProductName;
        TextView tvCategory;
        CheckBox checkbox;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            layout = itemView.findViewById(R.id.recyclerProductsStockLayout);
            tvProductName = itemView.findViewById(R.id.recyclerProductsStockTvProductName);
            tvCategory = itemView.findViewById(R.id.recyclerProductsStockTvCategory);
            checkbox = itemView.findViewById(R.id.recyclerProductsStockCheckbox);
        }
    }


    private Context context;
    private ViewGroup layout;
    private ArrayList<Product> products; // Products that will be displayed
    private ArrayList<Product> productsAll; // All products (used for filtering)
    private ArrayList<Product> productsChecked; // Products checked (checkbox)

    public RecyclerViewAdapterProductsStock(Context context, ViewGroup layout, ArrayList<Product> products, ArrayList<Product> productsChecked) {
        this.context = context;
        this.layout = layout;
        this.products = products;
        this.productsChecked = productsChecked;
        this.productsAll = new ArrayList<>(this.products);
    }

    public ArrayList<Product> getProductsChecked() {
        return productsChecked;
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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_recycler_products_stock, parent, false);
        RecyclerViewAdapterProductsStock.ViewHolder holder = new RecyclerViewAdapterProductsStock.ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final Product product = products.get(position);

        for(Product productChecked : productsChecked){
            if(productChecked.getName().equals(product.getName())){
                holder.checkbox.setChecked(true);
                break;
            }
        }

        holder.tvProductName.setText(product.getName());
        holder.tvCategory.setText(product.getCategory());
        holder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                toggleCheckBoxManagement(holder.checkbox, product);
            }
        });

        holder.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(holder.checkbox.isChecked()){
                    holder.checkbox.setChecked(false);
                }else{
                    holder.checkbox.setChecked(true);
                }
            }
        });


    }

    private void toggleCheckBoxManagement(CheckBox checkbox, Product product) {
        if(checkbox.isChecked()){
            productsChecked.add(product);
        }else{
            for(int i = 0; i < productsChecked.size(); ++i){
                if(productsChecked.get(i).getName().equals(product.getName())){
                    productsChecked.remove(i);
                    break;
                }
            }
        }
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
