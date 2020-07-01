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

    private TextInputLayout etEmail;
    private TextInputLayout etPassword;
    private Button btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
        String targetEmail = "loic.dessaules@heig-vd.ch";
        String targetPassword = "loic";

        String emailEntered = etEmail.getEditText().getText().toString();
        String passwordEntered = etPassword.getEditText().getText().toString();

        // Auth OK
        if(emailEntered.equalsIgnoreCase(targetEmail) && passwordEntered.equalsIgnoreCase(targetPassword)){

            // Create the connected user
            User user = new User(1, "Loïc", "Dessaules", "loic.dessaules@heig-vd.ch", Role.SUPER_ADMIN);
            MainActivity.setUser(user);

            // Comeback to previous activity (MainActivity) and notify it that result is OK and we are connected
            Intent intent = new Intent();
            intent.putExtra("isConnected", true);
            setResult(RESULT_OK, intent);
            finish();
        }
        // Error
        else{
            etPassword.setError("Email ou mot de passe incorrect");
        }
    }
}