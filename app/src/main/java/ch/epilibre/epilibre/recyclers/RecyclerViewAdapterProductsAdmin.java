package ch.epilibre.epilibre.recyclers;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import ch.epilibre.epilibre.Product;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.Utils;
import ch.epilibre.epilibre.activities.ProductsAdminActivity;

/**
 * Reuse the RecyclerViewAdapterProducts to build our products admin adapter. We only change the
 * btnAdd to be an edit button (behaviour and icon change)
 */
public class RecyclerViewAdapterProductsAdmin extends RecyclerViewAdapterProducts {

    private ProductsAdminActivity productsAdminActivity;

    /**
     * Constructor
     * @param context The context
     * @param layout The main layout (parent)
     * @param products All the products we want to display
     */
    public RecyclerViewAdapterProductsAdmin(Context context, ViewGroup layout, ArrayList<Product> products, ProductsAdminActivity productsAdminActivity) {
        super(context, layout, products);
        this.productsAdminActivity = productsAdminActivity;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        // Call the super method
        super.onBindViewHolder(holder, position);

        final ArrayList<Product> products = super.getProducts();

        // Change the add button to be an edit button
        holder.btnAdd.setImageResource(R.drawable.ic_edit);
        holder.btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Utils.isConnectedToInternet(RecyclerViewAdapterProductsAdmin.this.getContext())){
                    productsAdminActivity.startEditActivity(products.get(position));
                }else{
                    Utils.NoInternetSnackBar(RecyclerViewAdapterProductsAdmin.this.getContext(), RecyclerViewAdapterProductsAdmin.this.getLayout());
                }
            }
        });
    }
}
