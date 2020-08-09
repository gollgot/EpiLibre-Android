package ch.epilibre.epilibre.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.channels.NonReadableChannelException;
import java.util.ArrayList;

import ch.epilibre.epilibre.Config;
import ch.epilibre.epilibre.CustomNavigationCallback;
import ch.epilibre.epilibre.Product;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.Utils;
import ch.epilibre.epilibre.http.HttpRequest;
import ch.epilibre.epilibre.http.RequestCallback;

public class ProductEditActivity extends AppCompatActivity {

    private static final int LAUNCH_IMAGE_PICKER = 1;

    private Product product;
    private String oldImage;
    private LinearLayout layout;
    private ProgressBar loader;
    private Spinner spinnerCategories;
    private Spinner spinnerUnits;
    private ImageView image;
    private ImageView imageRedo;
    private ImageView imageDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_edit);

        product = (Product) getIntent().getSerializableExtra("product");
        oldImage = product.getImage();
        layout = findViewById(R.id.productEditLayout);
        loader = findViewById(R.id.productEditLoader);
        spinnerCategories = findViewById(R.id.productEditSpinnerCategories);
        spinnerUnits = findViewById(R.id.productEditSpinnerUnits);
        final TextInputLayout etName = findViewById(R.id.productEditEtName);
        final TextInputLayout etPrice = findViewById(R.id.productEditEtPrice);
        image = findViewById(R.id.productEditImage);
        Button btnChooseImage = findViewById(R.id.productEditBtnImage);
        imageRedo = findViewById(R.id.productEditImageRedo);
        imageDelete = findViewById(R.id.productEditImageDelete);
        Button btnDelete = findViewById(R.id.productEditBtnDelete);
        Button btnEdit = findViewById(R.id.productEditBtnEdit);

        // Image back appear only when new image is loaded
        imageRedo.setVisibility(View.GONE);

        // Fill fields
        etName.getEditText().setText(product.getName());
        etPrice.getEditText().setText(String.valueOf(product.getPrice()));
        loadProductImage();


        // Choose an image from the device
        btnChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, LAUNCH_IMAGE_PICKER);
            }
        });

        // Come back to initial image
        imageRedo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadProductImage();
                imageRedo.setVisibility(View.GONE);
                imageDelete.setVisibility(View.VISIBLE);
            }
        });

        // Delete the current image (default no image will be used)
        imageDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                image.setImageResource(R.drawable.no_image);
                imageDelete.setVisibility(View.GONE);
                if(oldImage != null){
                    // We can comeback to original image if we want
                    imageRedo.setVisibility(View.VISIBLE);
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new MaterialAlertDialogBuilder(ProductEditActivity.this)
                        .setTitle("Confirmation")
                        .setMessage("Voulez-vous vraiment supprimer le produit " + product.getName() + " ?")
                        .setPositiveButton("Supprimer", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) { }
                        })
                        .setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) { }
                        })
                        .show();
            }
        });

        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fieldsAreValid(etName, etPrice)){

                }
            }
        });

        // Setup the toolbar
        setupCustomToolbar();

        // Load categories and after load units
        loadCategories();
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
     * Load the current product image from base64 to bitmap
     */
    private void loadProductImage(){
        if(product.getImage() == null){
            image.setImageResource(R.drawable.no_image);
            imageDelete.setVisibility(View.GONE);
        }else {
            byte[] imageBytes = Base64.decode(product.getImage(), Base64.DEFAULT);
            Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            image.setImageBitmap(decodedImage);
        }
    }

    /**
     * Load all categories into the spinnerCategory
     */
    private void loadCategories() {
        // Display loader
        layout.setVisibility(View.GONE);
        loader.setVisibility(View.VISIBLE);

        final HttpRequest httpCategoriesRequest = new HttpRequest(ProductEditActivity.this, layout, Config.API_BASE_URL + Config.API_CATEGORIES_INDEX, Request.Method.GET);
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
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(ProductEditActivity.this, android.R.layout.simple_spinner_item, categories);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerCategories.setAdapter(dataAdapter);

                spinnerCategories.setSelection(categories.indexOf(product.getCategory()));
                
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

        final HttpRequest httpUnitsRequest = new HttpRequest(ProductEditActivity.this, layout, Config.API_BASE_URL + Config.API_UNITS_INDEX, Request.Method.GET);
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
                ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(ProductEditActivity.this, android.R.layout.simple_spinner_item, units);
                dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerUnits.setAdapter(dataAdapter);

                spinnerUnits.setSelection(units.indexOf(product.getUnit()));

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
     * Hide the current AppTheme ActionBar and set a custom toolbar
     * With this custom toolbar we can have a custom arrow back icon with
     * its own custom onClick listener to comeback to the mainActivity without call OnCreate again
     */
    private void setupCustomToolbar() {
        Toolbar toolbar = findViewById(R.id.productEditToolbar);
        Utils.setUpCustomAppBar(toolbar, getString(R.string.product_edit_main_title) + product.getName(), new CustomNavigationCallback() {
            @Override
            public void onBackArrowPressed() {
                finish();
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

                // No old image
                if(oldImage == null){
                    // We can only delete the new image
                    imageDelete.setVisibility(View.VISIBLE);
                }
                // There is an old image
                else {
                    // We can comeback to original image if we want
                    imageRedo.setVisibility(View.VISIBLE);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}