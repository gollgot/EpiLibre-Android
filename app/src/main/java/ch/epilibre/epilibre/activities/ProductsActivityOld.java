package ch.epilibre.epilibre.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.widget.Toolbar;

import ch.epilibre.epilibre.CustomNavigationCallback;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.Utils;

public class ProductsActivityOld extends AppCompatActivity {

    private Button btnProduct1;
    private Button btnProduct2;
    private Button btnProduct3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products_old);

        setupCustomToolbar();

        btnProduct1 = (Button) findViewById(R.id.products_btn1);
        btnProduct2 = (Button) findViewById(R.id.products_btn2);
        btnProduct3 = (Button) findViewById(R.id.products_btn3);

        btnProduct1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnData(ProductsActivityOld.this.btnProduct1);
            }
        });

        btnProduct2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnData(ProductsActivityOld.this.btnProduct2);
            }
        });

        btnProduct3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnData(ProductsActivityOld.this.btnProduct3);
            }
        });


    }

    /**
     * Hide the current AppTheme ActionBar and set a custom toolbar
     * With this custom toolbar we can have a custom arrow back icon with
     * its own custom onClick listener to comeback to the mainActivity with no result
     */
    private void setupCustomToolbar() {
        Toolbar toolbar = findViewById(R.id.productsToolbar);
        Utils.setUpCustomAppBar(toolbar, new CustomNavigationCallback() {
            @Override
            public void onBackArrowPressed() {
                // Return to the MainActivity without result
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_CANCELED, returnIntent);
                finish();
            }
        });
    }

    /**
     * Return data to the MainActivity
     * @param button The called Button
     */
    private void returnData(Button button){
        Intent intent = new Intent();
        intent.putExtra("product",button.getText().toString());
        setResult(Activity.RESULT_OK,intent);
        finish();
    }
}