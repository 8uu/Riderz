package riderz.team10.ecse321.com.riderzpassengers.reservation;

import android.content.Context;
import android.location.Address;
import android.support.design.circularreveal.CircularRevealWidget;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;
import riderz.team10.ecse321.com.riderzpassengers.R;
import riderz.team10.ecse321.com.riderzpassengers.assets.convertor.Display;
import riderz.team10.ecse321.com.riderzpassengers.assets.convertor.SQLCompliance;
import riderz.team10.ecse321.com.riderzpassengers.assets.geolocation.ReverseGeocoding;
import riderz.team10.ecse321.com.riderzpassengers.assets.templates.activity.AppCompatActivityBack;
import riderz.team10.ecse321.com.riderzpassengers.constants.HTTP;
import riderz.team10.ecse321.com.riderzpassengers.constants.TAG;
import riderz.team10.ecse321.com.riderzpassengers.constants.URL;
import riderz.team10.ecse321.com.riderzpassengers.http.HttpRequestClient;

public class Reservation extends AppCompatActivityBack implements HttpRequestClient {

    // Contains a list of generated IDs for TextViews
    final private ArrayList<Integer> generatedId = new ArrayList<>();
    final private ArrayList<Integer> validReservationsTripID = new ArrayList<>();

    // Provides access
    private String username;

    // Current trips
    private JSONArray json;

    // HashMap containing the id to tripID
    private HashMap<Integer, Integer> hash = new HashMap<>();

