package ch.epilibre.epilibre;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private TextInputLayout etEmail;
    private TextInputLayout etPassword;
    private Button btnLogin;

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

        etEmail = findViewById(R.id.loginEtEmail);
        etPassword = findViewById(R.id.loginEtPassword);
        btnLogin = (Button) findViewById(R.id.loginBtnConnection);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(formInputsCorrect()){
                    authenticate();
                }
            }
        });

    }

    /**
     * Check all form input (not the data coherence)
     * @return True if all inputs form are corrects false otherwise
     */
    private boolean formInputsCorrect(){
        boolean result = true;

        etEmail.setError(null);
        etPassword.setError(null);

        if(TextUtils.isEmpty(etEmail.getEditText().getText())){
            etEmail.setError("Email requis");
            result = false;
        }
        if(TextUtils.isEmpty(etPassword.getEditText().getText())){
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
    private void authenticate(){
        String targetEmail1 = "loic";
        String targetPassword1 = "loic";

        String targetEmail2 = "sarah";
        String targetPassword2 = "sarah";

        String targetEmail3 = "david";
        String targetPassword3 = "david";

        String emailEntered = etEmail.getEditText().getText().toString();
        String passwordEntered = etPassword.getEditText().getText().toString();

        // Auth OK
        if(emailEntered.equalsIgnoreCase(targetEmail1) && passwordEntered.equalsIgnoreCase(targetPassword1)){
            // Store into session manager all user data and start the MainActivity
            sessionManager.createLoginSession(1, "Loïc", "Dessaules", "loic.dessaules@heig-vd.ch", "SUPER_ADMIN");
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            finish();
            startActivity(intent);
        }else if(emailEntered.equalsIgnoreCase(targetEmail2) && passwordEntered.equalsIgnoreCase(targetPassword2)){
            // Store into session manager all user data and start the MainActivity
            sessionManager.createLoginSession(2, "Sarah", "Voirin", "sarah.voirin@epfl.ch", "ADMIN");
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            finish();
            startActivity(intent);
        }else if(emailEntered.equalsIgnoreCase(targetEmail3) && passwordEntered.equalsIgnoreCase(targetPassword3)){
            // Store into session manager all user data and start the MainActivity
            sessionManager.createLoginSession(3, "David", "Dupond", "david.dupond@epfl.ch", "SELLER");
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            finish();
            startActivity(intent);
        }
        // Error
        else{
            etPassword.setError("Email ou mot de passe incorrect");
        }
    }
}