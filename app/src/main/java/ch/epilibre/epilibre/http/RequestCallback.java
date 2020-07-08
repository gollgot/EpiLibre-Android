package ch.epilibre.epilibre.http;

import com.android.volley.NetworkResponse;

public interface RequestCallback {
    void getResponse(String response);
    void getError400(NetworkResponse networkResponse);
}
