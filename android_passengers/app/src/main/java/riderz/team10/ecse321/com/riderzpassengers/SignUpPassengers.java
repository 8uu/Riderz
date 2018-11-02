package riderz.team10.ecse321.com.riderzpassengers;

import android.support.v7.app.AppCompatActivity;
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
import riderz.team10.ecse321.com.riderzpassengers.assets.verificator.RegexVerification;
import riderz.team10.ecse321.com.riderzpassengers.constants.HTTP;
import riderz.team10.ecse321.com.riderzpassengers.constants.URL;
import riderz.team10.ecse321.com.riderzpassengers.http.HttpRequestClient;

public class SignUpPassengers extends AppCompatActivity implements HttpRequestClient {
    // Used to display error messages
    protected TextView errorMsg;

    // Used to handle synchronous requests
    protected boolean success;
    protected String msg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_passengers);

        // Map error to the element
        errorMsg = findViewById(R.id.signUpError);

        mapButtons();
    }

    @Override
    public void mapButtons(){
        Button signUpButton = findViewById(R.id.signUpButton);
        signUpButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v){
                syncHttpRequest();
                if(success){
                    msg = "";
                    success = false;
                    finish();
                }
                errorMsg.setText(msg);
                errorMsg.setVisibility(View.VISIBLE);
            }
        });
    }

    public String getPlainText(int idName){
        EditText text = findViewById(idName);
        return text.getText().toString();
    }

    @Override
    public void syncHttpRequest (){
        final String userSignUpUrl = URL.baseUrl + URL.userUrl + "addUser/";

        final String firstName = getPlainText(R.id.signUpFirstName);
        final String lastName = getPlainText(R.id.signUpLastName);
        final String username = getPlainText(R.id.signUpUsername);
        final String email = getPlainText(R.id.signUpEmail);
        final String password = getPlainText(R.id.signUpPassword);
        final String confirmPassword = getPlainText(R.id.signUpVerifyPassword);
        final String phone = getPlainText(R.id.signUpPhoneNumber);

        // Verify that passwords match
        if(!password.equals(confirmPassword)) {
            msg = "Passwords do not match";
            success = false;
            return;
        }

        // Verify that the phone number is valid
        if(!RegexVerification.verifyPhone(phone)) {
            msg = "Phone number is invalid";
            success = false;
            return;
        }

        // Verify that the email is valid
        if (!RegexVerification.verifyEmail(email)) {
            msg = "Email is invalid";
            success = false;
            return;
        }

        // Verify that first name and last name are valid
        if (!RegexVerification.isAlphabetical(firstName) ||
            !RegexVerification.isAlphabetical(lastName)) {
            msg = "Name is invalid";
            success = false;
            return;
        }

        // Adding request parameters
        final RequestParams userParams = new RequestParams();
        userParams.add("username", username );
        userParams.add("email", email);
        userParams.add("firstName",firstName);
        userParams.add("lastName",lastName);
        userParams.add("password",password);
        userParams.add("phone", phone);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                SyncHttpClient client = new SyncHttpClient();
                client.setTimeout(HTTP.maxTimeout);
                client.post(userSignUpUrl, userParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        if (responseBody.length != 0) {
                            success = true;
                            msg = "";
                        } else {
                            // No valid response from server then username is already taken
                            success = false;
                            msg = "Username is already taken";
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        success = false;
                        msg = "Failed to contact server";
                        Log.e("Debug","Sign Up User process to create user failed");
                    }
                });



            }
        });
        t.start();

        try {
            t.join();
        } catch (InterruptedException e) {
            Log.e("Debug", "Thread exception in login thread");
        }
    }
}
