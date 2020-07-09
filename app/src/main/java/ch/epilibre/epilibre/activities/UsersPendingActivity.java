package ch.epilibre.epilibre.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toolbar;

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
import ch.epilibre.epilibre.RecyclerViewAdapter;
import ch.epilibre.epilibre.Utils;
import ch.epilibre.epilibre.http.HttpRequest;
import ch.epilibre.epilibre.http.RequestCallback;

public class UsersPendingActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar loader;
    private TextView tvTitle;
    private TextView tvNoData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users_pending);

        loader = findViewById(R.id.usersPendingLoader);
        tvTitle = findViewById(R.id.usersPendingTvTitle);
        tvNoData = findViewById(R.id.usersPendingTvNoData);

        setupCustomToolbar();

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.usersPendingSwipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                initRecyclerView(false);
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        // Init recycler view on resume, this way when we came back to this activity we update the recycler view
        // And first time, onResume are called after onCreate
    }

    @Override
    protected void onResume() {
        super.onResume();

        initRecyclerView(true);
    }

    private void initRecyclerView(boolean withCustomLoader) {
        // This way we can pull to refresh and keep only the pull to refresh loader
        if(withCustomLoader)
            loader.setVisibility(View.VISIBLE);

        final ArrayList<String> emails = new ArrayList<>();
        final ArrayList<String> firstnames = new ArrayList<>();
        final ArrayList<String> lastnames = new ArrayList<>();
        final ArrayList<Integer> ids = new ArrayList<>();

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
                        ids.add(jsonObject.getInt("id"));
                        emails.add(jsonObject.getString("email"));
                        firstnames.add(jsonObject.getString("firstname"));
                        lastnames.add(jsonObject.getString("lastname"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                // user pending title (singular or plural)
                String usersPendingTitle = emails.size() > 1
                        ? getResources().getString(R.string.users_pending_title_plural)
                        : getResources().getString(R.string.users_pending_title_singular);
                tvTitle.setText(emails.size() + " " + usersPendingTitle);

                // If no pending user -> display right text views
                if(emails.size() == 0){
                    tvTitle.setText(getResources().getString(R.string.users_pending_main_title));
                    tvNoData.setText(getResources().getString(R.string.users_pending_no_data));
                    tvNoData.setVisibility(View.VISIBLE);
                }else{
                    tvNoData.setVisibility(View.GONE);
                }

                loader.setVisibility(View.GONE);
                tvTitle.setVisibility(View.VISIBLE);

                // Create the recycler view
                RecyclerView recyclerView = findViewById(R.id.usersPendingRecycler);
                recyclerView.setVisibility(View.VISIBLE);
                RecyclerViewAdapter adapter = new RecyclerViewAdapter(UsersPendingActivity.this, layout, emails, firstnames, lastnames, ids, tvTitle, tvNoData);
                recyclerView.setAdapter(adapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(UsersPendingActivity.this));
            }

            @Override
            public void getError400(NetworkResponse networkResponse) {}

            @Override
            public void getErrorNoInternet() {
                loader.setVisibility(View.GONE);
                RecyclerView recyclerView = findViewById(R.id.usersPendingRecycler);
                recyclerView.setVisibility(View.GONE);
                tvTitle.setText(getResources().getString(R.string.users_pending_main_title));
                tvNoData.setText(getResources().getString(R.string.users_pending_no_connection));
                tvTitle.setVisibility(View.VISIBLE);
                tvNoData.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Hide the current AppTheme ActionBar and set a custom toolbar
     * With this custom toolbar we can have a custom arrow back icon with
     * its own custom onClick listener to comeback to the mainActivity without call OnCreate again
     */
    private void setupCustomToolbar() {
        // Hide the AppTheme ActionBar
        getSupportActionBar().hide();
        Toolbar toolbar = findViewById(R.id.usersPendingToolbar);
        Utils.setUpCustomAppBar(toolbar, new CustomNavigationCallback() {
            @Override
            public void onBackArrowPressed() {
                finish();
            }
        });
    }

}