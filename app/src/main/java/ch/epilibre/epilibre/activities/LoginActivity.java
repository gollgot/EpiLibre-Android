package ch.epilibre.epilibre.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
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

import java.util.HashMap;
import java.util.Map;

import ch.epilibre.epilibre.Config;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.SessionManager;
import ch.epilibre.epilibre.Utils;
import ch.epilibre.epilibre.http.HttpRequest;
import ch.epilibre.epilibre.http.RequestCallback;
import ch.epilibre.epilibre.user.Role;
import ch.epilibre.epilibre.user.User;

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
        String credentials = email + ":" + Utils.sha256(password);

        final HttpRequest httpLoginRequest = new HttpRequest(LoginActivity.this, layout, Config.API_BASE_URL + Config.API_AUTH_LOGIN, Request.Method.POST);
        httpLoginRequest.addHeader("Authorization", "Basic " + Base64.encodeToString(credentials.getBytes(), Base64.NO_WRAP));
        displayLoader();
        httpLoginRequest.executeRequest(new RequestCallback() {
            @Override
            public void getResponse(String response) {
                removeLoader();
                try {
                    JSONObject jsonObjectResource = httpLoginRequest.getJSONObjectResource(response);
                    User user = new User(
                            jsonObjectResource.getInt("id"),
                            jsonObjectResource.getString("firstname"),
                            jsonObjectResource.getString("lastname"),
                            jsonObjectResource.getString("email"),
                            Role.valueOf(jsonObjectResource.getString("role")),
                            jsonObjectResource.getString("tokenAPI")
                    );

                    // Store the user JWT into session manager and start the MainActivity
                    sessionManager.createLoginSession(user);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    finish();
                    startActivity(intent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void getError400(NetworkResponse networkResponse) {
                // Wrong email or password
                removeLoader();
                etPassword.setError("Email ou mot de passe incorrect");
            }

            @Override
            public void getErrorNoInternet() {
                removeLoader();
            }
        });
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