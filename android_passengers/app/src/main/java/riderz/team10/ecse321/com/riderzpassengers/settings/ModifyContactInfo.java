package riderz.team10.ecse321.com.riderzpassengers.settings;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import cz.msebera.android.httpclient.Header;
import riderz.team10.ecse321.com.riderzpassengers.R;
import riderz.team10.ecse321.com.riderzpassengers.assets.templates.activity.AppCompatActivityBack;
import riderz.team10.ecse321.com.riderzpassengers.assets.verificator.RegexVerification;
import riderz.team10.ecse321.com.riderzpassengers.constants.HTTP;
import riderz.team10.ecse321.com.riderzpassengers.constants.TAG;
import riderz.team10.ecse321.com.riderzpassengers.constants.URL;
import riderz.team10.ecse321.com.riderzpassengers.http.HttpRequestClient;

public class ModifyContactInfo extends AppCompatActivityBack implements HttpRequestClient {
    // Used to get and set values into phone EditText
    protected EditText phone;

    // Used to display error messages
    protected TextView errorMsg;

    // User credentials to uniquely identify an user
    protected String username;

    // Used to transmit information from synchronous HTTP requests
    protected boolean success;
    protected String msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_contact_info);

        // Map Java to XML
        phone = findViewById(R.id.updatePhoneNumber);

        // Hide error message for now
        errorMsg = findViewById(R.id.updatePhoneError);
        errorMsg.setVisibility(View.INVISIBLE);

        // Obtain username from intent
        username = getIntent().getStringExtra("username");

        // Perform Asynchronous request to obtain phone number
        asyncHttpRequest();
        mapButtons();
    }

    /**
     * Asynchronously obtains the user's phone number and inserts it into the EditText.
     */
    private void asyncHttpRequest() {
        // Add parameters
        final RequestParams params = new RequestParams();
        params.add("username", username);

        // Execute asynchronous http request on another thread other than main
        new Thread(new Runnable() {
            @Override
            public void run() {
                final String getPhoneUrl = URL.baseUrl + URL.userUrl + "getPhone";
                AsyncHttpClient client = new AsyncHttpClient();
                client.setTimeout(HTTP.maxTimeout);
                client.get(getPhoneUrl, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        // Check that we obtained a valid response from the server
                        if (responseBody.length == 0) {
                            // Response is invalid
                            Log.e(TAG.changePhoneTag, "Failed to fetch phone");
                            success = false;
                            msg = "Failed to fetch phone number";
                        } else {
                            // Response is valid
                            msg = "";
                            phone.setText(new String(responseBody));
                        }
                        // Display error message
                        errorMsg.setText(msg);
                        errorMsg.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.e(TAG.changePhoneTag, "Server error - Failed to contact server");
                        errorMsg.setText("Failed to contact server");
                        errorMsg.setVisibility(View.VISIBLE);
                    }
                });
            }
        }).run();
    }

    @Override
    public void mapButtons() {
        findViewById(R.id.updatePhoneButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Verify that the phone number is valid before proceeding with updating the phone number
                if (RegexVerification.verifyPhone(phone.getText().toString())) {
                    syncHttpRequest();
                    // If the request was successful, terminate current activity
                    if (success) {
                        success = false;
                        msg = "";
                        finish();
                    }
                    // Display error message
                    errorMsg.setText(msg);
                    errorMsg.setVisibility(View.VISIBLE);
                } else {
                    errorMsg.setText("Phone number is invalid");
                    errorMsg.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void syncHttpRequest() {
        // Add parameters
        final RequestParams params = new RequestParams();
        params.add("username", username);
        params.add("phone", phone.getText().toString());

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                final String setPhoneUrl = URL.baseUrl + URL.userUrl + "setPhone";
                SyncHttpClient client = new SyncHttpClient();
                client.setTimeout(HTTP.maxTimeout);
                client.put(setPhoneUrl, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        // Check that we obtained a valid response from the server
                        if (new Boolean(new String(responseBody))) {
                            success = true;
                            msg = "";
                        } else {
                            Log.e(TAG.changePhoneTag, "Response from server: " + new String(responseBody));
                            success = false;
                            msg = "Failed to update phone number";
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.e(TAG.changePhoneTag, "Server error - Failed to contact server");
                        success = false;
                        msg = "Could not contact server";
                    }
                });
            }
        });
        t.start();

        // Await until thread has finished execution before proceeding
        try {
            t.join();
        } catch (InterruptedException e) {
            Log.e(TAG.changePhoneTag, "Thread error in changing phone thread");
        }
    }
}
