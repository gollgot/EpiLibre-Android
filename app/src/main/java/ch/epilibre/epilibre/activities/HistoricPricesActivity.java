package ch.epilibre.epilibre.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;

import ch.epilibre.epilibre.CustomNavigationCallback;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.Utils;

public class HistoricPricesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_historic_prices);

        setupCustomToolbar();
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
}