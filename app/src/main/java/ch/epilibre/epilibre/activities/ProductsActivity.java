package ch.epilibre.epilibre.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.google.android.material.snackbar.Snackbar;

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

    private RelativeLayout layout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerViewProducts;
    private RecyclerViewAdapterProducts adapter;
    private TextView tvSearchNoResults;
    private ArrayList<Product> productsAll;
    private Spinner spinnerCategories;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        spinnerCategories = findViewById(R.id.productsSpinnerCategories);
        layout = findViewById(R.id.productsLayout);
        recyclerViewProducts = findViewById(R.id.productsRecycler);
        swipeRefreshLayout = findViewById(R.id.productsSwipeRefreshLayout);
        tvSearchNoResults = findViewById(R.id.productsTvNoResults);

        // Active the loader
        swipeRefreshLayout.setRefreshing(true);

        // Pull to refresh management -> will init again the recyclerview with new products from the API
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(Utils.isConnectedToInternet(ProductsActivity.this)){
                    initRecyclerView();
                }else{
                    swipeRefreshLayout.setRefreshing(false);
                    Snackbar.make(layout, getResources().getString(R.string.no_internet_connection), Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        setupCustomToolbar();
        initSpinner(spinnerCategories);
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

    /**
     * Categories spinner initialisation
     * @param spinner The spinner
     */
    private void initSpinner(Spinner spinner){
        final ArrayList<String> categories = new ArrayList<>();
        categories.add("Toutes");
        categories.add("Graines");
        categories.add("Produits ménagers");
        categories.add("Légumineuse");
        categories.add("Pâtes");
        categories.add("Autres");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
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
    }

    /**
     * Products recycler view initialisation.
     * It will fetch on the API all the products and update the recycler view with those products
     */
    private void initRecyclerView() {
        productsAll = new ArrayList<>();

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
                        productsAll.add(product);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                spinnerCategories.setSelection(0); // Each initialisation, come back to the initial state of the spinner
                updateRecyclerView(productsAll);
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
        adapter = new RecyclerViewAdapterProducts(ProductsActivity.this, layout, products);
        recyclerViewProducts.setAdapter(adapter);
        recyclerViewProducts.setLayoutManager(new LinearLayoutManager(ProductsActivity.this));
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.menuSearchSearchItem);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getResources().getString(R.string.menu_search_search_item));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(final String newText) {
                Filter.FilterListener listener = new Filter.FilterListener() {
                    @Override
                    public void onFilterComplete(int i) {
                        if(adapter.getItemCount() > 0){
                            tvSearchNoResults.setVisibility(View.GONE);
                            swipeRefreshLayout.setVisibility(View.VISIBLE);
                        }else{
                            swipeRefreshLayout.setVisibility(View.GONE);
                            tvSearchNoResults.setText(getResources().getString(R.string.products_search_no_results) + " \"" + newText + "\"");
                            tvSearchNoResults.setVisibility(View.VISIBLE);
                        }
                    }
                };
                adapter.getFilter().filter(newText, listener);
                return false;
            }
        });
        return true;
    }
}