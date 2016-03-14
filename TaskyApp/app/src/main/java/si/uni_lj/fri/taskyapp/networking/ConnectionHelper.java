package si.uni_lj.fri.taskyapp.networking;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;

import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import si.uni_lj.fri.taskyapp.R;
import si.uni_lj.fri.taskyapp.global.AppHelper;

/**
 * Created by urgas9 on 30. 10. 2015.
 */
public class ConnectionHelper {

    public final static int CONNECTION_TIMEOUT = 5000;
    public final static int READ_TIMEOUT = 15000;
    public final static int CODE_SUCCESS = 200;
    private static final String LOG_TAG = "NetworkingHelper";
    private static final String LANGUAGE = "en";

    /**
     * Method posts data to server, it is intended to use it in a background thread
     *
     * @param ctx          Context from which it is invoked
     * @param headerEntity Data to be sent with request
     * @return
     */
    public static String postHttpDataCustomUrl(Context ctx, String httpUrl, Object headerEntity) {
        return postHttpDataCustomUrl(ctx.getApplicationContext(), httpUrl, headerEntity, String.class).getContent();
    }

    public static void prepareUrlConnectionParameters(Context ctx, HttpURLConnection conn) throws ProtocolException {
        conn.setRequestMethod("POST");
        conn.setRequestProperty("X-Requested-With", "oh_mobile_client");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("X-Accept-Language", LANGUAGE);
        conn.setRequestProperty("Accept", "application/json");
        conn.setReadTimeout(READ_TIMEOUT);
        conn.setConnectTimeout(CONNECTION_TIMEOUT);
        conn.setDoInput(true);
        conn.setDoOutput(true);
    }

    public static <T> ConnectionResponse<T> postHttpDataCustomUrl(Context ctx, String httpUrl, Object headerEntity, Class<T> gsonClass) {
        String LOG_TAG = ctx.getClass().getSimpleName();
        HttpURLConnection conn = null;
        ConnectionResponse ncr = new ConnectionResponse();
        try {
            // Get
            URL url = new URL(httpUrl);

            conn = (HttpURLConnection) url.openConnection();

            prepareUrlConnectionParameters(ctx, conn);

            // Adding parameters, prepare the bundle of data to be sent

            String entityContentString;
            if (headerEntity instanceof String) {
                entityContentString = headerEntity.toString();
            } else {
                entityContentString = new Gson().toJson(headerEntity);
            }

            Log.d(LOG_TAG, "Posting to URL: " + httpUrl);
            Log.d(LOG_TAG, "Sending entity: " + entityContentString);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(entityContentString);
            writer.flush();
            writer.close();
            os.close();

            // Send the request
            Log.d(LOG_TAG, "Sending API post request.");
            // Get hold of the response entity
            if (conn.getResponseCode() == CODE_SUCCESS) {
                Log.d(LOG_TAG, "Connection successful");
                if (gsonClass == String.class) {
                    ncr.setContent(AppHelper.convertStreamToString(conn.getInputStream()));
                } else {
                    ncr.setContent(new Gson().fromJson(new InputStreamReader(conn.getInputStream()), gsonClass));
                }
            } else {
                Log.d(LOG_TAG, "Connection error at: " + httpUrl + ", code: " + conn.getResponseCode());
                ncr.setContent(null);
            }
            // Debugging print out options
            if (AppHelper.isDebugEnabled() && ncr.getContent() != null) {
                Object content = ncr.getContent();
                if (content instanceof String) {
                    Log.d(LOG_TAG, content.toString());
                } else {
                    Log.d(LOG_TAG, new Gson().toJson(content));
                }
            }
            ncr.setResponseCode(conn.getResponseCode());
        } catch (Exception e) {
            Log.e(LOG_TAG, "Cannot post data to server!");
            Log.e(LOG_TAG, "Message: " + e.getMessage());
            ncr.setResponseCode(-1);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        return ncr;
    }

    public static String getHttpJsonString(Context ctx, String urlString) {
        return getHttpJsonStringAndStatus(ctx, urlString).getContent();
    }

    /**
     * Method downloads json from server and return java object.
     * Json is not converted to string but uses buffered reader instead
     *
     * @param ctx
     * @param urlString url to be request
     * @return
     */
    public static ConnectionResponse<String> getHttpJsonStringAndStatus(Context ctx, String urlString) {
        return getHttpGsonObjectAndStatus(ctx, urlString, String.class);
    }

    public static <T> ConnectionResponse<T> getHttpGsonObjectAndStatus(Context ctx, String urlString, Class<T> gsonClass) {

        Log.d(LOG_TAG, "Requesting URL: " + urlString);
        ConnectionResponse ncr = new ConnectionResponse();

        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(urlString);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(READ_TIMEOUT);

            urlConnection.setRequestProperty("User-Agent", ctx.getString(R.string.app_name) + " (Android app)");
            urlConnection.setRequestProperty("X-Accept-Language", LANGUAGE);
            ncr.setResponseCode(urlConnection.getResponseCode());
            if (urlConnection.getResponseCode() == CODE_SUCCESS) {
                if (gsonClass == String.class) {
                    ncr.setContent(AppHelper.convertStreamToString(urlConnection.getInputStream()));
                } else {
                    T responseContent = new Gson().fromJson(new InputStreamReader(urlConnection.getInputStream()), gsonClass);
                    if (AppHelper.isDebugEnabled()) {
                        Log.d(LOG_TAG, new Gson().toJson(responseContent));
                    }
                    ncr.setContent(responseContent);
                }

            } else {
                ncr.setContent(null);
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Cannot get response from server!");
            Log.e(LOG_TAG, "Message: " + e.getMessage());
            ncr.setResponseCode(-1);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return ncr;
    }

}
