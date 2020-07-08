package ch.epilibre.epilibre;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

import ch.epilibre.epilibre.user.Role;
import ch.epilibre.epilibre.user.User;


public class SessionManager {

    private SharedPreferences pref;
    private Editor editor;
    private Context context;

    // SharedPref file name
    private static final String PREF_NAME = "userPref";

    // SharedPref data
    private static final String IS_LOGIN = "isLoggedIn";
    private static final String JWT = "jwt";

    /**
     * Constructor
     * @param context Current Activity Context
     */
    public SessionManager(Context context){
        this.context = context;
        this.pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = pref.edit();
    }

    /**
     * Create login session
     */
    public void createLoginSession(String jwt){
        editor.putBoolean(IS_LOGIN, true);
        editor.putString(JWT, jwt);

        // commit changes
        editor.commit();
    }


    /**
     * Get connected User
     * @return The connected User or null if not logged in
     */
    public User getUser(){
        return tokenToUser(pref.getString(JWT, null));
    }

    /**
     * Log out the user
     * Clear all preference and add IS_LOGIN -> false
     */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        editor.clear().commit();
        editor.putBoolean(IS_LOGIN, false);
    }

    /**
     * Check if the user is already logged in
     * @return True if user already logged in, false otherwise
     */
    // Get Login State
    public boolean isLoggedIn(){
        return pref.getBoolean(IS_LOGIN, false);
    }

    /**
     * Extract User object from the JWT token
     * @param token The JWT token
     * @return The User stored into the JWT token
     */
    private User tokenToUser(String token){
        String tokenData = token.split("\\.")[1];
        byte[] data = Base64.decode(tokenData, Base64.DEFAULT);
        String text = new String(data, StandardCharsets.UTF_8);

        User user = null;
        try {
            JSONObject jsonObjectData = new JSONObject(text);
            user = new User(
                    jsonObjectData.getString("firstname"),
                    jsonObjectData.getString("lastname"),
                    jsonObjectData.getString("email"),
                    Role.valueOf(jsonObjectData.getString("role"))
            );
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return user;
    }

}