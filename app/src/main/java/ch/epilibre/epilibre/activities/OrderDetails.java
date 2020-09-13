package ch.epilibre.epilibre.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Filter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.text.Normalizer;

import ch.epilibre.epilibre.CustomNavigationCallback;
import ch.epilibre.epilibre.Models.BasketLine;
import ch.epilibre.epilibre.Models.Order;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.Utils;
import ch.epilibre.epilibre.recyclers.RecyclerViewAdapterOrderDetails;
import ch.epilibre.epilibre.recyclers.RecyclerViewAdapterOrders;

public class OrderDetails extends AppCompatActivity {

    private Order order;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        setupCustomToolbar();

        order = (Order) getIntent().getSerializableExtra("order");
        boolean fromCheckout = getIntent().getBooleanExtra("fromCheckout", false);
        RelativeLayout mainLayout = findViewById(R.id.orderDetailsLayout);

        // Display a snackbar if we come from checkout (MainActivity)
        if(fromCheckout){
            Snackbar.make(mainLayout, getString(R.string.main_buy_successful), Snackbar.LENGTH_LONG).show();
        }

        TextView tvDate = findViewById(R.id.ordersDetailsTvDate);
        TextView tvSeller = findViewById(R.id.ordersDetailsTvSeller);
        TextView tvTotalPrice = findViewById(R.id.ordersDetailsTvTotalPrice);
        RecyclerView recyclerView = findViewById(R.id.ordersDetailsRecycler);

        // Fill texts
        tvDate.setText(order.getDate());
        tvSeller.setText(getString(R.string.order_details_seller) + " " + order.getSeller());
        tvTotalPrice.setText(getString(R.string.order_details_total_price) + " " + Utils.decimalFormat.format(order.getTotalPrice()) + " CHF");

        // Recyclerview setup
        RelativeLayout recyclerItemLayout = findViewById(R.id.recyclerOrderDetailsLayout);
        RecyclerViewAdapterOrderDetails adapter = new RecyclerViewAdapterOrderDetails(OrderDetails.this, recyclerItemLayout, order.getBasketLines());
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(OrderDetails.this));
    }

    /**
     * Hide the current AppTheme ActionBar and set a custom toolbar
     * With this custom toolbar we can have a custom arrow back icon with
     * its own custom onClick listener to comeback to the previous activity
     */
    private void setupCustomToolbar() {
        Toolbar toolbar = findViewById(R.id.ordersDetailsToolbar);
        setSupportActionBar(toolbar); // To be able to have a menu like a normal actionBar
        Utils.setUpCustomAppBar(toolbar, getResources().getString(R.string.order_details_main_title), new CustomNavigationCallback() {
            @Override
            public void onBackArrowPressed() {
                finish();
            }
        });
        // Specific for supported action bar, to display custom title
        getSupportActionBar().setTitle(getResources().getString(R.string.order_details_main_title));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.send_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            // Send order detail
            case R.id.menuSendSendItem:
                // Create the products string
                StringBuilder sbProducts = new StringBuilder();

                int productNb = order.hasDiscount() ? order.getBasketLines().size() - 1 : order.getBasketLines().size();

                // Add all product
                for(int i = 0; i < productNb; ++i){
                    BasketLine basketLine = order.getBasketLines().get(i);

                    sbProducts.append(basketLine.getProduct().getName()).append("\n")
                            .append("x ").append(basketLine.getQuantity()).append(" ").append(basketLine.getProduct().getUnit())
                            .append("\n")
                            .append(Utils.decimalFormat.format(basketLine.getPrice())).append(" CHF")
                            .append("\n\n");
                }

                // Add the discount
                if(order.hasDiscount()){
                    sbProducts
                            .append(order.getBasketLines().get(productNb).getMainInfo())
                            .append("\n")
                            .append(Utils.decimalFormat.format(order.getBasketLines().get(productNb).getPrice())).append(" CHF")
                            .append("\n\n");
                }

                // Create Intent for send text/plain (like whatsap, messenger, message, email, ...)
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_SUBJECT, "EpiLibre - Détail de vente"); // Case of email
                i.putExtra(Intent.EXTRA_TEXT   ,
                              "Epilibre \n" +
                                    "Date de vente: " + order.getDate() + "\n" +
                                    "Vendeur: " + order.getSeller() + "\n" +
                                    "Total: " + Utils.decimalFormat.format(order.getTotalPrice()) + " CHF \n\n" +
                                    sbProducts.toString()
                                );
                try {
                    startActivity(Intent.createChooser(i, "Epilibre - Envoi détail de vente"));
                } catch (android.content.ActivityNotFoundException ex) {
                    RelativeLayout mainLayout = findViewById(R.id.orderDetailsLayout);
                    Snackbar.make(mainLayout, R.string.order_details_no_message_client_found, Snackbar.LENGTH_SHORT).show();
                }
                break;
                
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }
}