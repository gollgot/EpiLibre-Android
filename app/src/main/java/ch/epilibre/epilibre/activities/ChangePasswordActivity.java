package ch.epilibre.epilibre.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import ch.epilibre.epilibre.Config;
import ch.epilibre.epilibre.CustomNavigationCallback;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.Utils;
import ch.epilibre.epilibre.http.HttpRequest;
import ch.epilibre.epilibre.http.RequestCallback;

public class ChangePasswordActivity extends AppCompatActivity {

    private RelativeLayout mainLayout;
    private ProgressBar loader;
    private RelativeLayout loaderBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        setupCustomToolbar();

        mainLayout = findViewById(R.id.changePasswordLayout);
        loader = findViewById(R.id.changePasswordLoader);
        loaderBackground = findViewById(R.id.changePasswordLoaderBackground);
        final TextInputLayout etOldPass = findViewById(R.id.changePasswordEtOldPassword);
        final TextInputLayout etnewPass= findViewById(R.id.changePasswordEtNewPassword);
        final TextInputLayout etNewPassRepeated = findViewById(R.id.changePasswordEtNewPasswordRepeated);
        Button btnEdit = findViewById(R.id.changePasswordBtnEdit);

        // Button edit clicked
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(fieldsAreValid(etOldPass, etnewPass, etNewPassRepeated)){
                    updatePassword(etOldPass, etnewPass, etNewPassRepeated);
                }
            }
        });
    }

    /**
     * Hide the current AppTheme ActionBar and set a custom toolbar
     * With this custom toolbar we can have a custom arrow back icon with
     * its own custom onClick listener to comeback to the previous activity
     */
    private void setupCustomToolbar() {
        Toolbar toolbar = findViewById(R.id.changePasswordToolbar);
        Utils.setUpCustomAppBar(toolbar, getString(R.string.change_password_main_title), new CustomNavigationCallback() {
            @Override
            public void onBackArrowPressed() {
                finish();
            }
        });
    }

    /**
     * Check if all fields are correct
     * @param etOldPass The old password layout edit text
     * @param etnewPass The new password layout edit text
     * @param etNewPassRepeated The new password repeated edit text
     * @return True if all fields are correct, false otherwise
     */
    private boolean fieldsAreValid(TextInputLayout etOldPass, TextInputLayout etnewPass, TextInputLayout etNewPassRepeated) {
        boolean result = true;

        // Reset errors
        etOldPass.setError(null);
        etnewPass.setError(null);
        etNewPassRepeated.setError(null);

        // First name mandatory
        if(TextUtils.isEmpty(etOldPass.getEditText().getText().toString())){
            result = false;
            etOldPass.setError("Mot de passe obligatoire");
        }

        // Last name mandatory
        if(TextUtils.isEmpty(etnewPass.getEditText().getText().toString())){
            result = false;
            etnewPass.setError("Nouveau mot de passe obligatoire");
        }

        // Email mandatory
        if(TextUtils.isEmpty(etNewPassRepeated.getEditText().getText().toString())){
            result = false;
            etNewPassRepeated.setError("Répétition obligatoire");
        }

        // If someone is empty -> return the false result
        if(!result){
            return result;
        }
        // Check the content validity
        else{
            String newPass = etnewPass.getEditText().getText().toString();
            String newPassRepeated = etNewPassRepeated.getEditText().getText().toString();

            if(!newPass.equals(newPassRepeated)){
                result = false;
                etNewPassRepeated.setError("Les mots de passe ne correspondent pas");
            }
        }

        return result;
    }

    /**
     * Update the password with http request to the API
     * @param etOldPass The old password layout edit text
     * @param etnewPass The new password layout edit text
     * @param etNewPassRepeated The new password repeated edit text
     */
    private void updatePassword(final TextInputLayout etOldPass, TextInputLayout etnewPass, TextInputLayout etNewPassRepeated) {
        displayLoader();

        HttpRequest httpPasswordRequest = new HttpRequest(ChangePasswordActivity.this, mainLayout, Config.API_BASE_URL + Config.API_PROFILE_PASSWORD, Request.Method.PATCH);
        httpPasswordRequest.addBearerToken();
        httpPasswordRequest.addParam("oldPassword", etOldPass.getEditText().getText().toString());
        httpPasswordRequest.addParam("newPassword", etnewPass.getEditText().getText().toString());
        httpPasswordRequest.addParam("newPasswordRepeated", etNewPassRepeated.getEditText().getText().toString());
        httpPasswordRequest.executeRequest(new RequestCallback() {
            @Override
            public void getResponse(String response) {
                removeLoader();
                // Return to previous activity and notify the update
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }

            @Override
            public void getError400(NetworkResponse networkResponse) {
                String errorString = new String(networkResponse.data);
                if (errorString.toLowerCase().contains("incorrect old password")) {
                    etOldPass.setError(getString(R.string.change_password_incorrect_old_password));
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