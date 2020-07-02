package ch.epilibre.epilibre;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.Activity;
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

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private SessionManager sessionManager;
    private User user;

    private final static int LAUNCH_PRODUCTS_ACTIVITY = 1;
    private Map<String, Integer> products;
    private TextView tv;
    private Button btnProducts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Fetch the current user (in this activity we have obligatory a connected user)
        sessionManager = new SessionManager(getApplicationContext());
        user = sessionManager.getUserDetails();
        Snackbar.make(findViewById(android.R.id.content), "Bienvenue " + user.getFirstname() + " " + user.getLastname() , Snackbar.LENGTH_SHORT).show();

        loadNavigationDrawer();

        products = new HashMap<>();
        tv = (TextView) findViewById(R.id.main_tv);
        btnProducts = (Button) findViewById(R.id.mainBtnProducts);

        btnProducts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ProductsActivity.class);
                startActivityForResult(intent, LAUNCH_PRODUCTS_ACTIVITY);
            }
        });
    }

    private void loadNavigationDrawer() {
        // Set up the drawer menu and enable the toggle mhamburger menu
        drawerLayout = (DrawerLayout) findViewById(R.id.mainDrawer);

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

    private void updateProducts(){
        tv.setText("");
        for(Map.Entry<String, Integer> product : products.entrySet()){
            tv.append("\n" + product.getKey() + "\t" + product.getValue() + "x");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            case LAUNCH_PRODUCTS_ACTIVITY:
                if(resultCode == Activity.RESULT_OK){
                    String productName = data.getStringExtra("product");
                    if(products.containsKey(productName)){
                        products.put(productName, products.get(productName) + 1);
                    }else{
                        products.put(productName, 1);
                    }

                    updateProducts();
                }
                break;
        }
    }

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



    /*
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(user != null) {
            menu.findItem(R.id.itemGuestMenuSignIn).setVisible(false);
            menu.findItem(R.id.itemGuestMenuSignOut).setVisible(true);
        }else{
            menu.findItem(R.id.itemGuestMenuSignIn).setVisible(true);
            menu.findItem(R.id.itemGuestMenuSignOut).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }*/

}