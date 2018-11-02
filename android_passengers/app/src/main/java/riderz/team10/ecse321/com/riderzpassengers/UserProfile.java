package riderz.team10.ecse321.com.riderzpassengers;

import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import riderz.team10.ecse321.com.riderzpassengers.constants.HTTP;
import riderz.team10.ecse321.com.riderzpassengers.constants.TAG;
import riderz.team10.ecse321.com.riderzpassengers.constants.URL;
import riderz.team10.ecse321.com.riderzpassengers.http.HttpRequestClient;

public class UserProfile extends Activity implements HttpRequestClient {

    // Provides access in the url
    private String username;

    // Information concerning the driver
    private JSONObject jsonUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        username = getIntent().getStringExtra("username");

        TextView userName = (TextView)findViewById(R.id.userName);
        TextView userEmail = (TextView)findViewById(R.id.userEmail);
        TextView userPhone = (TextView)findViewById(R.id.userPhone);
        TextView userFirstName = (TextView)findViewById(R.id.userFirstName);
        TextView userLastName = (TextView)findViewById(R.id.userLastName);
        mapButtons();

        syncHttpRequest();
        try {
            userName.setText("Username:\n" + jsonUser.getString("username"));
            userEmail.setText("Email:\n" + jsonUser.getString("email"));
            userPhone.setText("Phone:\n" + jsonUser.getString("phone"));
            userFirstName.setText("First Name:\n" + jsonUser.getString("firstName"));
            userLastName.setText("Last Name:\n" + jsonUser.getString("lastName"));
        } catch (JSONException e) {

        }

    }

    @Override
    public void mapButtons() {

    }

    @Override
    public void syncHttpRequest() {
        final RequestParams params = new RequestParams();
        params.add("username", username);

        // Instantiate a new Runnable object which will handle http requests asynchronously
        // Then await until thread is finished to make the request synchronous.
        Thread t = new Thread(new Runnable() {
            // URL to target
            final String userUrl = URL.baseUrl + "users/getUser";

            @Override
            public void run() {
                // Instantiate new synchronous http client, set timeout and perform get request
                // Uses extended timeout
                SyncHttpClient client = new SyncHttpClient();
                client.setTimeout(HTTP.maxTimeoutExtended);
                client.get(userUrl, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        try {
                            jsonUser = new JSONObject(new String(responseBody));
                        } catch (JSONException e) {

                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          byte[] responseBody, Throwable error) {
                    }
                });
            }
        });
        t.start();

        // Await until thread t has finished its execution before proceeding
        try {
            t.join();
        } catch (InterruptedException e) {
        }
    }
}
