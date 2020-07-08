package ch.epilibre.epilibre.http;

import android.content.Context;
import android.content.DialogInterface;
import android.view.ViewGroup;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import ch.epilibre.epilibre.SessionManager;

public class HttpRequest {

    private Context context;
    private ViewGroup layout;
    private String url;
    private int method;

    private Map<String, String> params;
    private Map<String, String> headers;

    /**
     * Constructor
     * @param context The Context
     * @param layout The main Layout of the Activity
     * @param url The URL to call
     * @param method The http method
     */
    public HttpRequest(Context context, ViewGroup layout, String url, int method) {
        this.context = context;
        this.layout = layout;
        this.url = url;
        this.method = method;

        this.params = new HashMap<>();
        this.headers = new HashMap<>();
    }

    /**
     * Add the connected user JWT as bearer token authorization header
     */
    public void addBearerToken(){
        addHeader("Authorization", "Bearer " + new SessionManager(context).getToken());
    }

    /**
     * Add new http body param
     * @param key The key
     * @param value The value
     */
    public void addParam(String key, String value) {
        params.put(key, value);
    }

    /**
     * Add new http header
     * @param key The key
     * @param value The value
     */
    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    /**
     * Execute the HTTP request
     * @param callback The request callback
     */
    public void executeRequest(final RequestCallback callback){
        StringRequest request = new StringRequest(method, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                callback.getResponse(response);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                // No internet connection error
                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Snackbar.make(layout, "Aucune connexion, veuillez vérifier votre connexion internet.", Snackbar.LENGTH_SHORT).show();
                    callback.getErrorNoInternet();
                }
                // Other error
                else {
                    NetworkResponse networkResponse = error.networkResponse;
                    // Error 400 Bad request
                    if (networkResponse != null && networkResponse.statusCode == 400) {
                        callback.getError400(networkResponse);
                    }
                    // Other error
                    else{
                        new MaterialAlertDialogBuilder(context)
                                .setTitle("Une erreur est survenue")
                                .setMessage("Veuillez réessayer plus tard ou contacter un administrateur")
                                .setPositiveButton("Fermer", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                })
                                .show();
                    }
                }
            }
        }) {
            // Parameters
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
            // Headers
            @Override
            public Map<String, String> getHeaders() {
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }

    /**
     * Parse the response String to return the resource JSONArray
     * @param response The String response from the HttpRequest
     * @return The JSONArray resource
     */
    public JSONArray getJSONArrayResource(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            return jsonObject.getJSONArray("resource");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Parse the response String to return the resource JSONObject
     * @param response The String response from the HttpRequest
     * @return The JsonObject resource
     */
    public JSONObject getJSONObjectResource(String response){
        try {
            JSONObject jsonObject = new JSONObject(response);
            return jsonObject.getJSONObject("resource");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
