package ch.epilibre.epilibre;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class ProductsActivity extends AppCompatActivity {

    private Button btnProduct1;
    private Button btnProduct2;
    private Button btnProduct3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_products);

        btnProduct1 = (Button) findViewById(R.id.products_btn1);
        btnProduct2 = (Button) findViewById(R.id.products_btn2);
        btnProduct3 = (Button) findViewById(R.id.products_btn3);

        btnProduct1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnData(ProductsActivity.this.btnProduct1);
            }
        });

        btnProduct2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnData(ProductsActivity.this.btnProduct2);
            }
        });

        btnProduct3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnData(ProductsActivity.this.btnProduct3);
            }
        });


    }

    private void returnData(Button button){
        Intent intent = new Intent();
        intent.putExtra("product",button.getText().toString());
        setResult(Activity.RESULT_OK,intent);
        finish();
    }
}