package ch.epilibre.epilibre;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;
import androidx.appcompat.widget.Toolbar;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utils {

    public static final String APP_NAME = "EpiLibre";

    /**
     * Hash a string with SHA-256 algorithm
     * @param value The value we want to hash
     * @return The SHA-256 representation of the value
     */
    public static String sha256(String value) {
        try {
            MessageDigest digest = null;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e1) {
                e1.printStackTrace();
            }
            digest.reset();
            return Utils.bin2hex(digest.digest(value.getBytes()));
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * Bin to hex transformer
     * @param data bytes data
     * @return String value of the bin data
     */
    private static String bin2hex(byte[] data) {
        StringBuilder hex = new StringBuilder(data.length * 2);
        for (byte b : data)
            hex.append(String.format("%02x", b & 0xFF));
        return hex.toString();
    }

    /**
     * Set up a custom Toolbar in place as the default App bar
     * @param toolbar The custom Toolbar
     * @param callback The CustomNavigationCallback when we'll press the back arrow
     */
    public static void setUpCustomAppBar(Toolbar toolbar, final CustomNavigationCallback callback){
        toolbar.setTitle(Utils.APP_NAME);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onBackArrowPressed();
            }
        });
    }

    /**
     * Check if the device is connected to internet
     * @param context The Context
     * @return True if there is an internet connection, false otherwise
     */
    public static boolean isConnectedToInternet(Context context){
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
    
}
