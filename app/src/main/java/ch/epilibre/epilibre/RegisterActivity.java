package ch.epilibre.epilibre;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.android.volley.AuthFailureError;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

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

    private void register() {
        final String firstname = etFirstname.getEditText().getText().toString();
        final String lastname = etLastname.getEditText().getText().toString();
        final String email = etEmail.getEditText().getText().toString();
        final String password = etPassword.getEditText().getText().toString();
        final String passwordRepeated = etPasswordRepeated.getEditText().getText().toString();

        final String url = Config.API_BASE_URL + Config.API_AUTH_REGISTER;
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Return to the LoginActivity with result OK -> account successfully created
                Intent intent = new Intent();
                setResult(Activity.RESULT_OK,intent);
                finish();
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // No internet connection error
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Snackbar.make(layout, "Aucune connexion, veuillez vérifier votre connexion internet.", Snackbar.LENGTH_SHORT).show();
                }
                // Other error
                else {
                    NetworkResponse networkResponse = error.networkResponse;

                    // No internet connection error
                    if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                        Snackbar.make(layout, "Aucune connexion, veuillez vérifier votre connexion internet.", Snackbar.LENGTH_SHORT).show();
                    }
                    // Known error
                    else if(networkResponse != null && networkResponse.data != null){
                        String errorString = new String(networkResponse.data);
                        // Error 400 -> Wrong login request
                        if (networkResponse.statusCode == 400 && errorString.contains("Email already used")) {
                            etEmail.setError("Cette adresse est déjà utilisée");
                        }
                    }
                    // Other error -> modal alert
                    else {
                        new MaterialAlertDialogBuilder(RegisterActivity.this)
                            .setTitle("Une erreur est survenue")
                            .setMessage("Veuillez réessayer plus tard ou contacter un administrateur")
                            .setPositiveButton("Fermer", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .show();
                    }
                }
            }
        }) {
            // Parameters
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("firstname", firstname);
                params.put("lastname", lastname);
                params.put("email", email);
                params.put("password", Utils.sha256(password));
                params.put("passwordRepeated", Utils.sha256(passwordRepeated));
                return params;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }
}