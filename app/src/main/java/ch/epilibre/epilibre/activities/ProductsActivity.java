package ch.epilibre.epilibre.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SearchView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ch.epilibre.epilibre.Config;
import ch.epilibre.epilibre.CustomNavigationCallback;
import ch.epilibre.epilibre.Product;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.Utils;
import ch.epilibre.epilibre.http.HttpRequest;
import ch.epilibre.epilibre.http.RequestCallback;
import ch.epilibre.epilibre.recyclers.RecyclerViewAdapterProducts;

public class ProductsActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerViewProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        recyclerViewProducts = findViewById(R.id.productsRecycler);
        swipeRefreshLayout = findViewById(R.id.productsSwipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initRecyclerView();
                swipeRefreshLayout.setRefreshing(false);
            }
        });


        setupCustomToolbar();
        initRecyclerView();
    }

    /**
     * Hide the current AppTheme ActionBar and set a custom toolbar
     * With this custom toolbar we can have a custom arrow back icon with
     * its own custom onClick listener to comeback to the mainActivity with no result
     */
    private void setupCustomToolbar() {
        Toolbar toolbar = findViewById(R.id.productsToolbar);
        setSupportActionBar(toolbar);
        Utils.setUpCustomAppBar(toolbar, new CustomNavigationCallback() {
            @Override
            public void onBackArrowPressed() {
                // Return to the MainActivity without result
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }
        });
    }


    private void initRecyclerView() {

        final ArrayList<Product> products = new ArrayList<>();

        final RelativeLayout layout = findViewById(R.id.productsLayout);
        final HttpRequest httpUsersProductsRequest = new HttpRequest(ProductsActivity.this, layout, Config.API_BASE_URL + Config.API_PRODUCTS_INDEX, Request.Method.GET);
        httpUsersProductsRequest.addBearerToken();
        httpUsersProductsRequest.executeRequest(new RequestCallback() {
            @Override
            public void getResponse(String response) {
                JSONArray jsonArrayResource = httpUsersProductsRequest.getJSONArrayResource(response);
                // Parse all json users and fill all array lists
                for(int i = 0; i < jsonArrayResource.length(); ++i){
                    try {
                        JSONObject jsonObject = jsonArrayResource.getJSONObject(i);
                        Product product = new Product(
                                jsonObject.getInt("id"),
                                jsonObject.getString("name"),
                                jsonObject.getDouble("price"),
                                jsonObject.getDouble("stock"),
                                jsonObject.getString("image"),
                                jsonObject.getString("category"),
                                jsonObject.getString("unit")
                        );
                        products.add(product);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // Create the recycler view
                RecyclerView recyclerView = findViewById(R.id.productsRecycler);
                RecyclerViewAdapterProducts adapter = new RecyclerViewAdapterProducts(ProductsActivity.this, layout, products);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(ProductsActivity.this));
            }

            @Override
            public void getError400(NetworkResponse networkResponse) {}

            @Override
            public void getErrorNoInternet() { }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        return true;
    }
}