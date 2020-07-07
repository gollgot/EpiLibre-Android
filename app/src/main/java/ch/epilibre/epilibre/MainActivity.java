package ch.epilibre.epilibre;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ActionBarDrawerToggle actionBarDrawerToggle;

    private SessionManager sessionManager;
    private User user;

    private final static int LAUNCH_PRODUCTS_ACTIVITY = 1;
    private Map<String, Integer> shoppingList;
    private TextView tv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Fetch the current user (in this activity we have obligatory a connected user)
        sessionManager = new SessionManager(getApplicationContext());
        user = sessionManager.getUser();

        // Load the navigation drawer
        loadNavigationDrawer();

        // Shopping list stuff
        shoppingList = new HashMap<>();
        tv = (TextView) findViewById(R.id.main_tv);
        Button btnAddProduct = (Button) findViewById(R.id.mainBtnAddProduct);

        btnAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ProductsActivity.class);
                startActivityForResult(intent, LAUNCH_PRODUCTS_ACTIVITY);
            }
        });
    }

    /**
     * Load the navigation Drawer
     * For SUPER_ADMIN: All menu items
     * For ADMIN: only shop stuff
     * For SELLER: No navigation drawer
     */
    private void loadNavigationDrawer() {
        // Set up the drawer menu and enable the toggle mhamburger menu
        DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.mainDrawer);

        if(user.getRole() == Role.SELLER){
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }else {
            actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.drawer_open, R.string.drawer_close);
            drawerLayout.addDrawerListener(actionBarDrawerToggle);
            actionBarDrawerToggle.syncState();
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            // Disable some menu items depends on your role
            NavigationView navigationView = findViewById(R.id.mainNavigationView);
            Menu menuNav = navigationView.getMenu();
            MenuItem itemUsers = menuNav.findItem(R.id.drawer_menu__item_users);

            // ADMIN restriction
            if (user.getRole() == Role.ADMIN) {
                itemUsers.setVisible(false);
            }
        }
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

        //Handle item selection
        switch (item.getItemId()) {
            case R.id.itemGuestMenuSignOut:
                //invalidateOptionsMenu();
                sessionManager.logoutUser();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                finish();
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


}