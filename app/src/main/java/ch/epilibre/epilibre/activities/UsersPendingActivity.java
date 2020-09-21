package ch.epilibre.epilibre.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ch.epilibre.epilibre.Config;
import ch.epilibre.epilibre.CustomNavigationCallback;
import ch.epilibre.epilibre.R;
import ch.epilibre.epilibre.recyclers.RecyclerViewAdapterUsersPending;
import ch.epilibre.epilibre.Utils;
import ch.epilibre.epilibre.http.HttpRequest;
import ch.epilibre.epilibre.http.RequestCallback;
import ch.epilibre.epilibre.Models.Role;
import ch.epilibre.epilibre.Models.User;

public class UsersPendingActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvNoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_pending);

        tvNoData = findViewById(R.id.usersPendingTvNoData);

        setupCustomToolbar();

        swipeRefreshLayout = findViewById(R.id.usersPendingSwipeRefreshLayout);
        // Active the loader
        swipeRefreshLayout.setRefreshing(true);

        // Pull to refresh management -> will init again the recyclerview with new users pending from the API
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(Utils.isConnectedToInternet(UsersPendingActivity.this)){
                    initRecyclerView();
                }else{
                    swipeRefreshLayout.setRefreshing(false);
                    Utils.NoInternetSnackBar(UsersPendingActivity.this, findViewById(R.id.usersPendingLayout));
                }

            }
        });

        initRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initRecyclerView() {
        final ArrayList<User> users = new ArrayList<>();

        final RelativeLayout layout = findViewById(R.id.usersPendingLayout);
        final HttpRequest httpUsersPendingRequest = new HttpRequest(UsersPendingActivity.this, layout,Config.API_BASE_URL + Config.API_USERS_PENDING, Request.Method.GET);
        httpUsersPendingRequest.addBearerToken();
        httpUsersPendingRequest.executeRequest(new RequestCallback() {
            @Override
            public void getResponse(String response) {
                JSONArray jsonArrayResource = httpUsersPendingRequest.getJSONArrayResource(response);
                // Parse all json users and fill all array lists
                for(int i = 0; i < jsonArrayResource.length(); ++i){
                    try {
                        JSONObject jsonObject = jsonArrayResource.getJSONObject(i);
                        User user = new User(
                                jsonObject.getInt("id"),
                                jsonObject.getString("firstname"),
                                jsonObject.getString("lastname"),
                                jsonObject.getString("email"),
                                Role.SELLER, // Pending user can only be a Seller
                                "Vendeur",
                                null
                        );
                        users.add(user);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // If no pending user -> display right text views
                if(users.size() == 0){
                    tvNoData.setText(getResources().getString(R.string.users_pending_no_data));
                    tvNoData.setVisibility(View.VISIBLE);
                }else{
                    tvNoData.setVisibility(View.GONE);
                }


                // Create the recycler view
                RecyclerView recyclerView = findViewById(R.id.usersPendingRecycler);
                recyclerView.setVisibility(View.VISIBLE);
                RecyclerViewAdapterUsersPending adapter = new RecyclerViewAdapterUsersPending(UsersPendingActivity.this, layout, users);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(UsersPendingActivity.this));
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void getError400(NetworkResponse networkResponse) {}

            @Override
            public void getErrorNoInternet() {
                swipeRefreshLayout.setRefreshing(false);
                RecyclerView recyclerView = findViewById(R.id.usersPendingRecycler);
                recyclerView.setVisibility(View.GONE);
            }
        });
    }

    /**
     * Hide the current AppTheme ActionBar and set a custom toolbar
     * With this custom toolbar we can have a custom arrow back icon with
     * its own custom onClick listener to comeback to the mainActivity without call OnCreate again
     */
    private void setupCustomToolbar() {
        Toolbar toolbar = findViewById(R.id.usersPendingToolbar);
        Utils.setUpCustomAppBar(toolbar, getResources().getString(R.string.users_pending_main_title), new CustomNavigationCallback() {
            @Override
            public void onBackArrowPressed() {
                finish();
            }
        });
    }

}