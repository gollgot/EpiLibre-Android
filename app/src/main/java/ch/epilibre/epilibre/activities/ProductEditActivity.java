package ch.epilibre.epilibre.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;

import ch.epilibre.epilibre.CustomNavigationCallback;
import ch.epilibre.epilibre.Product;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.Utils;

public class ProductEditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_edit);

        Product product = (Product) getIntent().getSerializableExtra("product");
        
    }

}