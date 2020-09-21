package ch.epilibre.epilibre.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import ch.epilibre.epilibre.Config;
import ch.epilibre.epilibre.CustomNavigationCallback;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.SessionManager;
import ch.epilibre.epilibre.Utils;
import ch.epilibre.epilibre.http.HttpRequest;
import ch.epilibre.epilibre.http.RequestCallback;

public class ProfileActivity extends AppCompatActivity {

    private static final int LAUNCH_CHANGE_PASSWORD_ACTIVITY = 1;

    private RelativeLayout layout;
    private SessionManager sessionManager;
    private ProgressBar loader;
    private RelativeLayout loaderBackground;
    private boolean hasBeenUpdated = false;

    private String oldFirstName;
    private String oldLastName;
    private String oldEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setupCustomToolbar();

        sessionManager = new SessionManager(this);
        layout = findViewById(R.id.profileLayout);
        loader = findViewById(R.id.profileLoader);
        loaderBackground = findViewById(R.id.profileLayoutLoaderBackground);
        final TextInputLayout etFirstName = findViewById(R.id.profileEtFirstName);
        final TextInputLayout etLastName = findViewById(R.id.profileEtLastName);
        final TextInputLayout etEmail = findViewById(R.id.profileEtEmail);
        TextInputLayout etRole = findViewById(R.id.profileEtRole);
        Button btnEdit = findViewById(R.id.profileBtnEdit);
        Button btnChangePassword = findViewById(R.id.profileBtnChangePassword);

        // Fill all edit text
        etFirstName.getEditText().setText(sessionManager.getUser().getFirstname());
        etLastName.getEditText().setText(sessionManager.getUser().getLastname());
        etEmail.getEditText().setText(sessionManager.getUser().getEmail());
        etRole.getEditText().setText(sessionManager.getUser().getRolePretty());
        etRole.getEditText().setEnabled(false);

        // Fetch all old values
        fetchOldValues(etFirstName, etLastName, etEmail);

