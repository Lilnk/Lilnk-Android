package ir.lilnk;

import android.content.Context;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static ir.lilnk.MainActivity.LongURL;

public class ApiService {
    private Context context;

    public ApiService(Context context) {
        this.context = context;
    }

    public void getIPData(final OnIPInfoReceived URL_Data) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, BuildConfig.Secret_API + LongURL
                , null, response -> {
            Log.i("MyApp", "onResponse: " + response.toString());
            URL_Data.OnReceived(parsResponse(response));
        }, error -> {
            Log.i("MyAppErr", "onErrorResponse: " + error.toString());
            URL_Data.OnReceived(null);
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put(BuildConfig.Secret_APIHeader, BuildConfig.Secret_APIHeaderValue);
                return params;
            }
        };
        request.setRetryPolicy(new DefaultRetryPolicy(10000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue rQueue = Volley.newRequestQueue(context);
        rQueue.add(request);
    }

    private Lilink parsResponse(JSONObject response) {
        try {
            Lilink lilink = new Lilink();
            lilink.setUrl(response.getString("url"));
            return lilink;

        } catch (JSONException e) {
            Log.e("JSONErr", "JSONException: ", e);
            return null;
        }
    }

    public interface OnIPInfoReceived {
        void OnReceived(Lilink lilink);
    }

}

class Lilink {

    private static String url;

    public static String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

}

