package riderz.team10.ecse321.com.riderzdrivers;

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
import riderz.team10.ecse321.com.riderzdrivers.assets.template.activity.AppCompatActivityBack;
import riderz.team10.ecse321.com.riderzdrivers.assets.verificator.RegexVerification;
import riderz.team10.ecse321.com.riderzdrivers.constants.HTTP;
import riderz.team10.ecse321.com.riderzdrivers.constants.TAG;
import riderz.team10.ecse321.com.riderzdrivers.constants.URL;
import riderz.team10.ecse321.com.riderzdrivers.http.HttpRequestClient;



public class SignUpDriver extends AppCompatActivityBack implements HttpRequestClient {
    // Error message
    protected TextView errorMsg;

    protected boolean success;
    protected String msg;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        // Map error message display
        errorMsg = findViewById(R.id.signUpError);

        mapButtons();
    }

    @Override
    public void mapButtons(){
        Button signUpButton = findViewById(R.id.signUpButton);

        signUpButton.setOnClickListener(new View.OnClickListener() {

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
    public void syncHttpRequest(){
        // RESTful APIs to target
        final String userSignUpUrl = URL.baseUrl + URL.userUrl + "addUser/";
        final String driverSignUpUrl = URL.baseUrl+URL.driverUrl + "new/";
        final String carSignUpUrl = URL.baseUrl+URL.carUrl;

        //User and Driver parameters
        final String firstName = getPlainText(R.id.signUpFirstName);
        final String lastName = getPlainText(R.id.signUpLastName);
        final String username = getPlainText(R.id.signUpUsername);
        final String email = getPlainText(R.id.signUpEmail);
        final String password = getPlainText(R.id.signUpPassword);
        final String verifyPassword = getPlainText(R.id.signUpVerifyPassword);
        final String phone = getPlainText(R.id.signUpPhoneNumber);

        // Verify that passwords match
        if (!password.equals(verifyPassword)) {
            msg = "Passwords do not match";
            success = false;
            return;
        }

        // Verify that name is valid
        if (!RegexVerification.isAlphabetical(firstName) ||
            !RegexVerification.isAlphabetical(lastName)) {
            msg = "Name is invalid";
            success = false;
            return;
        }

        // Verify that email is valid
        if (!RegexVerification.verifyEmail(email)) {
            msg = "Email is invalid";
            success = false;
            return;
        }

        // Verify that the phone number is valid
        if (!RegexVerification.verifyPhone(phone)) {
            msg = "Phone is invalid";
            success = false;
            return;
        }

        //Car Parameters
        final String carMake= getPlainText(R.id.signUpCarMake);
        final String carModel= getPlainText(R.id.signUpCarModel);
        final String carYear= getPlainText(R.id.signUpCarYear);
        final String carSeats = getPlainText(R.id.signUpCarSeats);
        final String fuelEfficiency= getPlainText(R.id.signUpFuelEffiency);
        final String licensePlate = getPlainText(R.id.signUpLicensePlate);

        // Verify that the make of the car is alphabets
        if (!RegexVerification.isAlphabetical(carMake)) {
            msg = "Make has to be alphabetical";
            success = false;
            return;
        }

        //Driver signup requests create a user, driver and car in DB
        final RequestParams userParams = new RequestParams();
        final RequestParams driverParams = new RequestParams();
        final RequestParams carParams = new RequestParams();

        //Adding the user parameters to create a new user
        userParams.add("username", username );
        userParams.add("email", email);
        userParams.add("firstName",firstName);
        userParams.add("lastName",lastName);
        userParams.add("password",password);
        userParams.add("phone", phone);

        //Linking the driver profile to the a user via the username
        driverParams.add("operator", username);

        //Adding car parameters and linking car with the driver via username
        carParams.add("operator",username);
        carParams.add("make",carMake);
        carParams.add("model",carModel);
        carParams.add("year",carYear);
        carParams.add("numberOfSeats",carSeats);
        carParams.add("fuelEfficiency",fuelEfficiency);
        carParams.add("licensePlate",licensePlate);

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                SyncHttpClient client = new SyncHttpClient();
                client.setTimeout(HTTP.maxTimeout);

                client.post(userSignUpUrl, userParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        if (responseBody.length != 0) {
                            // Created user
                            Log.d(TAG.signUpTag, "Successful creation of user in Driver Sign Up");
                            success = true;
                            msg = "";
                        } else {
                            // Username is already taken
                            success = false;
                            msg = "Username is already taken";
                            return;
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        // Failed to contact server
                        Log.e(TAG.signUpTag,"Sign Up driver process to create user failed");
                        success = false;
                        msg = "Failed to contact server";
                        return;
                    }
                });


                client.put(driverSignUpUrl, driverParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        if (responseBody.length != 0) {
                            // Created driver
                            Log.d(TAG.signUpTag, "Successful creation of driver in Driver Sign Up");
                            success = true;
                            msg = "";
                        } else {
                            // Failed to insert to SQL database
                            success = false;
                            msg = "Failed to sign up user";
                            return;
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        // No response from server
                        Log.e("Debug","Sign Up driver process to create driver failed");
                        success = false;
                        msg = "Failed to contact server";
                        return;
                    }
                });

                client.post(carSignUpUrl, carParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        if (responseBody.length != 0) {
                            // Driver successfully created
                            Log.d(TAG.signUpTag,"Successful creation of car in Driver Sign Up");
                            success = true;
                            msg = "";
                        } else {
                            success = false;
                            msg = "Failed to sign up user";
                            return;
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.d(TAG.signUpTag,"Sign Up driver process to create car failed");
                        success = false;
                        msg = "Failed to contact server";
                        return;
                    }
                });
            }
        });

        // Block further execution until current thread is finished
        t.start();
        try {
            t.join();
        } catch (InterruptedException e) {
            Log.e("Debug", "Thread exception in login thread");
        }


    }



}
