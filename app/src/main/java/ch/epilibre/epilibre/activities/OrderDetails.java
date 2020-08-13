package ch.epilibre.epilibre.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import ch.epilibre.epilibre.CustomNavigationCallback;
import ch.epilibre.epilibre.Models.Order;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.Utils;
import ch.epilibre.epilibre.recyclers.RecyclerViewAdapterOrderDetails;
import ch.epilibre.epilibre.recyclers.RecyclerViewAdapterOrders;

public class OrderDetails extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_details);

        setupCustomToolbar();

        Order order = (Order) getIntent().getSerializableExtra("order");
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
        Utils.setUpCustomAppBar(toolbar, getResources().getString(R.string.order_details_main_title), new CustomNavigationCallback() {
            @Override
            public void onBackArrowPressed() {
                finish();
            }
        });
    }
}