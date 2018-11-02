package riderz.team10.ecse321.com.riderzdrivers;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import riderz.team10.ecse321.com.riderzdrivers.constants.HTTP;
import riderz.team10.ecse321.com.riderzdrivers.constants.TAG;
import riderz.team10.ecse321.com.riderzdrivers.constants.URL;
import riderz.team10.ecse321.com.riderzdrivers.http.HttpRequestClient;

public class DriverProfile extends AppCompatActivity implements HttpRequestClient {

    // Provides access in the url
    private String username;

    // Information concerning the driver
    private JSONObject jsonDriver;
    private JSONObject jsonCar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_profile);
        username = getIntent().getStringExtra("username");
        Log.e(TAG.driverProfileTag, username);
        TextView driverName = (TextView)findViewById(R.id.driverName);
        TextView driverRating = (TextView)findViewById(R.id.driverRating);
        TextView driverCarMake = (TextView)findViewById(R.id.driverCarMake);
        TextView driverCarModel = (TextView)findViewById(R.id.driverCarModel);
        TextView driverCarYear = (TextView)findViewById(R.id.driverCarYear);
        TextView driverCarSeats = (TextView)findViewById(R.id.driverCarSeats);
        TextView driverCarLicense = (TextView)findViewById(R.id.driverCarLicensePlate);
        mapButtons();

        syncHttpRequest();
        syncHttpRequestCar();
        try {
            driverName.setText("Users:\n" + username);
            driverRating.setText("Rating:\n" + jsonDriver.getString("rating"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            driverCarMake.setText("Make:\n" + jsonCar.getString("make"));
            driverCarModel.setText("Model:\n" + jsonCar.getString("model"));
            driverCarYear.setText("Year:\n" + jsonCar.getInt("year"));
            driverCarSeats.setText("Number of Seats:\n" + jsonCar.getString("numOfSeats"));
            driverCarLicense.setText("License Plate:\n" + jsonCar.getString("licensePlate"));
        } catch (JSONException e) {
            Log.e(TAG.driverProfileTag, e.getLocalizedMessage());
        }
//      http request to get info
    }

    @Override
    public void mapButtons() {
        Log.e(TAG.driverProfileTag, "CADFASFSAF");
        syncHttpRequest();
    }

    @Override
    public void syncHttpRequest() {
        Log.e(TAG.driverProfileTag, "Entered Sync");
        final RequestParams params = new RequestParams();
        params.add("operator", username);

        // Instantiate a new Runnable object which will handle http requests asynchronously
        // Then await until thread is finished to make the request synchronous.
        Thread t = new Thread(new Runnable() {
            // URL to target
            final String driverUrl = URL.baseUrl + "driver";

            @Override
            public void run() {
                // Instantiate new synchronous http client, set timeout and perform get request
                // Uses extended timeout
                SyncHttpClient client = new SyncHttpClient();
                client.setTimeout(HTTP.maxTimeoutExtended);
                client.get(driverUrl, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        try {
                            jsonDriver = new JSONObject(new String(responseBody));
                        } catch (JSONException e) {
                            Log.e(TAG.driverProfileTag, "Failed to parse JSON from HTTP response");
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          byte[] responseBody, Throwable error) {
                        Log.e(TAG.driverProfileTag, "Server error - could not contact server");
                    }
                });
            }
        });
        t.start();

        // Await until thread t has finished its execution before proceeding
        try {
            t.join();
        } catch (InterruptedException e) {
            Log.e(TAG.driverProfileTag, "Thread exception in login thread");
        }

    }

    public void syncHttpRequestCar() {
        final RequestParams params = new RequestParams();
        params.add("operator", username);

        // Instantiate a new Runnable object which will handle http requests asynchronously
        // Then await until thread is finished to make the request synchronous.
        Thread t = new Thread(new Runnable() {
            // URL to target
            final String carUrl = URL.baseUrl + "car";

            @Override
            public void run() {
                // Instantiate new synchronous http client, set timeout and perform get request
                // Uses extended timeout
                SyncHttpClient client = new SyncHttpClient();
                client.setTimeout(HTTP.maxTimeoutExtended);
                client.get(carUrl, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        try {
                            jsonCar = new JSONObject(new String(responseBody));
                        } catch (JSONException e) {
                            Log.e(TAG.driverProfileTag, "Failed to parse JSON from HTTP response");
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          byte[] responseBody, Throwable error) {
                        Log.e(TAG.driverProfileTag, "Server error - could not contact server");
                    }
                });
            }
        });
        t.start();

        // Await until thread t has finished its execution before proceeding
        try {
            t.join();
        } catch (InterruptedException e) {
            Log.e(TAG.driverProfileTag, "Thread exception in login thread");
        }

    }
}
