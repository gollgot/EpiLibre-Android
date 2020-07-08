package ch.epilibre.epilibre.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;

import ch.epilibre.epilibre.Config;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.Utils;
import ch.epilibre.epilibre.http.HttpRequest;
import ch.epilibre.epilibre.http.RequestCallback;

public class RegisterActivity extends AppCompatActivity {

    private RelativeLayout layout;
    private TextInputLayout etFirstname;
    private TextInputLayout etLastname;
    private TextInputLayout etEmail;
    private TextInputLayout etPassword;
    private TextInputLayout etPasswordRepeated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        layout = findViewById(R.id.registerLayout);
        etFirstname = findViewById(R.id.registerEtFirstname);
        etLastname = findViewById(R.id.registerEtLastname);
        etEmail = findViewById(R.id.registerEtEmail);
        etPassword = findViewById(R.id.registerEtPassword);
        etPasswordRepeated = findViewById(R.id.registerEtPasswordRepeated);

        Button btnRegister = findViewById(R.id.registerBtnRegister);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(notEmptyFields() && checkFieldsValidity()){
                    // todo: Send POST request (with sha256 password hash)
                    register();
                }
            }
        });

    }

    /**
     * Clear all error fields
     */
    private void clearErrors(){
        etFirstname.setError(null);
        etLastname.setError(null);
        etEmail.setError(null);
        etPassword.setError(null);
        etPasswordRepeated.setError(null);
    }

    /**
     * Check if all fields are not empty
     * @return True if all fields are not empty, false otherwise
     */
    private boolean notEmptyFields(){
        boolean result = true;

        String firstname = etFirstname.getEditText().getText().toString();
        String lastname = etLastname.getEditText().getText().toString();
        String email = etEmail.getEditText().getText().toString();
        String password = etPassword.getEditText().getText().toString();
        String passwordRepeated = etPasswordRepeated.getEditText().getText().toString();

        clearErrors();

        if(TextUtils.isEmpty(firstname)){
            etFirstname.setError("Prénom requis");
            result = false;
        }
        if(TextUtils.isEmpty(lastname)){
            etLastname.setError("Nom requis");
            result = false;
        }
        if(TextUtils.isEmpty(email)){
            etEmail.setError("Email requis");
            result = false;
        }
        if(TextUtils.isEmpty(password)){
            etPassword.setError("Mot de passe requis");
            result = false;
        }
        if(TextUtils.isEmpty(passwordRepeated)){
            etPasswordRepeated.setError("Répétition mot de passe requise");
            result = false;
        }

        return result;
    }

    /**
     * Check if all fields are valid
     * @return True if all fields are valid, false otherwise
     */
    private boolean checkFieldsValidity(){
        boolean result = true;

        clearErrors();

        String email = etEmail.getEditText().getText().toString();
        String password = etPassword.getEditText().getText().toString();
        String passwordRepeated = etPasswordRepeated.getEditText().getText().toString();

        // Password and repeated password mut be the same
        if(!password.equals(passwordRepeated)){
            etPasswordRepeated.setError("Les mots de passe doivent être identiques");
            result = false;
        }

        // Check email pattern validity
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmail.setError("Email invalid");
            result = false;
        }

        return result;
    }

    /**
     * Send HttpRequest to register the new user
     */
    private void register() {
        final String firstname = etFirstname.getEditText().getText().toString();
        final String lastname = etLastname.getEditText().getText().toString();
        final String email = etEmail.getEditText().getText().toString();
        final String password = etPassword.getEditText().getText().toString();
        final String passwordRepeated = etPasswordRepeated.getEditText().getText().toString();

        HttpRequest httpRegisterRequest = new HttpRequest(RegisterActivity.this, layout, Config.API_BASE_URL + Config.API_AUTH_REGISTER, Request.Method.POST);
        httpRegisterRequest.addParam("firstname", firstname);
        httpRegisterRequest.addParam("lastname", lastname);
        httpRegisterRequest.addParam("email", email);
        httpRegisterRequest.addParam("password", Utils.sha256(password));
        httpRegisterRequest.addParam("passwordRepeated", Utils.sha256(passwordRepeated));
        httpRegisterRequest.executeRequest(new RequestCallback() {
            @Override
            public void getResponse(String response) {
                // Return to the LoginActivity with result OK -> account successfully created
                Intent intent = new Intent();
                setResult(Activity.RESULT_OK,intent);
                finish();
            }

            @Override
            public void getError400(NetworkResponse networkResponse) {
                String errorString = new String(networkResponse.data);
                if (errorString.contains("Email already used")) {
                    etEmail.setError("Cette adresse est déjà utilisée");
                }
            }

            @Override
            public void getErrorNoInternet() {}
        });

    }
}