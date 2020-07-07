package ch.epilibre.epilibre;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private TextInputLayout etEmail;
    private TextInputLayout etPassword;

    private RelativeLayout layout;
    private RelativeLayout layoutBackgroundLoader;
    private ProgressBar loader;

    private final static int LAUNCH_REGISTER_ACTIVITY = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Check if we already logged in -> skip the login activity and launch the MainActivity
        sessionManager = new SessionManager(getApplicationContext());
        if(sessionManager.isLoggedIn()){
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            finish();
            startActivity(intent);
        }

        layout = findViewById(R.id.loginLayout);
        layoutBackgroundLoader = findViewById(R.id.loginLayoutLoaderBackground);
        loader = findViewById(R.id.loginLoader);
        etEmail = findViewById(R.id.loginEtEmail);
        etPassword = findViewById(R.id.loginEtPassword);
        TextView tvRegister = findViewById(R.id.loginTvRegister);
        Button btnLogin = findViewById(R.id.loginBtnConnection);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = etEmail.getEditText().getText().toString();
                String password = etPassword.getEditText().getText().toString();

                if(formInputsCorrect(email, password)){
                    authenticate(email, password);
                }
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivityForResult(intent, LAUNCH_REGISTER_ACTIVITY);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            // Came back from ProductActivity
            case LAUNCH_REGISTER_ACTIVITY:
                // Result OK
                if(resultCode == Activity.RESULT_OK){
                    Snackbar.make(layout, "Votre compte à bien été créé et est en attente de validation", Snackbar.LENGTH_LONG).show();
                }
                break;
        }
    }

    /**
     * Check all form input (not the data coherence)
     * @return True if all inputs form are corrects false otherwise
     */
    private boolean formInputsCorrect(String email, String password){
        boolean result = true;

        etEmail.setError(null);
        etPassword.setError(null);

        if(TextUtils.isEmpty(email)){
            etEmail.setError("Email requis");
            result = false;
        }
        if(TextUtils.isEmpty(password)){
            etPassword.setError("Mot de passe requis");
            result = false;
        }

        return result;
    }

    /**
     * Try to authenticate the user
     * If success: Set the user and comeback to MainActivity
     * If fail: Set error and wait
     */
    private void authenticate(final String email, final String password){

        displayLoader();

        final String url = Config.API_BASE_URL + Config.API_AUTH_LOGIN;
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                removeLoader();

                try {
                    JSONObject jsonObjectResponse = new JSONObject(response);
                    JSONObject jsonObjectResource = jsonObjectResponse.getJSONObject("resource");
                    String token = jsonObjectResource.getString("token");

                    // Store into session manager all user data and start the MainActivity
                    sessionManager.createLoginSession(token);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    finish();
                    startActivity(intent);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                removeLoader();

                // No internet connection error
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Snackbar.make(layout, "Aucune connexion, veuillez vérifier votre connexion internet.", Snackbar.LENGTH_SHORT).show();
                }
                // Other error
                else {
                    NetworkResponse networkResponse = error.networkResponse;
                    // Error 400 -> Wrong login request
                    if (networkResponse.statusCode == 400) {
                        etPassword.setError("Email ou mot de passe incorrect");
                    }
                    // Other error -> modal alert
                    else {
                        new MaterialAlertDialogBuilder(LoginActivity.this)
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

            //This is for Headers If You Needed
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String credentials = email + ":" + Utils.sha256(password);
                String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                return headers;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }

    /**
     * Display a loader ahead a 80% black screen
     */
    private void displayLoader(){
        // Disable user interaction with the screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        // Display black screen and loader
        layoutBackgroundLoader.setVisibility(View.VISIBLE);
        loader.setVisibility(View.VISIBLE);
    }

    /**
     * Remove the loader
     */
    private void removeLoader(){
        // Remove black screen and loader
        layoutBackgroundLoader.setVisibility(View.GONE);
        loader.setVisibility(View.GONE);
        // get user interaction with screen back
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}