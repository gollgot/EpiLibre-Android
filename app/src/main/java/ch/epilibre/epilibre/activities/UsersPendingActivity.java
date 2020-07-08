package ch.epilibre.epilibre.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toolbar;

import ch.epilibre.epilibre.CustomNavigationCallback;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.Utils;

public class UsersPendingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_pending);

        setupCustomToolbar();
    }

    /**
     * Hide the current AppTheme ActionBar and set a custom toolbar
     * With this custom toolbar we can have a custom arrow back icon with
     * its own custom onClick listener to comeback to the mainActivity without call OnCreate again
     */
    private void setupCustomToolbar() {
        // Hide the AppTheme ActionBar
        getSupportActionBar().hide();
        Toolbar toolbar = findViewById(R.id.usersPendingToolbar);
        Utils.setUpCustomAppBar(toolbar, new CustomNavigationCallback() {
            @Override
            public void onBackArrowPressed() {
                finish();
            }
        });
    }
}