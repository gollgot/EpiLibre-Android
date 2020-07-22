package ch.epilibre.epilibre.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

import ch.epilibre.epilibre.Config;
import ch.epilibre.epilibre.Utils;
import ch.epilibre.epilibre.http.HttpRequest;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.SessionManager;
import ch.epilibre.epilibre.http.RequestCallback;
import ch.epilibre.epilibre.user.Role;
import ch.epilibre.epilibre.user.User;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private ActionBarDrawerToggle actionBarDrawerToggle;

    private SessionManager sessionManager;
    private User user;

    private final static int LAUNCH_PRODUCTS_ACTIVITY = 1;

    private Map<String, Integer> shoppingList;
    private TextView tv;

    private DrawerLayout drawerLayout;

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

        // Shopping list stuff
        shoppingList = new HashMap<>();
        tv = (TextView) findViewById(R.id.main_tv);
        Button btnAddProduct = (Button) findViewById(R.id.mainBtnAddProduct);

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

    }


    @Override
    protected void onResume() {
        super.onResume();

        // SUPER_ADMIN stuff
        if(user.getRole() == Role.SUPER_ADMIN){
            // Reload the pending user badge count in navigation drawer
            // in onResume because all the time we came back to this activity, we update the badge
            NavigationView navigationView = findViewById(R.id.mainNavigationView);
            TextView tvUserPendingCount = (TextView) navigationView.getMenu().findItem(R.id.drawer_menu__item_users_pending).getActionView();
            loadPendingUserBadgeCount(tvUserPendingCount);
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

            // Disable some menu items depends on your role
            NavigationView navigationView = findViewById(R.id.mainNavigationView);
            navigationView.bringToFront(); // Mandatory
            navigationView.setNavigationItemSelectedListener(this);
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
                // Load the pending users badge count into the onResume method
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
     * Update the whole shopping list displaying
     */
    private void updateShoppingList(){
        tv.setText("");
        for(Map.Entry<String, Integer> product : shoppingList.entrySet()){
            tv.append("\n" + product.getKey() + "\t" + product.getValue() + "x");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            // Came back from ProductActivity
            case LAUNCH_PRODUCTS_ACTIVITY:
                // Result OK
                if(resultCode == Activity.RESULT_OK){
                    String productName = data.getStringExtra("product");
                    // Add to the hashmap the product and the quantity
                    if(shoppingList.containsKey(productName)){
                        shoppingList.put(productName, shoppingList.get(productName) + 1);
                    }else{
                        shoppingList.put(productName, 1);
                    }

                    updateShoppingList();
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
        if(!shoppingList.isEmpty()){
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
            case R.id.itemGuestMenuSignOut:
                //invalidateOptionsMenu();
                sessionManager.logoutUser();
                Intent intentLogin = new Intent(MainActivity.this, LoginActivity.class);
                finish();
                startActivity(intentLogin);
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
            }
        }

        //close navigation drawer
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}