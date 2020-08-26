package ch.epilibre.epilibre.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.ArrayList;
import ch.epilibre.epilibre.Models.BasketLine;
import ch.epilibre.epilibre.Config;
import ch.epilibre.epilibre.Models.Order;
import ch.epilibre.epilibre.Models.Product;
import ch.epilibre.epilibre.Utils;
import ch.epilibre.epilibre.http.HttpRequest;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.SessionManager;
import ch.epilibre.epilibre.http.RequestCallback;
import ch.epilibre.epilibre.recyclers.RecyclerViewAdapterBasketLine;
import ch.epilibre.epilibre.Models.Role;
import ch.epilibre.epilibre.Models.User;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private ActionBarDrawerToggle actionBarDrawerToggle;

    private SessionManager sessionManager;
    private User user;

    private final static int LAUNCH_PRODUCTS_ACTIVITY = 1;

    private DrawerLayout drawerLayout;

    private ArrayList<BasketLine> basketLines;
    private TextView tvTotalPrice;
    private RelativeLayout recyclerBasketLayout;
    private LinearLayout mainLayout;
    private RecyclerViewAdapterBasketLine adapter;
    private RecyclerView recyclerViewBasketLines;
    private Button btnCheckout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Fetch the current user (in this activity we have obligatory a connected user)
        sessionManager = new SessionManager(getApplicationContext());
        user = sessionManager.getUser();

        // Load the navigation drawer
        drawerLayout = findViewById(R.id.mainDrawer);
        loadNavigationDrawer();

        Button btnAddProduct = findViewById(R.id.mainBtnAddProduct);
        btnCheckout = findViewById(R.id.mainBtnCheckout);
        tvTotalPrice = findViewById(R.id.mainTvTotalPrice);
        mainLayout = findViewById(R.id.mainLayout);

        // Add new product to the basket
        btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Utils.isConnectedToInternet(MainActivity.this)){
                    Intent intent = new Intent(MainActivity.this, ProductsActivity.class);
                    startActivityForResult(intent, LAUNCH_PRODUCTS_ACTIVITY);
                }else{
                    Utils.NoInternetSnackBar(MainActivity.this, findViewById(R.id.mainLayout));
                }
            }
        });

        // Checkout process
        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String itemsOrtho = basketLines.size() > 1 ? getString(R.string.main_items_plurial) : getString(R.string.main_items_singular);
                double totalPrice = 0;
                for(BasketLine basketLine : basketLines){
                    totalPrice += basketLine.getPrice();
                }

                final double finalTotalPrice = totalPrice;
                new MaterialAlertDialogBuilder(MainActivity.this)
                        .setTitle("Validation")
                        .setMessage("Voulez vous vraiment valider le payement de "
                                + basketLines.size() + " " + itemsOrtho + " pour un total de "
                                + Utils.decimalFormat.format(totalPrice) + " CHF ?")
                        .setPositiveButton("Valider", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                checkout(finalTotalPrice);
                            }
                        })
                        .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) { }
                        })
                        .show();
            }
        });


        initRecyclerView();
    }

    /**
     * Initialisation of the recycler view
     */
    private void initRecyclerView() {
        recyclerBasketLayout = findViewById(R.id.recyclerBasketLinesLayout);
        recyclerViewBasketLines = findViewById(R.id.mainRecycler);
        basketLines = new ArrayList<>();
        updateBasket(basketLines);
    }

    /**
     * Update the recycler view with basket lines
     * @param basketLines ArrayList of BasketLine
     */
    private void updateBasket(ArrayList<BasketLine> basketLines) {
        adapter = new RecyclerViewAdapterBasketLine(MainActivity.this, recyclerBasketLayout, basketLines, tvTotalPrice, btnCheckout);
        recyclerViewBasketLines.setAdapter(adapter);
        recyclerViewBasketLines.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        // Update total price
        Utils.updateTotalPrice(basketLines, tvTotalPrice);

        // Hide or show the checkout button
        if(basketLines.size() > 0){
            btnCheckout.setVisibility(View.VISIBLE);
        }else{
            btnCheckout.setVisibility(View.GONE);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        // SUPER_ADMIN stuff
        if(user.getRole() == Role.SUPER_ADMIN){
            // Reload the pending user and price historics badges count in navigation drawer
            // in onResume because all the time we came back to this activity, we update the badge
            NavigationView navigationView = findViewById(R.id.mainNavigationView);

            TextView tvUserPendingCount = (TextView) navigationView.getMenu().findItem(R.id.drawer_menu__item_users_pending).getActionView();
            loadPendingUserBadgeCount(tvUserPendingCount);

            TextView tvHistoricPricesCount = (TextView) navigationView.getMenu().findItem(R.id.drawer_menu__item_historic_prices).getActionView();
            loadHistoricPricesBadgeCount(tvHistoricPricesCount);
        }
    }

    /**
     * Load the navigation Drawer
     * For SUPER_ADMIN: All menu items
     * For ADMIN: only shop stuff
     * For SELLER: No navigation drawer
     */
    private void loadNavigationDrawer() {
        // Set up the drawer menu and enable the toggle hamburger menu

        // SELLER Restriction
        if(user.getRole() == Role.SELLER){
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }else {
            // Init the drawer
            actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
            drawerLayout.addDrawerListener(actionBarDrawerToggle);
            actionBarDrawerToggle.syncState();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            // NavigationView management
            NavigationView navigationView = findViewById(R.id.mainNavigationView);
            navigationView.bringToFront(); // Mandatory
            navigationView.setNavigationItemSelectedListener(this);

            // Navigation header user name, email, role management
            View headerView = navigationView.getHeaderView(0);
            TextView tvUserName = headerView.findViewById(R.id.navDrawerUserName);
            TextView tvUserEmail = headerView.findViewById(R.id.navDrawerUserEmail);
            tvUserName.setText(user.getFirstname() + " " + user.getLastname() + " - " + user.getRolePretty());
            tvUserEmail.setText(user.getEmail());

            // Disable some menu items depends on your role
            Menu menuNav = navigationView.getMenu();
            MenuItem itemUsers = menuNav.findItem(R.id.drawer_menu__item_users);
            MenuItem itemUsersPending = menuNav.findItem(R.id.drawer_menu__item_users_pending);

            // ADMIN restriction
            if (user.getRole() == Role.ADMIN) {
                itemUsers.setVisible(false);
                itemUsersPending.setVisible(false);
            }
            // SUPER_ADMIN restriction
            else if(user.getRole() == Role.SUPER_ADMIN){
                // /!\ load the pending users and price historics badges count into the onResume method
            }
        }
    }

    /**
     * Send a http request to get the number of user pending validation to display it as a badge into
     * the navigation drawer
     * @param tvUserPendingCount The badge TextView to update
     */
    private void loadPendingUserBadgeCount(final TextView tvUserPendingCount) {
        final HttpRequest httpRequest = new HttpRequest(MainActivity.this, drawerLayout, Config.API_BASE_URL + Config.API_USERS_PENDING, Request.Method.GET);
        httpRequest.addBearerToken();
        httpRequest.executeRequest(new RequestCallback() {
            @Override
            public void getResponse(String response) {
                JSONArray jsonArrayResource = httpRequest.getJSONArrayResource(response);
                tvUserPendingCount.setVisibility(View.VISIBLE);
                // More than 99 users
                if(jsonArrayResource.length() > 99){
                    tvUserPendingCount.setText("99+");
                }
                // Between 1 and 99 users
                else if(jsonArrayResource.length() > 0){
                    tvUserPendingCount.setText(String.valueOf(jsonArrayResource.length()));
                }
                // 0 user
                else{
                    tvUserPendingCount.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void getError400(NetworkResponse networkResponse) { }

            @Override
            public void getErrorNoInternet() {
                tvUserPendingCount.setVisibility(View.INVISIBLE);
            }
        });
    }

    /**
     * Send a http request to get the historic prices not seen count to display it as a badge into
     * the navigation drawer
     * @param tvHistoricPricesCount The badge TextView to update
     */
    private void loadHistoricPricesBadgeCount(final TextView tvHistoricPricesCount) {
        final HttpRequest httpRequest = new HttpRequest(MainActivity.this, drawerLayout, Config.API_BASE_URL + Config.API_HISTORIC_PRICES_NOT_SEEN_COUNT, Request.Method.GET);
        httpRequest.addBearerToken();
        httpRequest.executeRequest(new RequestCallback() {
            @Override
            public void getResponse(String response) {
                JSONObject jsonObjectResource = httpRequest.getJSONObjectResource(response);
                try {
                    int count = jsonObjectResource.getInt("count");
                    tvHistoricPricesCount.setVisibility(View.VISIBLE);

                    // More than 99 price historics
                    if(count > 99){
                        tvHistoricPricesCount.setText("99+");
                    }
                    // Between 1 and 99 price historics
                    else if(count > 0){
                        tvHistoricPricesCount.setText(String.valueOf(count));
                    }
                    // 0 price historics
                    else{
                        tvHistoricPricesCount.setVisibility(View.INVISIBLE);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void getError400(NetworkResponse networkResponse) { }

            @Override
            public void getErrorNoInternet() {
                tvHistoricPricesCount.setVisibility(View.INVISIBLE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            // Came back from ProductActivity
            case LAUNCH_PRODUCTS_ACTIVITY:
                // Result OK
                if(resultCode == Activity.RESULT_OK){
                    BasketLine basketLine = (BasketLine) data.getSerializableExtra("basketLine");
                    basketLines.add(basketLine);
                    updateBasket(basketLines);
                }
                break;
        }
    }

    @Override
    public void onBackPressed()
    {
        // Shopping list not empty -> display dialog box to be sure to quit the app
        // Because back button will stop the app and call again OnCreate() at comeback,
        // so we'll lost the shopping list
        if(!basketLines.isEmpty()){
            new MaterialAlertDialogBuilder(MainActivity.this)
                .setTitle("Liste d'achats en cours")
                .setMessage("Voulez-vous vraiment quitter l'application ?")
                .setPositiveButton("Quitter", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        MainActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) { }
                })
                .show();
        }
        // Shopping list empty, no problem
        else{
            super.onBackPressed();
        }
    }

    /**
     * Send http request to checkout and reset the basket if successful
     * @param totalPrice The total price of the order
     */
    private void checkout(double totalPrice) {
        // Create our 2 string for represent productsId and quantities => e.g: 1,2,3
        StringBuilder productsId = new StringBuilder();
        StringBuilder quantities = new StringBuilder();
        StringBuilder prices = new StringBuilder();

        for(int i = 0; i < basketLines.size(); ++i){
            // First element don't display the semicolon
            if(i > 0){
                productsId.append(";");
                quantities.append(";");
                prices.append(";");
            }

            productsId.append(basketLines.get(i).getProduct().getId());
            quantities.append(basketLines.get(i).getQuantity());
            prices.append(basketLines.get(i).getPrice());
        }

        final HttpRequest httpCheckoutRequest = new HttpRequest(MainActivity.this, mainLayout, Config.API_BASE_URL + Config.API_ORDERS_INDEX, Request.Method.POST);
        httpCheckoutRequest.addBearerToken();
        httpCheckoutRequest.addParam("totalPrice", String.valueOf(totalPrice));
        httpCheckoutRequest.addParam("productsId", productsId.toString());
        httpCheckoutRequest.addParam("quantities", quantities.toString());
        httpCheckoutRequest.addParam("prices", prices.toString());
        httpCheckoutRequest.executeRequest(new RequestCallback() {
            @Override
            public void getResponse(String response) {
                ArrayList<BasketLine> basketLinesBackup = new ArrayList<>(basketLines);

                // Clear the basket
                basketLines.clear();
                updateBasket(basketLines);

                // Fetch the response Order from API to go to the OrderDetail Activity
                JSONObject jsonObjectResource = httpCheckoutRequest.getJSONObjectResource(response);
                try {
                    String date = jsonObjectResource.getString("created_at");
                    String seller = jsonObjectResource.getString("seller");
                    double totalPrice = jsonObjectResource.getDouble("totalPrice");

                    Order order = new Order(date, seller, totalPrice, basketLinesBackup);
                    Intent intentOrderDetails = new Intent(MainActivity.this, OrderDetails.class);
                    intentOrderDetails.putExtra("order", order);
                    intentOrderDetails.putExtra("fromCheckout", true);
                    startActivity(intentOrderDetails);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void getError400(NetworkResponse networkResponse) { }
            @Override
            public void getErrorNoInternet(){ }
        });
    }



    /********* MENU MANAGEMENT *********/


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Enable the Drawer hamburger menu toggle, only for other role than SELLER
        // Because seller doesn't have a drawer menu activated
        if(user.getRole() != Role.SELLER){
            if(actionBarDrawerToggle.onOptionsItemSelected(item)){
                return true;
            }
        }

        //Handle item selection for the menu
        switch (item.getItemId()) {
            case R.id.itemMainMenuSignOut:
                //invalidateOptionsMenu();
                sessionManager.logoutUser();
                Intent intentLogin = new Intent(MainActivity.this, LoginActivity.class);
                finish();
                startActivity(intentLogin);
                return true;
            case R.id.itemMainMenuProfile:
                Intent intentProfile = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intentProfile);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // If no internet -> display snack bar and do nothing
        if(!Utils.isConnectedToInternet(MainActivity.this)){
            Utils.NoInternetSnackBar(MainActivity.this, findViewById(R.id.mainLayout));
        }
        // Internet connection OK -> go to the activity
        else {
            // Handle drawer navigation on item selected
            switch (item.getItemId()) {
                case R.id.drawer_menu__item_users_pending:
                    Intent intentUsersPending = new Intent(MainActivity.this, UsersPendingActivity.class);
                    startActivity(intentUsersPending);
                    break;
                case R.id.drawer_menu__item_products:
                    Intent intentAdminProducts = new Intent(MainActivity.this, ProductsAdminActivity.class);
                    startActivity(intentAdminProducts);
                    break;
                case R.id.drawer_menu__item_orders:
                    Intent intentOrders = new Intent(MainActivity.this, OrdersActivity.class);
                    startActivity(intentOrders);
                    break;
                case R.id.drawer_menu__item_historic_prices:
                    Intent intentHistoricPrices = new Intent(MainActivity.this, HistoricPricesActivity.class);
                    startActivity(intentHistoricPrices);
                    break;
            }
        }

        //close navigation drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}