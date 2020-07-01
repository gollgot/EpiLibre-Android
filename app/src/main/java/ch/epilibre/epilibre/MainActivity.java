package ch.epilibre.epilibre;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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