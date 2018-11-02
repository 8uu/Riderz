package riderz.team10.ecse321.com.riderzpassengers.trip;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;
import riderz.team10.ecse321.com.riderzpassengers.R;
import riderz.team10.ecse321.com.riderzpassengers.assets.convertor.Display;
import riderz.team10.ecse321.com.riderzpassengers.assets.templates.activity.AppCompatActivityBack;
import riderz.team10.ecse321.com.riderzpassengers.constants.HTTP;
import riderz.team10.ecse321.com.riderzpassengers.constants.TAG;
import riderz.team10.ecse321.com.riderzpassengers.constants.URL;
import riderz.team10.ecse321.com.riderzpassengers.http.HttpRequestClient;
import riderz.team10.ecse321.com.riderzpassengers.assets.geolocation.ReverseGeocoding;
import riderz.team10.ecse321.com.riderzpassengers.assets.convertor.SQLCompliance;

public class EditTrip extends AppCompatActivityBack implements HttpRequestClient {
    // Contains a list of generated IDs for TextViews
    final private ArrayList<Integer> generatedId = new ArrayList<>();

    // Contains a list of TextViews which can be modified
    final private ArrayList<Integer> validTrips = new ArrayList<>();

    // Provides access
    private String username;

    // Current trips
    private JSONArray json;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_trip);

        // Obtains the username from intent
        username = getIntent().getStringExtra("username");

        populateLinearLayout();
        mapButtons();
    }

    /**
     * Populate linear layout with programmatically generated TextViews
     */
    private void populateLinearLayout() {
        syncHttpRequest();
        LinearLayout layout = findViewById(R.id.editTripLayout);
        createTextViews(layout);
    }

    /**
     * Creates new TextView components and inserts them into a specific layout.
     * @param layout LinearLayout which will contain the TextView
     */
    private void createTextViews(LinearLayout layout) {
        try {
            for (int i = 0; i < json.length(); i++) {
                JSONObject object = json.getJSONObject(i);
                String text = "";

                // Allow modification if the trip isn't taking place, i.e. time now < starting time
                Timestamp ts = Timestamp.valueOf(SQLCompliance.convertToSQLTimestamp(object.getString("startingTime")));
                Timestamp now = new Timestamp(System.currentTimeMillis());
                if (now.before(ts)) {
                    // Inserts starting address
                    try {
                        Address address = ReverseGeocoding.reverseLookup(getApplicationContext(),
                                object.getDouble("startingLatitude"),
                                object.getDouble("startingLongitude"));
                        text += "Starting Address: " + ReverseGeocoding.safeAddressToString(address) + "\n";
                    } catch (IOException e) {
                        Log.e(TAG.editTripTag, e.getLocalizedMessage());
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG.editTripTag, e.getLocalizedMessage());
                    }

                    // Modify Timestamp to make it SQL compliant and insert address
                    text += "Starting Time: " +
                            SQLCompliance.convertToSQLTimestamp(object.getString("startingTime")) + "\n";

                    // Inserts ending address
                    try {
                        Address address = ReverseGeocoding.reverseLookup(getApplicationContext(),
                                object.getDouble("endingLatitude"),
                                object.getDouble("endingLongitude"));
                        text += "Ending Address: " + ReverseGeocoding.safeAddressToString(address) + "\n";
                    } catch (IOException e) {
                        Log.e(TAG.editTripTag, e.getLocalizedMessage());
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG.editTripTag, e.getLocalizedMessage());
                    }

                    // Modify Timestamp to make it SQL compliant and insert address
                    text += "Ending Time: " +
                            SQLCompliance.convertToSQLTimestamp(object.getString("endingTime"));

                    // Generate a new unique View ID
                    int id = View.generateViewId();
                    generatedId.add(id);

                    // Instantiate a new TextView, set text and id
                    TextView tv = new TextView(this);
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
                    validTrips.add(i);
                }
            }

            // No trips available to edit
            if (validTrips.size() == 0) {
                TextView tv = findViewById(R.id.editTripsNone);
                tv.setText("You have no trips to edit!");

                // Convert padding from dp to px and set padding
                int padding_in_px = Display.dpToPX(getResources(),16);
                tv.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);

                // Set font size to 16dp
                tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
                tv.setVisibility(View.VISIBLE);
            }
        } catch (JSONException e) {
            Log.e(TAG.editTripTag, "Failed to parse JSON array");
        }

    }

    @Override
    public void mapButtons() {
        for (int i = 0; i < validTrips.size(); i++) {
            final int x = i;
            findViewById(generatedId.get(i)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        // Pass information onto the next activity (i.e. EditTripTemplate)
                        // Finishes current activity as well
                        Intent intent = new Intent(EditTrip.this, EditTripTemplate.class);
                        JSONObject object = json.getJSONObject(validTrips.get(x));
                        intent.putExtra("tripID", "" + object.getInt("tripID"));
                        intent.putExtra("startingAddress",
                                ReverseGeocoding.safeAddressToString(ReverseGeocoding.reverseLookup(getApplicationContext(),
                                        object.getDouble("startingLatitude"),
                                        object.getDouble("startingLongitude"))));
                        intent.putExtra("startingTime", SQLCompliance.convertToSQLTimestamp(object.getString("startingTime")));
                        intent.putExtra("endingAddress",
                                ReverseGeocoding.safeAddressToString(ReverseGeocoding.reverseLookup(getApplicationContext(),
                                        object.getDouble("endingLatitude"),
                                        object.getDouble("endingLongitude"))));
                        intent.putExtra("endingTime", SQLCompliance.convertToSQLTimestamp(object.getString("endingTime")));
                        intent.putExtra("username", username);
                        intent.putExtra("seatsLeft", "" + object.getInt("seatsLeft"));
                        finish();
                        startActivity(intent);
                    } catch (JSONException e) {
                        Log.e(TAG.editTripTag, "Failed to map buttons");
                    } catch (IOException e) {
                        Log.e(TAG.editTripTag, "Failed to map buttons: " + e.getLocalizedMessage());
                    }
                }
            });
        }
    }

    @Override
    public void syncHttpRequest() {
        final RequestParams params = new RequestParams();
        params.add("operator", username);

        // Instantiate a new Runnable object which will handle http requests asynchronously
        // Then await until thread is finished to make the request synchronous.
        Thread t = new Thread(new Runnable() {
            // URL to target
            final String tripUrl = URL.baseUrl + "getItineraryByUsername";

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
                            json = new JSONArray(new String(responseBody));
                        } catch (JSONException e) {
                            Log.e(TAG.editTripTag, "Failed to parse JSON from HTTP response");
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          byte[] responseBody, Throwable error) {
                        Log.e(TAG.editTripTag, "Server error - could not contact server");
                    }
                });
            }
        });
        t.start();

        // Await until thread t has finished its execution before proceeding
        try {
            t.join();
        } catch (InterruptedException e) {
            Log.e(TAG.editTripTag, "Thread exception in login thread");
        }
    }
}
