package ch.epilibre.epilibre.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import ch.epilibre.epilibre.Config;
import ch.epilibre.epilibre.CustomNavigationCallback;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.Utils;
import ch.epilibre.epilibre.http.HttpRequest;
import ch.epilibre.epilibre.http.RequestCallback;

public class ProductNewActivity extends AppCompatActivity {

    private static final int LAUNCH_IMAGE_PICKER = 1;

    private LinearLayout layout;
    private ProgressBar loader;
    private Spinner spinnerCategories;
    private Spinner spinnerUnits;
    private ImageView image;
    private ImageView imageDelete;
    private TextInputLayout etName;
    private TextInputLayout etPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_new);

        layout = findViewById(R.id.productNewLayout);
        loader = findViewById(R.id.productNewLoader);
        spinnerCategories = findViewById(R.id.productNewSpinnerCategories);
        spinnerUnits = findViewById(R.id.productNewSpinnerUnits);
        image = findViewById(R.id.productNewImage);
        imageDelete = findViewById(R.id.productNewImageDelete);
        Button btnChooseImage = findViewById(R.id.productNewBtnImage);
        Button btnAdd = findViewById(R.id.productNewBtnAdd);
        etName = findViewById(R.id.productNewEtName);
        etPrice = findViewById(R.id.productNewEtPrice);


        imageDelete.setVisibility(View.GONE);


        // Choose an image
        btnChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, LAUNCH_IMAGE_PICKER);
            }
        });

        // Remove the current image (if we chose one)
        imageDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image.setImageResource(R.drawable.no_image);
                imageDelete.setVisibility(View.GONE);
            }
        });

        // Add product
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Fields of -> set data into the product object and update it into DB
                if(fieldsAreValid(etName, etPrice)){
                    addProduct();
                }
            }
        });


        // Setup the toolbar
        setupCustomToolbar();

        // Load categories and after load units
        loadCategories();
    }

    /**
     * Hide the current AppTheme ActionBar and set a custom toolbar
     * With this custom toolbar we can have a custom arrow back icon with
     * its own custom onClick listener to comeback to the mainActivity without call OnCreate again
     */
    private void setupCustomToolbar() {
        Toolbar toolbar = findViewById(R.id.productNewToolbar);
        Utils.setUpCustomAppBar(toolbar, getString(R.string.product_new_main_title), new CustomNavigationCallback() {
            @Override
            public void onBackArrowPressed() {
                finish();
            }
        });
    }

    /**
     * Load all categories into the spinnerCategory
     */
    private void loadCategories() {
        // Display loader
        layout.setVisibility(View.GONE);
        loader.setVisibility(View.VISIBLE);

        final HttpRequest httpCategoriesRequest = new HttpRequest(ProductNewActivity.this, layout, Config.API_BASE_URL + Config.API_CATEGORIES_INDEX, Request.Method.GET);
        httpCategoriesRequest.addBearerToken();
        httpCategoriesRequest.executeRequest(new RequestCallback() {
            @Override
            public void getResponse(String response) {
                final ArrayList<String> categories = new ArrayList<>();

                // Fetch all categories names from the json response
                JSONArray jsonArrayResource = httpCategoriesRequest.getJSONArrayResource(response);
                // Parse all json users and fill all array lists
                for(int i = 0; i < jsonArrayResource.length(); ++i){
                    try {
                        JSONObject jsonObject = jsonArrayResource.getJSONObject(i);
                        categories.add(jsonObject.getString("name"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // Fill the spinner
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(ProductNewActivity.this, android.R.layout.simple_spinner_item, categories);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategories.setAdapter(dataAdapter);

                loadUnits();
            }

            @Override
            public void getError400(NetworkResponse networkResponse) {

            }

            @Override
            public void getErrorNoInternet() {

            }
        });

    }

    /**
     * Load all the units into the spinnerUnits
     */
    private void loadUnits() {

        final HttpRequest httpUnitsRequest = new HttpRequest(ProductNewActivity.this, layout, Config.API_BASE_URL + Config.API_UNITS_INDEX, Request.Method.GET);
        httpUnitsRequest.addBearerToken();
        httpUnitsRequest.executeRequest(new RequestCallback() {
            @Override
            public void getResponse(String response) {

                final ArrayList<String> units = new ArrayList<>();

                // Fetch all categories names from the json response
                JSONArray jsonArrayResource = httpUnitsRequest.getJSONArrayResource(response);
                // Parse all json users and fill all array lists
                for(int i = 0; i < jsonArrayResource.length(); ++i){
                    try {
                        JSONObject jsonObject = jsonArrayResource.getJSONObject(i);
                        units.add(jsonObject.getString("abbreviation"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // Fill the spinner
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(ProductNewActivity.this, android.R.layout.simple_spinner_item, units);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerUnits.setAdapter(dataAdapter);

                // Remove loader and display the layout
                loader.setVisibility(View.GONE);
                layout.setVisibility(View.VISIBLE);
            }

            @Override
            public void getError400(NetworkResponse networkResponse) {

            }

            @Override
            public void getErrorNoInternet() {

            }
        });

    }

    /**
     * Check if all fields are correct
     * @param etName The product name layout edit text
     * @param etPrice The product price layout edit text
     * @return True if all fields are correct, false otherwise
     */
    private boolean fieldsAreValid(TextInputLayout etName, TextInputLayout etPrice) {
        boolean result = true;

        // Reset errors
        etName.setError(null);
        etPrice.setError(null);

        // Name mandatory
        if(TextUtils.isEmpty(etName.getEditText().getText().toString())){
            result = false;
            etName.setError("Nom du produit obligatoire");
        }

        // Price mandatory
        if(TextUtils.isEmpty(etPrice.getEditText().getText().toString())){
            result = false;
            etPrice.setError("Prix du produit obligatoire");
        }
        // Price must be > 0
        else{
            double price = Double.parseDouble(etPrice.getEditText().getText().toString());
            if(price <= 0){
                result = false;
                etPrice.setError("Le prix doit être supérieur à zéro");
            }
        }

        return result;
    }

    /**
     * Call the API with http request to add a new product
     */
    private void addProduct() {

        final HttpRequest httpAddProductRequest = new HttpRequest(ProductNewActivity.this, layout, Config.API_BASE_URL + Config.API_PRODUCTS_INDEX, Request.Method.POST);
        httpAddProductRequest.addBearerToken();
        httpAddProductRequest.addParam("name", etName.getEditText().getText().toString());
        httpAddProductRequest.addParam("price", etPrice.getEditText().getText().toString());
        httpAddProductRequest.addParam("category", spinnerCategories.getSelectedItem().toString());
        httpAddProductRequest.addParam("unit", spinnerUnits.getSelectedItem().toString());

        // Image management -> if there is an image (btn delete visible)
        // we extract the base64 from the image to store it, otherwise image will be null
        if(imageDelete.getVisibility() == View.VISIBLE){
            image.buildDrawingCache();
            Bitmap bm = image.getDrawingCache();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos); //bm is the bitmap object
            byte[] b = baos.toByteArray();
            String encodedImage = Base64.encodeToString(b , Base64.DEFAULT);

            httpAddProductRequest.addParam("image", encodedImage);
        }

        httpAddProductRequest.executeRequest(new RequestCallback() {
            @Override
            public void getResponse(String response) {
                // Return to the previous activity (ProductsAdminActivity) with OK result
                Intent intent = new Intent();
                intent.putExtra("productName", etName.getEditText().getText().toString());
                setResult(Activity.RESULT_OK,intent);
                finish();
            }

            @Override
            public void getError400(NetworkResponse networkResponse) {

            }

            @Override
            public void getErrorNoInternet() {

            }
        });
    }

    /**
     * When we choose an image from the device, when we select the image, android will return a result code
     * if result code are OK we can decode the stream as bitmap and display it
     * @param requestCode The request code
     * @param resultCode The result code
     * @param data The image data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LAUNCH_IMAGE_PICKER && resultCode == Activity.RESULT_OK) {
            if (data == null) {
                //Display an error
                return;
            }

            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                image.setImageBitmap(bitmap);

                // Enable to delete the current image
                imageDelete.setVisibility(View.VISIBLE);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}