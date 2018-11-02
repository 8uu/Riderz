package riderz.team10.ecse321.com.riderzpassengers.trip;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;
import cz.msebera.android.httpclient.Header;
import org.json.JSONException;
import org.json.JSONObject;
import riderz.team10.ecse321.com.riderzpassengers.R;
import riderz.team10.ecse321.com.riderzpassengers.assets.geolocation.Geocoding;
import riderz.team10.ecse321.com.riderzpassengers.assets.geolocation.LatLng;
import riderz.team10.ecse321.com.riderzpassengers.assets.templates.activity.AppCompatActivityBack;
import riderz.team10.ecse321.com.riderzpassengers.constants.HTTP;
import riderz.team10.ecse321.com.riderzpassengers.constants.TAG;
import riderz.team10.ecse321.com.riderzpassengers.constants.URL;
import riderz.team10.ecse321.com.riderzpassengers.http.HttpRequestClient;
import riderz.team10.ecse321.com.riderzpassengers.navigation.MainNavigation;

import java.sql.Timestamp;
import java.util.Locale;

public class EditTripTemplate extends AppCompatActivityBack implements HttpRequestClient {
    // Contains information from intent
    protected String startingAddress;
    protected String startingTime;
    protected String endingAddress;
    protected String endingTime;
    protected int tripID;
    protected int seatsLeft;
    protected String username;

    // EditTexts
    protected EditText sa;
    protected EditText st;
    protected EditText ea;
    protected EditText et;
    protected ConstraintLayout textLayout;

    // TextViews
    protected TextView errorMsg;

    // DatePicker
    protected DatePicker dp;
    protected ConstraintLayout dpLayout;

    // TimePicker
    protected TimePicker tp;
    protected ConstraintLayout tpLayout;

    // Used to handle where the timestamp goes
    protected boolean start = true;

