package ch.epilibre.epilibre;

import androidx.appcompat.app.AppCompatActivity;

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

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
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

    private RelativeLayout layoutBackgroundLoader;
    private ProgressBar loader;

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

        layoutBackgroundLoader = findViewById(R.id.loginLayoutLoaderBackground);
        loader = findViewById(R.id.loginLoader);
        etEmail = findViewById(R.id.loginEtEmail);
        etPassword = findViewById(R.id.loginEtPassword);
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

        final String url = "https://epilibre.gollgot.app/api/auth/login";
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                removeLoader();

                try {
                    JSONObject jsonObjectResponse = new JSONObject(response);
                    JSONObject jsonObjectResource = jsonObjectResponse.getJSONObject("resource");
                    String token = jsonObjectResource.getString("token");

                    User user = tokenToUser(token);
                    // Store into session manager all user data and start the MainActivity
                    sessionManager.createLoginSession(user);
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

                NetworkResponse networkResponse = error.networkResponse;
                // Error 400 -> Wrong login request
                if (networkResponse.statusCode == 400) {
                    etPassword.setError("Email ou mot de passe incorrect");
                }
                // Other error -> modal alert
                else{
                    new MaterialAlertDialogBuilder(LoginActivity.this)
                        .setTitle("Une erreur est survenue")
                        .setMessage("Veuillez réessayer plus tard ou contacter un administrateur")
                        .setPositiveButton("Fermer", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) { }
                        })
                        .show();
                }
            }
        }) {

            //This is for Headers If You Needed
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String credentials = email + ":" + sha256(password);
                String base64EncodedCredentials = Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP);
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Basic " + base64EncodedCredentials);
                return headers;
            }
            /*
            //Pass Your Parameters here
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("User", UserName);
                params.put("Pass", PassWord);
                return params;
            }*/
        };
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(request);
    }

    /**
     * Hash a string with SHA-256 algorithm
     * @param value The value we want to hash
     * @return The SHA-256 representation of the value
     */
    private String sha256(String value) {
        try {
            MessageDigest digest = null;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e1) {
                e1.printStackTrace();
            }
            digest.reset();
            return bin2hex(digest.digest(value.getBytes()));
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Bin to hex transformer
     * @param data bytes data
     * @return String value of the bin data
     */
    private String bin2hex(byte[] data) {
        StringBuilder hex = new StringBuilder(data.length * 2);
        for (byte b : data)
            hex.append(String.format("%02x", b & 0xFF));
        return hex.toString();
    }

    /**
     * Extract User object from the JWT token
     * @param token The JWT token
     * @return The User stored into the JWT token
     */
    private User tokenToUser(String token){
        String tokenData = token.split("\\.")[1];
        byte[] data = Base64.decode(tokenData, Base64.DEFAULT);
        String text = new String(data, StandardCharsets.UTF_8);

        User user = null;
        try {
            JSONObject jsonObjectData = new JSONObject(text);
            user = new User(
                jsonObjectData.getString("firstname"),
                jsonObjectData.getString("lastname"),
                jsonObjectData.getString("email"),
                Role.valueOf(jsonObjectData.getString("role"))
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return user;
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