package riderz.team10.ecse321.com.riderzdrivers;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Timestamp;

import cz.msebera.android.httpclient.Header;
import riderz.team10.ecse321.com.riderzdrivers.constants.HTTP;
import riderz.team10.ecse321.com.riderzdrivers.constants.TAG;
import riderz.team10.ecse321.com.riderzdrivers.constants.URL;
import riderz.team10.ecse321.com.riderzdrivers.http.HttpRequestClient;
import riderz.team10.ecse321.com.riderzdrivers.navigation.MainNavigation;

public class DriverTripForm extends AppCompatActivity implements HttpRequestClient {


    private JSONObject jsonCreateRoute;
    private JSONObject jsonRouteLast;
    private JSONObject jsonTrip;
    private String username;
    protected String startingAddress;
    protected String startingLatitude;
    protected String startingLongitude;
    protected String destinationAddress;
    protected String destinationLatitude;
    protected String destinationLongitude;
    protected String duration;
    protected double price;

    //to do link the main page to this page
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_trip_form);

        // TODO UNCOMMENT *******************************************************
        //username = getIntent().getStringExtra("username");
        username = getIntent().getStringExtra("username");
        startingAddress = getIntent().getStringExtra("startingAddress");
        startingLatitude = getIntent().getStringExtra("startingLatitude");
        startingLongitude = getIntent().getStringExtra("startingLongitude");
        destinationAddress = getIntent().getStringExtra("destinationAddress");
        destinationLatitude = getIntent().getStringExtra("destinationLatitude");
        destinationLongitude = getIntent().getStringExtra("destinationLongitude");
        duration = getIntent().getStringExtra("tripTime");
        price = Double.parseDouble(getIntent().getStringExtra("price"));
        mapButtons();
    }

    public void mapButtons() {

        Log.e("TAGGGGG", "ABC");
        Button newTrip = findViewById(R.id.new_trip_button);

//        Bundle bundle = this.getIntent().getExtras();
//        final String startLongitude = bundle.getString("startLongitude");
//        final String startLatitude = bundle.getString("startLatitude");
//        final String endLongitude = bundle.getString("endLongitude");
//        final String endLatitude = bundle.getString("endLatitude");
//        final String timeDifference = bundle.getString("timeDifference");

        // Add event listener for register button
        newTrip.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final EditText startTime   = (EditText) findViewById(R.id.starttime_form);
                final EditText startDate   = (EditText) findViewById(R.id.startdate_form);
                final EditText numberSeats   = (EditText) findViewById(R.id.numberseats_form);
                //add check if the fields are empty, return a warning

//                String startMoment = "2017-08-10 05:25:00";
                String startMoment = startDate.getText().toString() + " " + startTime.getText().toString();

//                long end = (Timestamp.valueOf("2017-08-10 05:25:00")).getTime() + (new Long(12321412)).longValue();

                long end = (Timestamp.valueOf(startMoment)).getTime() +
                           (new Long(duration)).longValue();


                Timestamp ts =  new Timestamp(end);
//                ts.setTime(end);
                String endTimeString = ts.toString();
//                int index = endTimeString.indexOf(".");
//                endTimeString = endTimeString.substring(0, index);

                syncHttpRequestCreate();
                syncHttpRequestLastTrip();
                String tripID = null;
                Log.e(TAG.driverTripFormTag, jsonRouteLast.toString());
                try {
                    tripID = jsonRouteLast.getString("tripID");
                    Log.e(TAG.driverTripFormTag, "" + tripID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                //i receive startLong, startLat, endLong, endLat, time difference(ms)
                //the start time is taken from driver

                syncHttpRequestCreateRoute(tripID, startingLongitude, startingLatitude, startMoment,
                        destinationLongitude, destinationLatitude, endTimeString, numberSeats.getText().toString());
            }
        });
    }

    @Override
    public void syncHttpRequest(){

    }

    //create the trip
    public void syncHttpRequestCreate(){
        final RequestParams params = new RequestParams();
        params.add("operator", username);

        // Instantiate a new Runnable object which will handle http requests asynchronously
        // Then await until thread is finished to make the request synchronous.
        Thread t = new Thread(new Runnable() {
            // URL to target
            final String tripUrl = URL.baseUrl + "trip/insertTrip";

            @Override
            public void run() {
                // Instantiate new synchronous http client, set timeout and perform get request
                // Uses extended timeout
                SyncHttpClient client = new SyncHttpClient();
                client.setTimeout(HTTP.maxTimeoutExtended);
                client.put(tripUrl, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        if (new Boolean(new String(responseBody))) {
                            Log.e(TAG.driverTripFormTag, "Created trip");
                            Intent intent = new Intent(DriverTripForm.this, MainNavigation.class);
                            DriverTripForm.this.startActivity(intent);
                        } else {
                            Log.e(TAG.driverTripFormTag, "Failed to create trip");
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          byte[] responseBody, Throwable error) {
                        Log.e(TAG.driverTripFormTag, "Server error - could not contact server Create");
                    }
                });
            }
        });
        t.start();

        // Await until thread t has finished its execution before proceeding
        try {
            t.join();
        } catch (InterruptedException e) {
            Log.e(TAG.driverTripFormTag, "Thread exception in login thread");
        }
    }

    public void syncHttpRequestLastTrip(){
        final RequestParams params = new RequestParams();
        params.add("operator", username);

        // Instantiate a new Runnable object which will handle http requests asynchronously
        // Then await until thread is finished to make the request synchronous.
        Thread t = new Thread(new Runnable() {
            // URL to target
            final String tripUrl = URL.baseUrl + "trip/last";

            @Override
            public void run() {
                // Instantiate new synchronous http client, set timeout and perform get request
                // Uses extended timeout
                SyncHttpClient client = new SyncHttpClient();
                client.setTimeout(HTTP.maxTimeoutExtended);
                client.get(tripUrl, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        try {
                            jsonRouteLast = new JSONObject(new String(responseBody));
                        } catch (JSONException e) {
                            Log.e(TAG.driverTripFormTag, "Failed to parse JSON from HTTP response Last");
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          byte[] responseBody, Throwable error) {
                        Log.e(TAG.driverTripFormTag, new String(responseBody));
                        Log.e(TAG.driverTripFormTag, "Server error - could not contact server Last");
                    }
                });
            }
        });
        t.start();

        // Await until thread t has finished its execution before proceeding
        try {
            t.join();
        } catch (InterruptedException e) {
            Log.e(TAG.driverTripFormTag, "Thread exception in login thread");
        }

    }

    public void syncHttpRequestCreateRoute(final String tripID, final String startLongitude, final String startLatitude, final String startTime,
                                           final String endLongitude, final String endLatitude, final String endTime, final String seatsLeft) {

        // Instantiate a new Runnable object which will handle http requests asynchronously
        // Then await until thread is finished to make the request synchronous.
        Thread t = new Thread(new Runnable() {
            // URL to target
            String routeUrl = URL.baseUrl + "insertItinerary/" + tripID + "/" + startLongitude + "/"
                    + startLatitude + "/" + startTime + ".000/" + endLongitude + "/" + endLatitude + "/"
                    + endTime + "/" + seatsLeft + "/" + username;

            @Override
            public void run() {
                routeUrl = routeUrl.replaceAll("\\s", "%20");
                Log.e(TAG.driverTripFormTag, routeUrl);
                // Instantiate new synchronous http client, set timeout and perform get request
                // Uses extended timeout
                SyncHttpClient client = new SyncHttpClient();
                client.setTimeout(HTTP.maxTimeoutExtended);
                client.post(routeUrl, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        try {
                            jsonTrip = new JSONObject(new String(responseBody));
                            Log.e(TAG.driverTripFormTag, new String(responseBody));
                        } catch (JSONException e) {
                            Log.e(TAG.driverTripFormTag, "Failed to parse JSON from HTTP response Route");
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          byte[] responseBody, Throwable error) {
                        Log.e(TAG.driverTripFormTag, "Server error - could not contact server Route");
                    }
                });
            }
        });
        t.start();

        // Await until thread t has finished its execution before proceeding
        try {
            t.join();
        } catch (InterruptedException e) {
            Log.e(TAG.driverTripFormTag, "Thread exception in login thread");
        }
    }
}