    // Used by HTTP requests
    protected boolean success = false;
    protected String msg = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_trip_template);

        // Obtain information from intent
        startingAddress =  getIntent().getStringExtra("startingAddress");
        startingTime = getIntent().getStringExtra("startingTime");
        endingAddress = getIntent().getStringExtra("endingAddress");
        endingTime = getIntent().getStringExtra("endingTime");
        tripID = Integer.parseInt(getIntent().getStringExtra("tripID"));
        seatsLeft = Integer.parseInt(getIntent().getStringExtra("seatsLeft"));
        username = getIntent().getStringExtra("username");

        // Hide error message for now
        errorMsg = findViewById(R.id.editTripTemplateError);
        errorMsg.setVisibility(View.INVISIBLE);

        // Map objects to their counterparts
        sa = findViewById(R.id.startingAddressETT);
        st = findViewById(R.id.startingTimeETT);
        ea = findViewById(R.id.endingAddressETT);
        et = findViewById(R.id.endingTimeETT);
        errorMsg = findViewById(R.id.editTripTemplateError);
        textLayout = findViewById(R.id.editTripTempOuter);
        dp = findViewById(R.id.editTripDate);
        dpLayout = findViewById(R.id.datePickerETT);
        dpLayout.setVisibility(View.GONE);
        tp = findViewById(R.id.editTripClock);
        tpLayout = findViewById(R.id.timePickerETT);
        tpLayout.setVisibility(View.GONE);

        // Display current values on screen
        sa.setText(startingAddress);
        st.setText(startingTime);
        ea.setText(endingAddress);
        et.setText(endingTime);

        mapButtons();
    }

    @Override
    public void mapButtons() {
        // Hide DatePicker on cancel and redisplay texts
        findViewById(R.id.cancelDateETT).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dpLayout.setVisibility(View.GONE);
                textLayout.setVisibility(View.VISIBLE);
            }
        });

        // Hide DatePicker and proceed to TimePicker on confirm
        findViewById(R.id.confirmDateETT).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dpLayout.setVisibility(View.GONE);
                tpLayout.setVisibility(View.VISIBLE);
            }
        });

        // Hide TimePicker on cancel and redisplay texts
        findViewById(R.id.cancelTimeETT).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                tpLayout.setVisibility(View.GONE);
                textLayout.setVisibility(View.VISIBLE);
            }
        });

        // Hide TimePicker on cancel and redisplay texts
        findViewById(R.id.confirmTimeETT).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                tpLayout.setVisibility(View.GONE);
                textLayout.setVisibility(View.VISIBLE);
                fillTextBoxes();
            }
        });

        // Open DatePicker on click startingTime
        findViewById(R.id.startingTimeETT).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dpLayout.setVisibility(View.VISIBLE);
                textLayout.setVisibility(View.GONE);
                start = true;
            }
        });

        // Open DatePicker on click endingTime
        findViewById(R.id.endingTimeETT).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dpLayout.setVisibility(View.VISIBLE);
                textLayout.setVisibility(View.GONE);
                start = false;
            }
        });

        // Perform HTTP request to edit trip
        findViewById(R.id.confirmEditTrip).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                syncHttpRequest();
                if (success) {
                    success = false;
                    msg = "";
                    Intent intent = new Intent(EditTripTemplate.this, MainNavigation.class);
                    finish();
                    startActivity(intent);
                }
                errorMsg.setText(msg);
                errorMsg.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Fills starting and stopping
     */
    private void fillTextBoxes() {
        int year = dp.getYear();
        int day = dp.getDayOfMonth();
        int month = dp.getMonth() + 1;

        int hour;
        int minute;

        // Handle API >= 23
        if (Build.VERSION.SDK_INT >= 23) {
            hour = tp.getHour();
            minute = tp.getMinute();
        } else {
            hour = tp.getCurrentHour();
            minute = tp.getCurrentMinute();
        }
        if (start) {
            st.setText(String.format("%04d-%02d-%02d %02d:%02d:00", year, month, day, hour, minute));
        } else {
            et.setText(String.format("%04d-%02d-%02d %02d:%02d:00", year, month, day, hour, minute));
            Timestamp start = Timestamp.valueOf(st.getText().toString());
            Timestamp end = Timestamp.valueOf(et.getText().toString());
            if (start.after(end)) {
                msg = "Start time cannot be after end time";
            }
        }
    }

    @Override
    public void syncHttpRequest() {
        // Perform geocoding on starting address
        final LatLng startingLatLng = Geocoding.getLatLngFromAddress(getApplicationContext(),
                sa.getText().toString(),
                TAG.editTripTemplateTag);
        if (startingLatLng == null) {
            Log.e(TAG.editTripTemplateTag, "Failed to obtain any addresses");
            msg = "Starting address is not valid";
            success = false;
            return;
        }

        // Perform geocoding on ending address
        final LatLng endingLatLng = Geocoding.getLatLngFromAddress(getApplicationContext(),
                ea.getText().toString(),
                TAG.editTripTemplateTag);
        if (endingLatLng == null) {
            Log.e(TAG.editTripTemplateTag, "Failed to obtain any addresses");
            msg = "Ending address is not valid";
            success = false;
            return;
        }

        // Verify time
        if (Timestamp.valueOf(st.getText().toString()).after(Timestamp.valueOf(et.getText().toString()))) {
            success = false;
            msg = "End time cannot be before start time";
            return;
        }

        // Instantiate a new Runnable object which will handle http requests asynchronously
        // Then await until thread is finished to make the request synchronous.
        Thread t = new Thread(new Runnable() {
            // URL to target
            String tripUrl = URL.baseUrl +
                    String.format(Locale.CANADA, "updateItinerary/%d/%f/%f/%s.000/%f/%f/%s.000/%d/%s",
                            tripID,
                            startingLatLng.getLongitude(),
                            startingLatLng.getLatitude(),
                            st.getText().toString(),
                            endingLatLng.getLongitude(),
                            endingLatLng.getLatitude(),
                            et.getText().toString(),
                            seatsLeft,
                            username);
            @Override
            public void run() {
                // Instantiate new synchronous http client, set timeout and perform get request
                // Uses extended timeout
                SyncHttpClient client = new SyncHttpClient();
                client.setTimeout(HTTP.maxTimeoutExtended);
                tripUrl = tripUrl.replaceAll("\\s+", "%20");
                client.put(tripUrl, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        if (responseBody.length > 0) {
                            // Tests if the response is a valid JSON object
                            try {
                                new JSONObject(new String(responseBody));
                                success = true;
                            } catch (JSONException e) {
                                Log.e(TAG.editTripTemplateTag, "Invalid response from server");
                                msg = "Obtained invalid response from server";
                            }
                        } else {
                            success = false;
                            msg = "Failed to update trip";
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          byte[] responseBody, Throwable error) {
                        Log.e(TAG.editTripTemplateTag, "Server error - could not contact server");
                        Log.e(TAG.editTripTemplateTag, new String(responseBody));
                    }
                });
            }
        });
        t.start();

        // Await until thread t has finished its execution before proceeding
        try {
            t.join();
        } catch (InterruptedException e) {
            Log.e(TAG.editTripTemplateTag, "Thread exception in login thread");
        }
    }
}
