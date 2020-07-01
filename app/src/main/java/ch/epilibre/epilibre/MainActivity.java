package ch.epilibre.epilibre;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private final static int LOGIN_ACTIVITY_REQUEST_CODE = 1;
    private TextView tv;
    private static User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = (TextView) findViewById(R.id.mainTv);
    }

    // This method is called when a "startActivityForResult" finishes
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Check that is the LoginActivity
        if (requestCode == LOGIN_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Get boolean data from Intent
                boolean isConnected = data.getBooleanExtra("isConnected", false);
                if(isConnected){
                    user = new User();
                    Snackbar.make(findViewById(android.R.id.content), "Bienvenue", Snackbar.LENGTH_SHORT).show();
                    // Force to reload the options menu (will call the onPrepareOptionsMenu)
                    invalidateOptionsMenu();
                    tv.setText("Connected");
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.guest_menu, menu);
        return true;
    }

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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Handle item selection
        switch (item.getItemId()) {
            case R.id.itemGuestMenuSignIn:
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivityForResult(intent, LOGIN_ACTIVITY_REQUEST_CODE);
                return true;
            case R.id.itemGuestMenuSignOut:
                user = null;
                invalidateOptionsMenu();
                tv.setText("Hello World");
                Snackbar.make(findViewById(android.R.id.content), "Vous vous êtes déconnecté", Snackbar.LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Static method to fetch from which activity we want, the connected user
     * @return The User object (can be null if not successfully log in)
     */
    public static User getUser(){
        return user;
    }
}