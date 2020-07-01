package ch.epilibre.epilibre;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.DragEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toolbar;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private SessionManager sessionManager;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Fetch the current user (in this activity we have obligatory a connected user)
        SessionManager sessionManager = new SessionManager(getApplicationContext());
        user = sessionManager.getUserDetails();
        Snackbar.make(findViewById(android.R.id.content), "Bienvenue " + user.getFirstname() + " " + user.getLastname() , Snackbar.LENGTH_SHORT).show();

        loadDrawerMenu();


    }

    private void loadDrawerMenu() {
        // Set up the drawer menu and enable the toggle mhamburger menu
        drawerLayout = (DrawerLayout) findViewById(R.id.mainDrawer);

        if(user.getRole() == Role.SELLER){
            drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }else {
            actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.login_hint_email, R.string.login_hint_password);
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
                SessionManager sessionManager = new SessionManager(getApplicationContext());
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