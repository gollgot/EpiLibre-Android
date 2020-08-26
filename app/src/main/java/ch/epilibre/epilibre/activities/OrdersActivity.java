package ch.epilibre.epilibre.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.prefs.Preferences;

import ch.epilibre.epilibre.Config;
import ch.epilibre.epilibre.CustomNavigationCallback;
import ch.epilibre.epilibre.Models.BasketLine;
import ch.epilibre.epilibre.Models.Order;
import ch.epilibre.epilibre.Models.Product;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.SessionManager;
import ch.epilibre.epilibre.Utils;
import ch.epilibre.epilibre.http.HttpRequest;
import ch.epilibre.epilibre.http.RequestCallback;
import ch.epilibre.epilibre.recyclers.RecyclerViewAdapterOrders;
import ch.epilibre.epilibre.recyclers.RecyclerViewAdapterUsersPending;
import ch.epilibre.epilibre.Models.Role;
import ch.epilibre.epilibre.Models.User;

public class OrdersActivity extends AppCompatActivity {

    private final static int REQUEST_CODE_EXTERNAL_STORAGE = 1;
    private SessionManager sessionManager;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout mainLayout;
    private ArrayList<Order> orders;
    private boolean downloadOrdersEnable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders);

        sessionManager = new SessionManager(getApplicationContext());

        mainLayout = findViewById(R.id.ordersLayout);
        swipeRefreshLayout = findViewById(R.id.ordersSwipeRefreshLayout);
        // Active the loader
        swipeRefreshLayout.setRefreshing(true);

        // Pull to refresh management -> will init again the recyclerview with new orders from the API
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
        setSupportActionBar(toolbar); // To be able to have a menu like a normal actionBar
        Utils.setUpCustomAppBar(toolbar, getResources().getString(R.string.orders_main_title), new CustomNavigationCallback() {
            @Override
            public void onBackArrowPressed() {
                finish();
            }
        });
        // Specific for supported action bar, to display custom title
        getSupportActionBar().setTitle(getResources().getString(R.string.orders_main_title));
    }

    /**
     * Init the recycler view with new orders from API
     */
    private void initRecyclerView() {
        // Reload menu
        downloadOrdersEnable = false;
        invalidateOptionsMenu();

        orders = new ArrayList<>();

        final HttpRequest httpUsersPendingRequest = new HttpRequest(OrdersActivity.this, mainLayout, Config.API_BASE_URL + Config.API_ORDERS_INDEX, Request.Method.GET);
        httpUsersPendingRequest.addBearerToken();
        httpUsersPendingRequest.executeRequest(new RequestCallback() {
            @Override
            public void getResponse(String response) {
                JSONArray jsonArrayResource = httpUsersPendingRequest.getJSONArrayResource(response);

                // Parse all json data related to Orders
                for(int i = 0; i < jsonArrayResource.length(); ++i){
                    try {
                        // Extract order info
                        JSONObject jsonObjectResource = jsonArrayResource.getJSONObject(i);
                        String date = jsonObjectResource.getString("created_at");
                        String seller = jsonObjectResource.getString("seller");
                        double totalPrice = jsonObjectResource.getDouble("totalPrice");

                        // Extract basketLine / product info
                        ArrayList<BasketLine> basketLines = new ArrayList<>();
                        JSONArray jsonArrayProducts = jsonObjectResource.getJSONArray("products");
                        for(int j = 0; j < jsonArrayProducts.length(); ++j){
                            JSONObject jsonObjectProduct = jsonArrayProducts.getJSONObject(j);
                            Product product = new Product(-1, jsonObjectProduct.getString("name"), -1, -1, null, jsonObjectProduct.getString("category"), jsonObjectProduct.getString("unit"), null, null);
                            basketLines.add(new BasketLine(product, jsonObjectProduct.getDouble("quantity"), jsonObjectProduct.getDouble("price")));
                        }

                        orders.add(new Order(date, seller, totalPrice, basketLines));
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

                // Reload menu
                downloadOrdersEnable = true;
                invalidateOptionsMenu();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Download only available for SUPER_ADMIN
        if(sessionManager.getUser().getRole() == Role.SUPER_ADMIN){
            getMenuInflater().inflate(R.menu.download_menu, menu);

            MenuItem downloadItem = menu.findItem(R.id.menuDownloadDownloadItem);

            // Display download menu item only if we finish to load all orders
            if(downloadOrdersEnable){
                downloadItem.setVisible(true);
            }else{
                downloadItem.setVisible(false);
            }

        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Menu only for SUPER_ADMIN
        if(sessionManager.getUser().getRole() == Role.SUPER_ADMIN){
            switch (item.getItemId()){
                // Download all orders stuff in CSV
                case R.id.menuDownloadDownloadItem:
                    // Permission granted -> download CSV
                    if(checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                        downloadOrdersCSV();
                    }
                    // Permission not granted -> request permission
                    else{
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_EXTERNAL_STORAGE);
                    }
                    break;

                default:
                    return super.onOptionsItemSelected(item);
            }
        }

        return true;
    }

    /**
     * CHeck if permission is granted or not
     * @param permission The permission we want to check
     * @return True if the permission is granted false otherwise
     */
    private boolean checkPermission(String permission) {
        int check = ContextCompat.checkSelfPermission(this, permission);
        return (check == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Download all orders in CSV into the download folder
     */
    private void downloadOrdersCSV() {
        long sec = System.currentTimeMillis() / 1000L;
        final String FILE_NAME = "EpiLibre_Ventes_" + sec + ".csv";
        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(path, FILE_NAME);

        try {
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);

            // CSV generation
            out.write("# vente,Date,Vendeur,Produit,Quantité,Unité,Prix de vente\n");
            int i = 1;
            for(Order order : orders){
                for(BasketLine basketLine : order.getBasketLines()){
                    out.write(i + ","
                            + order.getDate() + ","
                            + order.getSeller() + ","
                            + basketLine.getProduct().getName() + ","
                            + basketLine.getQuantity() + ","
                            + basketLine.getProduct().getUnit() + ","
                            + basketLine.getPrice() + "\n");
                }
                ++i;
            }

            out.close();

            Snackbar.make(mainLayout, R.string.orders_file_saved_download, Snackbar.LENGTH_LONG)
                .setAction(R.string.orders_open, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
                    }
                })
                .show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case REQUEST_CODE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    downloadOrdersCSV();
                } else {
                    Snackbar.make(mainLayout, R.string.orders_permission_refused_msg, Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }
}