    // Used to handle sync HTTP requests
    private boolean success = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reservation);

        // Obtains the username from intent
        username = getIntent().getStringExtra("username");

        populateLinearLayout();
    }

    /**
     * Populate linear layout with programmatically generated TextViews
     */
    private void populateLinearLayout() {
        syncHttpRequest();
        final LinearLayout layout = findViewById(R.id.reservationLayout);
        createTextViews(layout);
    }

    /**
     * Creates new TextView components and inserts them into a specific layout.
     * @param layout LinearLayout which will contain the TextView
     */
    private void createTextViews(final LinearLayout layout) {
        try {
            final Context context = getApplicationContext();
            if (json != null) {
                for (int i = 0; i < json.length(); i++) {
                    final int index = i;
                    JSONObject object = json.getJSONObject(i);
                    // Keep tripID immutable
                    final int tripID = object.getInt("tripID");

                    fetchAndDisplayReservations(layout, context, index, tripID, true);
                }
            }
            // No reservations available to be seen
            if (validReservationsTripID.size() == 0) {
                TextView tv = findViewById(R.id.reservationNone);
                tv.setText("You have no reservations to view!");

                // Convert padding from dp to px and set padding
                int padding_in_px = Display.dpToPX(getResources(), 16);
                tv.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);

                // Set font size to 16dp
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                tv.setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            Log.e(TAG.viewReservationTag, "Failed to parse JSON array");
        }
    }

    /**
     * Performs an asynchronous request to fetch reservation information.
     * @param layout
     * @param context
     * @param index
     * @param tripID
     * @param retry
     */
    private void fetchAndDisplayReservations(final LinearLayout layout,
                                             final Context context,
                                             final int index,
                                             final int tripID,
                                             final boolean retry) {
        new Thread(new Runnable() {
            final String itineraryUrl = URL.baseUrl + "getItineraryByTripID/" +
                    tripID + "/" + username;

            @Override
            public void run() {
                // Instantiate new asynchronous http client, set timeout and perform get request
                // Uses extended timeout
                AsyncHttpClient client = new AsyncHttpClient();
                client.setTimeout(HTTP.maxTimeoutExtended);
                client.get(itineraryUrl, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        try {
                            Log.e(TAG.viewReservationTag, new String(responseBody));
                            JSONObject obj = new JSONObject(new String(responseBody));
                            double startingLongitude = obj.getDouble("startingLongitude");
                            double startingLatitude = obj.getDouble("startingLatitude");
                            double endingLongitude = obj.getDouble("endingLongitude");
                            double endingLatitude = obj.getDouble("endingLatitude");

                            String startingTime = SQLCompliance.convertToSQLTimestamp(obj.getString("startingTime"));
                            String endingTime = SQLCompliance.convertToSQLTimestamp(obj.getString("endingTime"));

                            Timestamp now = new Timestamp(System.currentTimeMillis());
                            if (Timestamp.valueOf(startingTime).before(now)) {
                                return;
                            }

                            String text = "";

                            // Perform reverse geocoding to get starting and ending addresses
                            String startingAddress = ReverseGeocoding.safeAddressToString(
                                    ReverseGeocoding.reverseLookup(context, startingLatitude, startingLongitude));
                            String endingAddress = ReverseGeocoding.safeAddressToString(
                                    ReverseGeocoding.reverseLookup(context, endingLatitude, endingLongitude));

                            text += "Trips Left: " + tripID + "\n";
                            text += "Starting Time: " + startingTime + "\n";
                            text += "Ending Time: " + endingTime + "\n";
                            text += "Starting Address: " + startingAddress + "\n";
                            text += "Ending Address: " + endingAddress;

                            // Generate a new unique View ID
                            int id = View.generateViewId();
                            generatedId.add(id);

                            // Instantiate a new TextView, set text and id
                            TextView tv = new TextView(context);
                            tv.setText(text);
                            tv.setId(id);

                            // Convert padding from dp to px and set padding
                            int padding_in_px = Display.dpToPX(getResources(),16);
                            tv.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);

                            // Set font size to 16dp
                            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);

                            // Add TextView to layout
                            layout.addView(tv);

                            // Insert trip as a valid modifiable trip
                            validReservationsTripID.add(index);

                            // Remove the no reservation found textview
                            findViewById(R.id.reservationNone).setVisibility(View.GONE);

                            hash.put(id, tripID);
                            customMapButton(id);
                        } catch (Exception e) {
                            Log.e(TAG.viewReservationTag, e.getLocalizedMessage());
                            // Try again if it fails
                            if (retry) {
                                fetchAndDisplayReservations(layout, context, index, tripID, false);
                            }
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          byte[] responseBody, Throwable error) {
                        Log.e(TAG.viewReservationTag, "Server error - could not contact server");
                        // Try again if it fails
                        if (retry) {
                            fetchAndDisplayReservations(layout, context, index, tripID, false);
                        }
                    }
                });
            }
        }).run();
    }

    @Override
    public void mapButtons() { /* Not used in this activity */}

    private void customMapButton(int id) {
        findViewById(id).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (hash.containsKey(view.getId())) {
                    int tripID = hash.get(view.getId());
                    syncHttpRemoveReservation(tripID);
                    // Reservation was successfully removed
                    if (success) {
                        success = false;
                        Toast.makeText(getApplicationContext(), "Removed reservation", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "Could not remove reservation", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void syncHttpRequest() {
        // Instantiate a new Runnable object which will handle http requests asynchronously
        // Then await until thread is finished to make the request synchronous.
        Thread t = new Thread(new Runnable() {
            // URL to target
            final String tripUrl = URL.baseUrl + "getReservationByUsername/" + username;

            @Override
            public void run() {
                // Instantiate new synchronous http client, set timeout and perform get request
                // Uses extended timeout
                SyncHttpClient client = new SyncHttpClient();
                client.setTimeout(HTTP.maxTimeoutExtended);
                client.get(tripUrl, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        try {
                            json = new JSONArray(new String(responseBody));
                        } catch (JSONException e) {
                            Log.e(TAG.viewReservationTag, "Failed to parse JSON from HTTP response");
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          byte[] responseBody, Throwable error) {
                        Log.e(TAG.viewReservationTag, "Server error - could not contact server");
                    }
                });
            }
        });
        t.start();

        // Await until thread t has finished its execution before proceeding
        try {
            t.join();
        } catch (InterruptedException e) {
            Log.e(TAG.viewReservationTag, "Thread exception in login thread");
        }
    }

    private void syncHttpRemoveReservation(final int tripID) {
        // Instantiate a new Runnable object which will handle http requests asynchronously
        // Then await until thread is finished to make the request synchronous.
        Thread t = new Thread(new Runnable() {
            // URL to target
            final String deleteReservation = URL.baseUrl + "deleteReservation/" + username + "/" + tripID;
            final String incrementSeats = URL.baseUrl + "incrementSeatsLeft/" + tripID + "/" + username;

            @Override
            public void run() {
                // Instantiate new synchronous http client, set timeout and perform get request
                // Uses extended timeout
                SyncHttpClient client = new SyncHttpClient();
                client.setTimeout(HTTP.maxTimeoutExtended);
                client.delete(deleteReservation, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        Log.d(TAG.viewReservationTag, "Reservation deleted");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          byte[] responseBody, Throwable error) {
                        Log.e(TAG.viewReservationTag, "Server error - could not contact server");
                        success = false;
                        return;
                    }
                });

                client.put(incrementSeats, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        success = true;
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        Log.e(TAG.viewReservationTag, "Server error - could not contact server");
                        success = false;
                        return;
                    }
                });
            }
        });
        t.start();

        // Await until thread t has finished its execution before proceeding
        try {
            t.join();
        } catch (InterruptedException e) {
            Log.e(TAG.viewReservationTag, "Thread exception in login thread");
        }
    }
}
