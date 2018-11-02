package riderz.team10.ecse321.com.riderzpassengers.settings;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import cz.msebera.android.httpclient.Header;
import riderz.team10.ecse321.com.riderzpassengers.R;
import riderz.team10.ecse321.com.riderzpassengers.assets.templates.activity.AppCompatActivityBack;
import riderz.team10.ecse321.com.riderzpassengers.constants.HTTP;
import riderz.team10.ecse321.com.riderzpassengers.constants.TAG;
import riderz.team10.ecse321.com.riderzpassengers.constants.URL;
import riderz.team10.ecse321.com.riderzpassengers.http.HttpRequestClient;

public class ResetPassword extends AppCompatActivityBack implements HttpRequestClient {
    // resetError is to be accessed within nested functions, required to give global access
    protected TextView errorMsg;

    // Used for transmit information from synchronous HTTP responses from the login REST API
    protected boolean success;
    protected String msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Hide error message for now
        errorMsg = findViewById(R.id.resetError);
        errorMsg.setVisibility(View.INVISIBLE);

        // Map buttons
        mapButtons();
    }

    @Override
    public void mapButtons() {
        Button resetPasswordButton = findViewById(R.id.resetPasswordButton);
        resetPasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncHttpRequest();
                if (success) {
                    success = false;
                    msg = "";
                    finish();
                }
                // Display error message
                errorMsg.setText(msg);
                errorMsg.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void syncHttpRequest() {
        // URL to target
        final String resetUrl = URL.baseUrl + URL.userUrl + "setPassword";

        // Obtains values from EditText elements
        final EditText newPass = findViewById(R.id.resetNewPassword);
        final EditText confirmPass = findViewById(R.id.resetConfirmNewPassword);
        final String newPassText = newPass.getText().toString();
        final String confirmPassText = confirmPass.getText().toString();
        final String usernameText = getIntent().getStringExtra("username");

        // Verifies that both passwords match each other
        if (!newPassText.equals(confirmPassText)) {
            success = false;
            msg = "Passwords do not match";
            Log.e(TAG.resetTag, "Passwords do not match");
            return;
        }

        // Add parameters to request
        final RequestParams params = new RequestParams();
        params.add("username", usernameText);
        params.add("newPassword", confirmPassText);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // Instantiate new synchronous http client, set timeout and perform put request
                SyncHttpClient client = new SyncHttpClient();
                client.setTimeout(HTTP.maxTimeout);
                client.put(resetUrl, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String responseString = new String(responseBody);
                        if (new Boolean(responseString)) {
                            success = true;
                            msg = "";
                        } else {
                            success = false;
                            msg = "Failed to reset password";
                            Log.e(TAG.resetTag, "Failed to reset password for user: " +
                                    usernameText + " password: " + newPassText);
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          byte[] responseBody, Throwable error) {
                        success = false;
                        msg = "Could not contact server";
                        Log.e(TAG.resetTag, "Server error - failed to reset password");
                    }
                });
            }
        });
        t.start();

        // Await until thread t has finished its execution before proceeding
        try {
            t.join();
        } catch (InterruptedException e) {
            Log.e(TAG.resetTag, "Thread exception in reset thread");
        }
    }
}
