package ch.epilibre.epilibre.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ch.epilibre.epilibre.Config;
import ch.epilibre.epilibre.CustomNavigationCallback;
import ch.epilibre.epilibre.Models.HistoricPrice;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.Utils;
import ch.epilibre.epilibre.http.HttpRequest;
import ch.epilibre.epilibre.http.RequestCallback;
import ch.epilibre.epilibre.recyclers.RecyclerViewAdapterHistoricPrices;

public class HistoricPricesActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout mainLayout;
    private TextView tvNoResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historic_prices);

        setupCustomToolbar();

        mainLayout = findViewById(R.id.historicPricesLayout);
        swipeRefreshLayout = findViewById(R.id.historicPricesSwipeRefreshLayout);
        tvNoResult = findViewById(R.id.historicPricesTvNoResult);

        // Active the loader
        swipeRefreshLayout.setRefreshing(true);
        // Pull to refresh management -> will init again the recyclerview with new historic prices from the API
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(Utils.isConnectedToInternet(HistoricPricesActivity.this)){
                    initRecyclerView();
                }else{
                    swipeRefreshLayout.setRefreshing(false);
                    Utils.NoInternetSnackBar(HistoricPricesActivity.this, mainLayout);
                }
            }
        });

        initRecyclerView();
    }

    /**
     * Hide the current AppTheme ActionBar and set a custom toolbar
     * With this custom toolbar we can have a custom arrow back icon with
     * its own custom onClick listener to comeback to previous activity
     */
    private void setupCustomToolbar() {
        Toolbar toolbar = findViewById(R.id.historicPricesToolbar);
        Utils.setUpCustomAppBar(toolbar, getString(R.string.drawer_item_historic_prices), new CustomNavigationCallback() {
            @Override
            public void onBackArrowPressed() {
                finish();
            }
        });
    }

    /**
     * Init the recycler view with all historic prices from the API
     */
    private void initRecyclerView() {
        final ArrayList<HistoricPrice> historicPrices = new ArrayList<>();

        final HttpRequest httpHistoricPricesRequest = new HttpRequest(HistoricPricesActivity.this, mainLayout, Config.API_BASE_URL + Config.API_HISTORIC_PRICES_INDEX, Request.Method.GET);
        httpHistoricPricesRequest.addBearerToken();
        httpHistoricPricesRequest.executeRequest(new RequestCallback() {
            @Override
            public void getResponse(String response) {
                JSONArray jsonArrayResource = httpHistoricPricesRequest.getJSONArrayResource(response);

                RecyclerView recyclerView = findViewById(R.id.historicPricesRecycler);

                // No result
                if(jsonArrayResource.length() == 0){
                    swipeRefreshLayout.setRefreshing(false);
                    tvNoResult.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }
                // Result found
                else{
                    tvNoResult.setVisibility(View.GONE);

                    // Parse all json data related to Historic Prices
                    for (int i = 0; i < jsonArrayResource.length(); ++i) {
                        try {
                            // Extract historic price info
                            JSONObject jsonObjectResource = jsonArrayResource.getJSONObject(i);
                            String productName = jsonObjectResource.getString("productName");
                            String createdAt = jsonObjectResource.getString("createdAt");
                            String createdBy = jsonObjectResource.getString("createdBy");
                            double oldPrice = jsonObjectResource.getDouble("oldPrice");
                            double newPrice = jsonObjectResource.getDouble("newPrice");
                            boolean seen = jsonObjectResource.getInt("seen") != 0;

                            historicPrices.add(new HistoricPrice(productName, oldPrice, newPrice, seen, createdAt, createdBy));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    // Create the recycler view
                    RelativeLayout recyclerItemLayout = findViewById(R.id.recyclerHistoricPricesLayout);
                    RecyclerViewAdapterHistoricPrices adapter = new RecyclerViewAdapterHistoricPrices(HistoricPricesActivity.this, recyclerItemLayout, historicPrices);
                    recyclerView.setAdapter(adapter);
                    recyclerView.setLayoutManager(new LinearLayoutManager(HistoricPricesActivity.this));
                    swipeRefreshLayout.setRefreshing(false);

                    recyclerView.setVisibility(View.VISIBLE);

                    for (HistoricPrice historicPrice : historicPrices) {
                        if (!historicPrice.isSeen()) {
                            toggleHistoricPricesSeen();
                            break;
                        }
                    }
                }
            }

            @Override
            public void getError400(NetworkResponse networkResponse) {
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void getErrorNoInternet() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    /**
     * Toggle all historic price that are not seen yet (false) to seen (true)
     * It means that we have seen the new lines
     *
     * We run this method as soon as we displayed the recycler and there is some new not seen historic prices,
     * this is implicitly send (no button click needed)
     */
    private void toggleHistoricPricesSeen() {
        final HttpRequest httpRequest = new HttpRequest(HistoricPricesActivity.this, mainLayout, Config.API_BASE_URL + Config.API_HISTORIC_PRICES_TOGGLE_SEEN, Request.Method.PATCH);
        httpRequest.addBearerToken();
        httpRequest.executeRequest(new RequestCallback() {
            @Override
            public void getResponse(String response) { }

            @Override
            public void getError400(NetworkResponse networkResponse) {}

            @Override
            public void getErrorNoInternet() { }
        });
    }
}