        // Button edit clicked
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Fields must be valid
                if(fieldsAreValid(etFirstName, etLastName, etEmail)){
                    // Fields must have at least one change (to prevent useless API call)
                    if(fieldsHasChanged(etFirstName.getEditText().getText().toString(), etLastName.getEditText().getText().toString(), etEmail.getEditText().getText().toString(), oldFirstName, oldLastName, oldEmail)){
                        updateProfile(etFirstName, etLastName, etEmail);
                    }else{
                        Snackbar.make(layout, R.string.profile_no_change, Snackbar.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Button change password clicked
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProfileActivity.this, ChangePasswordActivity.class);
                startActivityForResult(intent, LAUNCH_CHANGE_PASSWORD_ACTIVITY);
            }
        });

    }

    @Override
    public void onBackPressed() {
        returnResultToPreviousActivity();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            // Came back from ProductEditActivity
            case LAUNCH_CHANGE_PASSWORD_ACTIVITY:
                // Result OK
                if(resultCode == Activity.RESULT_OK){
                    Snackbar.make(layout, getString(R.string.profile_pass_change_successful), Snackbar.LENGTH_LONG).show();
                }
                break;
        }
    }

    /**
     * Hide the current AppTheme ActionBar and set a custom toolbar
     * With this custom toolbar we can have a custom arrow back icon with
     * its own custom onClick listener to comeback to the previous activity
     */
    private void setupCustomToolbar() {
        Toolbar toolbar = findViewById(R.id.profileToolbar);
        Utils.setUpCustomAppBar(toolbar, getString(R.string.profile_main_title), new CustomNavigationCallback() {
            @Override
            public void onBackArrowPressed() {
                returnResultToPreviousActivity();
            }
        });
    }

    /**
     * Fetch old values from edit text
     * @param etFirstName The first name layout edit text
     * @param etLastName The last name layout edit text
     * @param etEmail The email layout edit text
     */
    private void fetchOldValues(TextInputLayout etFirstName, TextInputLayout etLastName, TextInputLayout etEmail) {
        oldFirstName = etFirstName.getEditText().getText().toString();
        oldLastName = etLastName.getEditText().getText().toString();
        oldEmail = etEmail.getEditText().getText().toString();
    }

    /**
     * Return to the previous activity and return the result
     * RESULT_OK if an update have been made, RESULT_CANCELED otherwise
     */
    private void returnResultToPreviousActivity() {
        Intent returnIntent = new Intent();
        if(hasBeenUpdated){
            setResult(Activity.RESULT_OK, returnIntent);
        }else{
            setResult(Activity.RESULT_CANCELED, returnIntent);
        }

        finish();
    }

    /**
     * Check if all fields are correct
     * @param etFirstName The first name layout edit text
     * @param etLastName The last name layout edit text
     * @param etEmail The email layout edit text
     * @return True if all fields are correct, false otherwise
     */
    private boolean fieldsAreValid(TextInputLayout etFirstName, TextInputLayout etLastName, TextInputLayout etEmail) {
        boolean result = true;

        // Reset errors
        etFirstName.setError(null);
        etLastName.setError(null);
        etEmail.setError(null);

        // First name mandatory
        if(TextUtils.isEmpty(etFirstName.getEditText().getText().toString())){
            result = false;
            etFirstName.setError("Prénom obligatoire");
        }

        // Last name mandatory
        if(TextUtils.isEmpty(etLastName.getEditText().getText().toString())){
            result = false;
            etLastName.setError("Nom obligatoire");
        }

        // Email mandatory
        if(TextUtils.isEmpty(etEmail.getEditText().getText().toString())){
            result = false;
            etEmail.setError("Email obligatoire");
        }

        return result;
    }

    /**
     * Check if at least one field has changed
     * @param firstName The first name
     * @param lastName The last name
     * @param email The email
     * @param oldFirstName The old first name value
     * @param oldLastName The old last name value
     * @param oldEmail The old email value
     * @return True if at least one field has changed, false otherwise
     */
    private boolean fieldsHasChanged(String firstName, String lastName, String email, String oldFirstName, String oldLastName, String oldEmail){
        return !oldFirstName.equals(firstName) || !oldLastName.equals(lastName) || !oldEmail.equals(email);
    }

    /**
     * Update the profile with an http request to the API
     * @param etFirstName The first name layout edit text
     * @param etLastName The last name layout edit text
     * @param etEmail The email layout edit text
     */
    private void updateProfile(final TextInputLayout etFirstName, final TextInputLayout etLastName, final TextInputLayout etEmail){
        displayLoader();

        HttpRequest httpUpdateProfileRequest = new HttpRequest(ProfileActivity.this, layout, Config.API_BASE_URL + Config.API_PROFILE, Request.Method.PATCH);
        httpUpdateProfileRequest.addBearerToken();
        httpUpdateProfileRequest.addParam("firstname", etFirstName.getEditText().getText().toString());
        httpUpdateProfileRequest.addParam("lastname", etLastName.getEditText().getText().toString());
        httpUpdateProfileRequest.addParam("email", etEmail.getEditText().getText().toString());
        httpUpdateProfileRequest.executeRequest(new RequestCallback() {
            @Override
            public void getResponse(String response) {
                // Update the session variable (shared preferences)
                sessionManager.updateFirstName(etFirstName.getEditText().getText().toString());
                sessionManager.updateLastName(etLastName.getEditText().getText().toString());
                sessionManager.updateEmail(etEmail.getEditText().getText().toString());
                Snackbar.make(layout, R.string.profile_update_successful, Snackbar.LENGTH_LONG).show();

                removeLoader();
                hasBeenUpdated = true; // Notify that an update has been made
                fetchOldValues(etFirstName, etLastName, etEmail);
            }

            @Override
            public void getError400(NetworkResponse networkResponse) {
                String errorString = new String(networkResponse.data);
                if (errorString.toLowerCase().contains("email already used")) {
                    etEmail.setError(getString(R.string.profile_email_already_used));
                }

                removeLoader();
            }

            @Override
            public void getErrorNoInternet() {
                removeLoader();
            }
        });
    }

    /**
     * Display the loader
     */
    public void displayLoader(){
        loaderBackground.setVisibility(View.VISIBLE);
        loader.setVisibility(View.VISIBLE);
    }

    /**
     * Remove the loader
     */
    public void removeLoader(){
        loader.setVisibility(View.GONE);
        loaderBackground.setVisibility(View.GONE);
    }

}