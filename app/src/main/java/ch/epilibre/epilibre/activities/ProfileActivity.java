package ch.epilibre.epilibre.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.widget.Spinner;

import com.google.android.material.textfield.TextInputLayout;

import ch.epilibre.epilibre.CustomNavigationCallback;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.SessionManager;
import ch.epilibre.epilibre.Utils;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        setupCustomToolbar();

        SessionManager sessionManager = new SessionManager(this);

        TextInputLayout etFirstName = findViewById(R.id.profileEtFirstName);
        TextInputLayout etLastName = findViewById(R.id.profileEtLastName);
        TextInputLayout etEmail = findViewById(R.id.profileEtEmail);
        TextInputLayout etRole = findViewById(R.id.profileEtRole);

        etFirstName.getEditText().setText(sessionManager.getUser().getFirstname());
        etLastName.getEditText().setText(sessionManager.getUser().getLastname());
        etEmail.getEditText().setText(sessionManager.getUser().getEmail());
        etRole.getEditText().setText(sessionManager.getUser().getRolePretty());
        etRole.getEditText().setEnabled(false);

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
                finish();
            }
        });
    }

}