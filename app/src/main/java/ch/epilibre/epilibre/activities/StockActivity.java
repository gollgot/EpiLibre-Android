package ch.epilibre.epilibre.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ch.epilibre.epilibre.Config;
import ch.epilibre.epilibre.CustomNavigationCallback;
import ch.epilibre.epilibre.Models.Product;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.Utils;
import ch.epilibre.epilibre.http.HttpRequest;
import ch.epilibre.epilibre.http.RequestCallback;
import ch.epilibre.epilibre.recyclers.RecyclerViewAdapterProductsStock;

public class StockActivity extends AppCompatActivity {

    private RelativeLayout mainLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerViewProducts;
    private RecyclerViewAdapterProductsStock adapter;
    private TextView tvSearchNoResults;
    private ArrayList<Product> productsAll;
    private ArrayList<Product> productsChecked;
    private Spinner spinnerCategories;
    private RelativeLayout spinnerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock);

        setupCustomToolbar();

        spinnerLayout = findViewById(R.id.stockSpinnerLayout);
        spinnerCategories = findViewById(R.id.stockSpinnerCategories);
        mainLayout = findViewById(R.id.stockLayout);
        recyclerViewProducts = findViewById(R.id.stockRecycler);
        swipeRefreshLayout = findViewById(R.id.stockSwipeRefreshLayout);
        tvSearchNoResults = findViewById(R.id.stockTvNoResults);
        productsChecked = new ArrayList<>();
        Button btnSend = findViewById(R.id.stockBtnSend);

        // Active the loader
        swipeRefreshLayout.setRefreshing(true);
        // Pull to refresh management -> will init again the recyclerview with new products from the API
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(Utils.isConnectedToInternet(StockActivity.this)){
                    initRecyclerView();
                }else{
                    swipeRefreshLayout.setRefreshing(false);
                    Utils.NoInternetSnackBar(StockActivity.this, mainLayout);
                }
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StringBuilder sb = new StringBuilder();
                sb.append("Salut, nous sommes à court de :\n");
                for(Product product : productsChecked){
                    sb.append("- ").append(product.getName()).append("\n");
                }
                sb.append("\nBonne journée :)\n");


                // Create Intent for send text/plain (like whatsap, messenger, message, email, ...)
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");

                i.putExtra(Intent.EXTRA_TEXT, sb.toString());
                try {
                    startActivity(Intent.createChooser(i, "Epilibre - Commande produits"));
                } catch (android.content.ActivityNotFoundException ex) {
                    Snackbar.make(mainLayout, R.string.order_details_no_message_client_found, Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        initSpinner(spinnerCategories);
    }

    /**
     * Hide the current AppTheme ActionBar and set a custom toolbar
     * With this custom toolbar we can have a custom arrow back icon with
     * its own custom onClick listener to comeback to the previous activity
     */
    private void setupCustomToolbar() {
        Toolbar toolbar = findViewById(R.id.stockToolbar);
        setSupportActionBar(toolbar); // To be able to have a menu like a normal actionBar
        Utils.setUpCustomAppBar(toolbar, getResources().getString(R.string.stock_main_title), new CustomNavigationCallback() {
            @Override
            public void onBackArrowPressed() {
                finish();
            }
        });
        // Specific for supported action bar, to display custom title
        getSupportActionBar().setTitle(getResources().getString(R.string.stock_main_title));
    }

    /**
     * Categories spinner initialisation
     * @param spinner The spinner
     */
    private void initSpinner(final Spinner spinner){
        spinnerLayout.setVisibility(View.GONE);

        final HttpRequest httpCategoriesRequest = new HttpRequest(StockActivity.this, mainLayout, Config.API_BASE_URL + Config.API_CATEGORIES_INDEX, Request.Method.GET);
        httpCategoriesRequest.addBearerToken();
        httpCategoriesRequest.executeRequest(new RequestCallback() {
            @Override
            public void getResponse(String response) {
                // Categories array -> first add the "All" category
                final ArrayList<String> categories = new ArrayList<>();
                categories.add(getString(R.string.all_categories));

                // Fetch all categories names from the json response
                JSONArray jsonArrayResource = httpCategoriesRequest.getJSONArrayResource(response);
                // Parse all json users and fill all array lists
                for(int i = 0; i < jsonArrayResource.length(); ++i){
                    try {
                        JSONObject jsonObject = jsonArrayResource.getJSONObject(i);
                        categories.add(jsonObject.getString("name"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(StockActivity.this, android.R.layout.simple_spinner_item, categories);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(dataAdapter);
                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        // If all products selected -> just update the recycler with all products
                        if(i == 0){
                            updateRecyclerView(productsAll);
                        }
                        // Else, category chose -> get from all products only those correspond to the selected category
                        else{
                            ArrayList<Product> productsFilteredByCategory = new ArrayList<>();
                            for(Product product : productsAll){
                                if(product.getCategory().equals(categories.get(i))){
                                    productsFilteredByCategory.add(product);
                                }
                            }
                            updateRecyclerView(productsFilteredByCategory);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                initRecyclerView();
            }

            @Override
            public void getError400(NetworkResponse networkResponse) {

            }

            @Override
            public void getErrorNoInternet() {

            }
        });
    }

    /**
     * Products recycler view initialisation.
     * It will fetch on the API all the products and update the recycler view with those products
     */
    private void initRecyclerView() {
        productsAll = new ArrayList<>();

        final HttpRequest httpUsersProductsRequest = new HttpRequest(StockActivity.this, mainLayout, Config.API_BASE_URL + Config.API_PRODUCTS_INDEX, Request.Method.GET);
        httpUsersProductsRequest.addBearerToken();
        httpUsersProductsRequest.executeRequest(new RequestCallback() {
            @Override
            public void getResponse(String response) {
                JSONArray jsonArrayResource = httpUsersProductsRequest.getJSONArrayResource(response);
                // Parse all json users and fill all array lists
                for(int i = 0; i < jsonArrayResource.length(); ++i){
                    try {
                        JSONObject jsonObject = jsonArrayResource.getJSONObject(i);
                        String productImage = jsonObject.isNull("image") ? null : jsonObject.getString("image");
                        Product product = new Product(
                                jsonObject.getInt("id"),
                                jsonObject.getString("name"),
                                jsonObject.getDouble("price"),
                                jsonObject.getDouble("stock"),
                                productImage,
                                jsonObject.getString("category"),
                                jsonObject.getString("unit"),
                                jsonObject.getString("updatedAt"),
                                jsonObject.getString("updatedBy")
                        );
                        productsAll.add(product);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                spinnerCategories.setSelection(0); // Each initialisation, come back to the initial state of the spinner
                updateRecyclerView(productsAll);

                // When all request finished, also display the spinner categories
                spinnerLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void getError400(NetworkResponse networkResponse) {}

            @Override
            public void getErrorNoInternet() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    /**
     * Update the recyclerview with products
     * @param products Products that will be shown by the recyclerview
     */
    private void updateRecyclerView(ArrayList<Product> products){
        if(adapter != null){
            productsChecked = new ArrayList<>(adapter.getProductsChecked());
        }
        adapter = new RecyclerViewAdapterProductsStock(StockActivity.this, mainLayout, products, productsChecked);
        recyclerViewProducts.setAdapter(adapter);
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(StockActivity.this));
        swipeRefreshLayout.setRefreshing(false);
    }


}