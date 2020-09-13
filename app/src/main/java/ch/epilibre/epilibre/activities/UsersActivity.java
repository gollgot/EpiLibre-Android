package ch.epilibre.epilibre.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import ch.epilibre.epilibre.Config;
import ch.epilibre.epilibre.CustomNavigationCallback;
import ch.epilibre.epilibre.Models.Role;
import ch.epilibre.epilibre.Models.User;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.Utils;
import ch.epilibre.epilibre.http.HttpRequest;
import ch.epilibre.epilibre.http.RequestCallback;
import ch.epilibre.epilibre.recyclers.RecyclerViewAdapterUsers;

public class UsersActivity extends AppCompatActivity {

    private static final int LAUNCH_USERS_EDIT_ACTIVITY = 1;
    private RelativeLayout mainLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mainLayout = findViewById(R.id.usersLayout);
        swipeRefreshLayout = findViewById(R.id.usersSwipeRefreshLayout);
        // Active the loader
        swipeRefreshLayout.setRefreshing(true);

        // Pull to refresh management -> will init again the recyclerview with new users from the API
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(Utils.isConnectedToInternet(UsersActivity.this)){
                    initRecyclerView();
                }else{
                    swipeRefreshLayout.setRefreshing(false);
                    Utils.NoInternetSnackBar(UsersActivity.this, mainLayout);
                }
            }
        });

        setupCustomToolbar();
        initRecyclerView();
    }

    /**
     * Hide the current AppTheme ActionBar and set a custom toolbar
     * With this custom toolbar we can have a custom arrow back icon with
     * its own custom onClick listener
     */
    private void setupCustomToolbar() {
        Toolbar toolbar = findViewById(R.id.usersToolbar);
        setSupportActionBar(toolbar); // To be able to have a menu like a normal actionBar
        Utils.setUpCustomAppBar(toolbar, getResources().getString(R.string.users_main_title), new CustomNavigationCallback() {
            @Override
            public void onBackArrowPressed() {
                finish();
            }
        });
        // Specific for supported action bar, to display custom title
        getSupportActionBar().setTitle(getResources().getString(R.string.users_main_title));
    }

    /**
     * Init the recycler view with new orders from API
     */
    private void initRecyclerView() {
        final ArrayList<User> users = new ArrayList<>();

        final HttpRequest httpUsersRequest = new HttpRequest(UsersActivity.this, mainLayout, Config.API_BASE_URL + Config.API_USERS, Request.Method.GET);
        httpUsersRequest.addBearerToken();
        httpUsersRequest.executeRequest(new RequestCallback() {
            @Override
            public void getResponse(String response) {
                JSONArray jsonArrayResource = httpUsersRequest.getJSONArrayResource(response);
                // Parse all json users and fill all array lists
                for(int i = 0; i < jsonArrayResource.length(); ++i){
                    try {
                        JSONObject jsonObject = jsonArrayResource.getJSONObject(i);
                        JSONObject roleObject = jsonObject.getJSONObject("role");
                        User user = new User(
                                jsonObject.getInt("id"),
                                jsonObject.getString("firstname"),
                                jsonObject.getString("lastname"),
                                jsonObject.getString("email"),
                                Role.valueOf(roleObject.getString("shortName")),
                                roleObject.getString("name"),
                                null
                        );
                        users.add(user);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // Create the recycler view
                RecyclerView recyclerView = findViewById(R.id.usersRecycler);
                RelativeLayout recyclerLayout = findViewById(R.id.recyclerUsersLayout);
                RecyclerViewAdapterUsers adapter = new RecyclerViewAdapterUsers(UsersActivity.this, recyclerLayout, users, UsersActivity.this);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(UsersActivity.this));
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void getError400(NetworkResponse networkResponse) {}

            @Override
            public void getErrorNoInternet() {
                swipeRefreshLayout.setRefreshing(false);
                RecyclerView recyclerView = findViewById(R.id.usersRecycler);
                recyclerView.setVisibility(View.GONE);
            }
        });

    }

    /**
     * Start with result the usersEditActivity (called from the RecyclerViewAdapterUsers)
     * @param user The User to edit
     */
    public void startUsersEditActivity(User user){
        Intent intent = new Intent(UsersActivity.this, UsersEditActivity.class);
        intent.putExtra("user", user);
        startActivityForResult(intent, LAUNCH_USERS_EDIT_ACTIVITY);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            // Came back from ProductEditActivity
            case LAUNCH_USERS_EDIT_ACTIVITY:
                // Result OK
                if(resultCode == Activity.RESULT_OK){
                    String strUser = data.getStringExtra("strUser");
                    Snackbar.make(mainLayout, strUser + " " + getString(R.string.products_admin_edit_successful), Snackbar.LENGTH_SHORT).show();
                    initRecyclerView();
                }
                break;
        }
    }
}