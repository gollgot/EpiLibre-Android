package ch.epilibre.epilibre.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import ch.epilibre.epilibre.R;

/**
 * Splash screen activity, this activity is the main launcher activity and has a specific theme
 * (style.SplashScreenTheme) (manifest) this way there is a background with app logo while the app is loading.
 *
 * When the activity is loaded, we pass to the login
 */
public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Set the noAction bar theme before the super.onCreate
        setTheme(R.style.AppThemeNoActionBar);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Load the login activity
        Intent loginIntent = new Intent(SplashScreenActivity.this, LoginActivity.class);
        SplashScreenActivity.this.startActivity(loginIntent);
        SplashScreenActivity.this.finish();
    }
}