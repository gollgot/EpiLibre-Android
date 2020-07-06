package ch.epilibre.epilibre;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout etFirstname;
    private TextInputLayout etLastname;
    private TextInputLayout etEmail;
    private TextInputLayout etPassword;
    private TextInputLayout etPasswordRepeated;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

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
        }

        return result;
    }
}