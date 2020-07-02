package ch.epilibre.epilibre;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;


public class SessionManager {

    private SharedPreferences pref;
    private Editor editor;
    private Context context;

    // SharedPref file name
    private static final String PREF_NAME = "userPref";

    // SharedPref data
    private static final String IS_LOGIN = "isLoggedIn";
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
     */
    public void createLoginSession(User user){
        editor.putBoolean(IS_LOGIN, true);

        // Store user's data
        editor.putString(USER_FIRSTNAME, user.getFirstname());
        editor.putString(USER_LASTNAME, user.getLastname());
        editor.putString(USER_EMAIL, user.getEmail());
        editor.putString(USER_ROLE, user.getRole().toString());

        // commit changes
        editor.commit();
    }


    /**
     * Get connected User
     * @return The connected User or null if not logged in
     */
    public User getUserDetails(){
        return new User(
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