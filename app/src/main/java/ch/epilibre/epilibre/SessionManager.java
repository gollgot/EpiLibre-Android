package ch.epilibre.epilibre;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import ch.epilibre.epilibre.Models.Role;
import ch.epilibre.epilibre.Models.User;


public class SessionManager {

    private SharedPreferences pref;
    private Editor editor;

    // SharedPref file name
    private static final String PREF_NAME = "userPref";

    // SharedPref data
    private static final String IS_LOGIN = "isLoggedIn";
    private static final String ID = "id";
    private static final String FIRSTNAME = "firstname";
    private static final String LASTNAME = "lastname";
    private static final String EMAIL = "email";
    private static final String ROLE = "role";
    private static final String ROLE_PRETTY = "rolePretty";
    private static final String TOKEN_API = "tokenAPI";

    /**
     * Constructor
     * @param context Current Activity Context
     */
    public SessionManager(Context context){
        this.pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        this.editor = pref.edit();
    }

    /**
     * Create login session
     */
    public void createLoginSession(User user){
        editor.putBoolean(IS_LOGIN, true);

        editor.putInt(ID, user.getId());
        editor.putString(FIRSTNAME, user.getFirstname());
        editor.putString(LASTNAME, user.getLastname());
        editor.putString(EMAIL, user.getEmail());
        editor.putString(ROLE, user.getRole().toString());
        editor.putString(ROLE_PRETTY, user.getRolePretty());
        editor.putString(TOKEN_API, user.getTokenAPI());

        // commit changes
        editor.commit();
    }


    /**
     * Get connected User
     * @return The connected User or null if not logged in
     */
    public User getUser(){
        return new User(
                pref.getInt(ID, 0),
                pref.getString(FIRSTNAME, null),
                pref.getString(LASTNAME, null),
                pref.getString(EMAIL, null),
                Role.valueOf(pref.getString(ROLE, null)),
                pref.getString(ROLE_PRETTY, null),
                pref.getString(TOKEN_API, null)
        );
    }

    /**
     * Get the user's tokenAPI
     * @return the user's tokenAPI
     */
    public String getToken(){
        return pref.getString(TOKEN_API, null);
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