package ch.epilibre.epilibre;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import java.util.HashMap;

public class SessionManager {

    private SharedPreferences pref;
    private Editor editor;
    private Context context;

    // SharedPref file name
    private static final String PREF_NAME = "userPref";

    // SharedPref data
    private static final String IS_LOGIN = "isLoggedIn";
    private static final String USER_ID = "id";
    private static final String USER_FIRSTNAME = "firstname";
    private static final String USER_LASTNAME = "lastName";
    private static final String USER_EMAIL = "email";
    private static final String USER_ROLE = "role";

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
     * @param id The user ID
     * @param firstname The user firstname
     * @param lastname The user lastname
     * @param email The user email
     * @param role The user Role
     */
    public void createLoginSession(int id, String firstname, String lastname, String email, String role){
        editor.putBoolean(IS_LOGIN, true);

        // Store user's data
        editor.putInt(USER_ID, id);
        editor.putString(USER_FIRSTNAME, firstname);
        editor.putString(USER_LASTNAME, lastname);
        editor.putString(USER_EMAIL, email);
        editor.putString(USER_ROLE, role);

        // commit changes
        editor.commit();
    }


    /**
     * Get connected User
     * @return The connected User or null if not logged in
     */
    public User getUserDetails(){
        return new User(
                pref.getInt(USER_ID, -1),
                pref.getString(USER_FIRSTNAME, null),
                pref.getString(USER_LASTNAME, null),
                pref.getString(USER_EMAIL, null),
                Role.valueOf(pref.getString(USER_ROLE, null))
        );
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

}