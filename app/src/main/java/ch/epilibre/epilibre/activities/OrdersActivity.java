package ch.epilibre.epilibre.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ch.epilibre.epilibre.Config;
import ch.epilibre.epilibre.CustomNavigationCallback;
import ch.epilibre.epilibre.Models.Order;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.Utils;
import ch.epilibre.epilibre.http.HttpRequest;
import ch.epilibre.epilibre.http.RequestCallback;
import ch.epilibre.epilibre.recyclers.RecyclerViewAdapterOrders;
import ch.epilibre.epilibre.recyclers.RecyclerViewAdapterUsersPending;
import ch.epilibre.epilibre.Models.Role;
import ch.epilibre.epilibre.Models.User;

public class OrdersActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout mainLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        mainLayout = findViewById(R.id.ordersLayout);
        swipeRefreshLayout = findViewById(R.id.ordersSwipeRefreshLayout);
        // Active the loader
        swipeRefreshLayout.setRefreshing(true);

        // Pull to refresh management -> will init again the recyclerview with new users pending from the API
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(Utils.isConnectedToInternet(OrdersActivity.this)){
                    initRecyclerView();
                }else{
                    swipeRefreshLayout.setRefreshing(false);
                    Utils.NoInternetSnackBar(OrdersActivity.this, mainLayout);
                }

            }
        });

        setupCustomToolbar();
        initRecyclerView();
    }

    /**
     * Hide the current AppTheme ActionBar and set a custom toolbar
     * With this custom toolbar we can have a custom arrow back icon with
     * its own custom onClick listener to comeback to the mainActivity without call OnCreate again
     */
    private void setupCustomToolbar() {
        Toolbar toolbar = findViewById(R.id.ordersToolbar);
        Utils.setUpCustomAppBar(toolbar, getResources().getString(R.string.orders_main_title), new CustomNavigationCallback() {
            @Override
            public void onBackArrowPressed() {
                finish();
            }
        });
    }

    private void initRecyclerView() {
        final ArrayList<Order> orders = new ArrayList<>();

        final HttpRequest httpUsersPendingRequest = new HttpRequest(OrdersActivity.this, mainLayout, Config.API_BASE_URL + Config.API_ORDERS_INDEX, Request.Method.GET);
        httpUsersPendingRequest.addBearerToken();
        httpUsersPendingRequest.executeRequest(new RequestCallback() {
            @Override
            public void getResponse(String response) {
                JSONArray jsonArrayResource = httpUsersPendingRequest.getJSONArrayResource(response);
                // Parse all json users and fill all array lists
                for(int i = 0; i < jsonArrayResource.length(); ++i){
                    try {
                        JSONObject jsonObject = jsonArrayResource.getJSONObject(i);
                        Order order = new Order(
                                jsonObject.getInt("id"),
                                jsonObject.getString("created_at"),
                                jsonObject.getString("seller"),
                                jsonObject.getDouble("totalPrice"),
                                jsonObject.getInt("nbProducts")
                        );
                        orders.add(order);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }


                // Create the recycler view
                RecyclerView recyclerView = findViewById(R.id.ordersRecycler);
                RelativeLayout recyclerItemLayout = findViewById(R.id.recyclerOrdersLayout);
                RecyclerViewAdapterOrders adapter = new RecyclerViewAdapterOrders(OrdersActivity.this, recyclerItemLayout, orders);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(OrdersActivity.this));
                recyclerView.setVisibility(View.VISIBLE);
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void getError400(NetworkResponse networkResponse) {}

            @Override
            public void getErrorNoInternet() {
                swipeRefreshLayout.setRefreshing(false);
                RecyclerView recyclerView = findViewById(R.id.ordersRecycler);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }
}