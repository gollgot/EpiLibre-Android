package ch.epilibre.epilibre.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ch.epilibre.epilibre.Config;
import ch.epilibre.epilibre.CustomNavigationCallback;
import ch.epilibre.epilibre.Models.Role;
import ch.epilibre.epilibre.Models.User;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.Utils;
import ch.epilibre.epilibre.http.HttpRequest;
import ch.epilibre.epilibre.http.RequestCallback;

public class UsersEditActivity extends AppCompatActivity {

    private User user;
    private RelativeLayout mainLayout;
    private Spinner spinnerRole;
    private ProgressBar loader;
    private RelativeLayout loaderBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_edit);

        setupCustomToolbar();

        user = (User) getIntent().getSerializableExtra("user");

        mainLayout = findViewById(R.id.usersEditLayout);
        loader = findViewById(R.id.usersEditLoader);
        loaderBackground = findViewById(R.id.usersEditLoaderBackground);
        TextInputLayout etFirstName = findViewById(R.id.usersEditEtFirstName);
        TextInputLayout etLastName = findViewById(R.id.usersEditEtLastName);
        TextInputLayout etEmail = findViewById(R.id.usersEditEtEmail);
        spinnerRole = findViewById(R.id.usersEditSpinnerRole);
        Button btnEdit = findViewById(R.id.usersEditBtnEdit);

        // Setup all edit text and disabled them
        etFirstName.getEditText().setText(user.getFirstname());
        etLastName.getEditText().setText(user.getLastname());
        etEmail.getEditText().setText(user.getEmail());

        etFirstName.getEditText().setEnabled(false);
        etLastName.getEditText().setEnabled(false);
        etEmail.getEditText().setEnabled(false);

        // Fill the spinner
        List<Role> roles = Arrays.asList(Role.values());
        ArrayList<String> rolesValues = new ArrayList<>();
        for(Role role : roles){
            rolesValues.add(role.name());
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(UsersEditActivity.this, android.R.layout.simple_spinner_item, rolesValues);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRole.setAdapter(dataAdapter);

        // Select the good role (enum not pretty translate) maybe refactor later
        spinnerRole.setSelection(roles.indexOf(user.getRole()));

        // Edit
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(user.getRole() == Role.valueOf((String)spinnerRole.getSelectedItem())){
                    Snackbar.make(mainLayout, getString(R.string.profile_no_change), Snackbar.LENGTH_SHORT).show();
                }else{
                    updateUser();
                }
            }
        });
    }

    /**
     * Hide the current AppTheme ActionBar and set a custom toolbar
     * With this custom toolbar we can have a custom arrow back icon with
     * its own custom onClick listener
     */
    private void setupCustomToolbar() {
        Toolbar toolbar = findViewById(R.id.usersEditToolbar);
        setSupportActionBar(toolbar); // To be able to have a menu like a normal actionBar
        Utils.setUpCustomAppBar(toolbar, getResources().getString(R.string.users_edit_main_title), new CustomNavigationCallback() {
            @Override
            public void onBackArrowPressed() {
                finish();
            }
        });
        // Specific for supported action bar, to display custom title
        getSupportActionBar().setTitle(getResources().getString(R.string.users_edit_main_title));
    }

    /**
     * Update the user with an http request to the API
     */
    private void updateUser(){
        displayLoader();

        HttpRequest httpUpdateUserRequest = new HttpRequest(UsersEditActivity.this, mainLayout, Config.API_BASE_URL + Config.API_USERS_UPDATE(user.getId()), Request.Method.PATCH);
        httpUpdateUserRequest.addBearerToken();
        httpUpdateUserRequest.addParam("role", (String) spinnerRole.getSelectedItem());
        httpUpdateUserRequest.executeRequest(new RequestCallback() {
            @Override
            public void getResponse(String response) {
                removeLoader();

                // Return to previous activity and notify the user that has been updated
                Intent returnIntent = new Intent();
                returnIntent.putExtra("strUser", user.getFirstname() + " " + user.getLastname());
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }

            @Override
            public void getError400(NetworkResponse networkResponse) {
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