package riderz.team10.ecse321.com.riderzpassengers;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.loopj.android.http.*;
import cz.msebera.android.httpclient.Header;
import riderz.team10.ecse321.com.riderzpassengers.constants.HTTP;
import riderz.team10.ecse321.com.riderzpassengers.constants.TAG;
import riderz.team10.ecse321.com.riderzpassengers.constants.URL;
import riderz.team10.ecse321.com.riderzpassengers.http.HttpRequestClient;
import riderz.team10.ecse321.com.riderzpassengers.navigation.MainNavigation;
import riderz.team10.ecse321.com.riderzpassengers.settings.ResetPasswordVerification;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Main activity. Performs login functionalities.
 */
public class RiderzPassengers extends AppCompatActivity implements HttpRequestClient {
    // loginError is to be accessed within nested functions, required to give global access
    protected TextView errorMsg;
    protected EditText username;
    protected EditText password;

    // Used for transmit information from synchronous HTTP requests to the login REST API
    protected boolean success;
    protected String msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riderz_passengers);


        // Map TextView and EditText components
        errorMsg = findViewById(R.id.loginError);
        username = findViewById(R.id.loginUsername);
        password = findViewById(R.id.loginPassword);

        // Hide error message for now
        errorMsg.setVisibility(View.INVISIBLE);

        // Obtain mapping to each of the buttons
        mapButtons();
    }

    @Override
    public void mapButtons() {
        Button resetButton = findViewById(R.id.forgotPasswordButton);
        Button loginButton = findViewById(R.id.loginButton);
        Button registerButton = findViewById(R.id.loginRegisterButton);

        // Add event listener for reset button
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch reset password verification activity when on click event gets triggered
                Intent intent = new Intent(RiderzPassengers.this,
                        ResetPasswordVerification.class);
                RiderzPassengers.this.startActivity(intent);
            }
        });

        // Add event listener for login button
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                syncHttpRequest();
                if (success) {
                    // TODO: Link to main ListView
                    // TODO: Add login credentials to cache
                    // Clear error message
                    msg = "";
                    success = false;
                    File file = new File(getApplicationContext().getFilesDir(), "login.txt");
                    try {
                        // Create a new file and write login credentials
                        if (!file.exists()) {
                            file.createNewFile();
                        }
                        // Clears the file
                        PrintWriter writer = new PrintWriter(file);
                        writer.close();

                        // Reinstantiate a PrintWriter object and write credentials into the file
                        writer = new PrintWriter(file);
                        writer.println(username.getText().toString());
                        writer.println(password.getText().toString());
                        writer.close();
                    } catch (IOException e) {
                        Log.e(TAG.loginTag, "Failed to create/modify file");
                    }

                    // Kill off this activity and start new activity
                    // Take advantage of Android's asynchronous processing on finish();
                    Intent intent = new Intent(RiderzPassengers.this, MainNavigation.class);
                    finish();
                    startActivity(intent);
                }
                // Display error message
                errorMsg.setText(msg);
                errorMsg.setVisibility(View.VISIBLE);
            }
        });

        // Add event listener for register button
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: Complete
                Intent intent = new Intent(RiderzPassengers.this,
                        SignUpPassengers.class);
                RiderzPassengers.this.startActivity(intent);

            }
        });
    }

    @Override
    public void syncHttpRequest() {
        // URL to target
        final String loginUrl = URL.baseUrl + URL.loginUrl;

        // Obtains values from EditText elements
        final String usernameText = username.getText().toString();
        final String passwordText = password.getText().toString();

        // Instantiate RequestParams object
        // Add username and password strings
        final RequestParams params = new RequestParams();
        params.add("username", usernameText);
        params.add("password", passwordText);

        // Instantiate a new Runnable object which will handle http requests asynchronously
        // Then await until thread is finished to make the request synchronous.
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // Instantiate new synchronous http client, set timeout and perform post request
                SyncHttpClient client = new SyncHttpClient();
                client.setTimeout(HTTP.maxTimeout);
                client.post(loginUrl, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String responseString = new String(responseBody);
                        switch (responseString) {
                            case "User session has been established!":
                                Log.d(TAG.loginTag, "User: " + usernameText + " successfully authenticated");
                                success = true;
                                break;
                            default:
                                Log.d(TAG.loginTag, "User: " + usernameText + " failed to authenticate");
                                success = false;
                                msg = "This combination does not exist!";
                                break;
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          byte[] responseBody, Throwable error) {
                        success = false;
                        msg = "Could not contact server\n Please try again in a minute";
                        Log.e(TAG.loginTag, "Server error - could not contact server");
                    }
                });
            }
        });
        t.start();

        // Await until thread t has finished its execution before proceeding
        try {
            t.join();
        } catch (InterruptedException e) {
            Log.e(TAG.loginTag, "Thread exception in login thread");
        }
    }

    @Override
    public void onBackPressed() {
        // Do nothing back button is disabled for this screen
        Toast.makeText(getApplicationContext(), "Back button is disabled", Toast.LENGTH_SHORT).show();
    }
}
