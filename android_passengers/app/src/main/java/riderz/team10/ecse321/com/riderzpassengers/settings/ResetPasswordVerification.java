package riderz.team10.ecse321.com.riderzpassengers.settings;

import android.content.Intent;
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

/**
 * Reset password verification activity. Verifies that user's email address and username match
 * whatever is provided within the database.
 */
public class ResetPasswordVerification extends AppCompatActivityBack implements HttpRequestClient {
    // verifyError is to be accessed within nested functions, required to give global access
    protected TextView errorMsg;

    // Used for synchronous calls to REST APIs
    protected boolean success;
    protected String msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password_verification);

        // Hide error message for now
        errorMsg = findViewById(R.id.verifyError);
        errorMsg.setVisibility(View.INVISIBLE);

        // Obtain mapping to each of the buttons
        mapButtons();
    }

    @Override
    public void mapButtons() {
        // Maps verifyReset button
        Button verifyButton = findViewById(R.id.verifyReset);
        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncHttpRequest();
                if (success) {
                    // Clear error message
                    msg = "";
                    success = false;
                    final EditText username = findViewById(R.id.resetUsername);
                    // Launch reset password verification activity when on click event gets triggered
                    Intent intent = new Intent(ResetPasswordVerification.this,
                            ResetPassword.class);
                    intent.putExtra("username", username.getText().toString());
                    ResetPasswordVerification.this.startActivity(intent);
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
        final String resetUrl = URL.baseUrl + URL.userUrl + "getEmail";

        // Obtains values from EditText elements
        final EditText username = findViewById(R.id.resetUsername);
        final EditText email = findViewById(R.id.resetEmail);
        final String usernameText = username.getText().toString();
        final String emailText = email.getText().toString();

        final RequestParams params = new RequestParams();
        params.add("username", usernameText);
        params.add("password", emailText);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // Instantiate new synchronous http client, set timeout and perform get request
                SyncHttpClient client = new SyncHttpClient();
                client.setTimeout(HTTP.maxTimeout);
                client.get(resetUrl, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        // Null was returned
                        if (responseBody.length == 0) {
                            success = false;
                            msg = "Combination does not exist";
                            return;
                        }
                        String responseString = new String(responseBody);
                        if (emailText.equals(responseString)) {
                            success = true;
                        } else {
                            success = false;
                            msg = "Combination does not exist";
                            return;
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          byte[] responseBody, Throwable error) {
                        success = false;
                        msg = "Could not contact server";
                        Log.e(TAG.verifyTag, "Server error - failed to get email");
                    }
                });
            }
        });
        t.start();

        // Await until thread t has finished its execution before proceeding
        try {
            t.join();
        } catch (InterruptedException e) {
            Log.e(TAG.verifyTag, "Thread exception in reset thread");
        }
    }
}

