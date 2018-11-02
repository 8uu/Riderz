package riderz.team10.ecse321.com.riderzpassengers;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.loopj.android.http.*;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;
import riderz.team10.ecse321.com.riderzpassengers.assets.convertor.PriceCalculator;
import riderz.team10.ecse321.com.riderzpassengers.assets.convertor.SQLCompliance;
import riderz.team10.ecse321.com.riderzpassengers.assets.geolocation.ReverseGeocoding;
import riderz.team10.ecse321.com.riderzpassengers.constants.HTTP;

/**
 * This class deals with fetching Driver's Advertisement listings and showing them to an user who
 * wants to go from location A to location B. It filters itinerary by starting and ending geo
 * locations with a search radius variable by the user.
 */
public class AdsListings extends AppCompatActivity {

    private TextView editTxt;
    private Button selectAd;
    private SeekBar seekBar;
    private TextView seekBar_value;
    private LinearLayout listingsContainer;
    private ArrayList<JSONObject> adsListings_list = new ArrayList<JSONObject>();
    private ArrayList<JSONObject> adsInfo_list = new ArrayList<JSONObject>();
    private ArrayList<LinearLayout> adsInfo_boxes = new ArrayList<LinearLayout>();
    private String response;

    private int expandable_id= -1;
    private HashMap<String, Integer> hash = new HashMap<>();
    private HashMap<Integer, String> hash_geolocations = new HashMap<>();

    //INITIAL VALUES
    final String baseUrl = "https://riderz-t10.herokuapp.com/";
    final String itinerary_baseURL = baseUrl + "getItineraryNearDestination";
    final String adsInfo_baseURL = baseUrl + "adInfo";
    private String startingLongitude;
    private String startingLatitude;
    private String endingLongitude;
    private String endingLatitude;
    private String maximumDistance = "1000000000";
    private String arrivalTime;
    private String operator;



    //define different types of layout params to style them dynamically
    LinearLayout.LayoutParams param_1 = new LinearLayout.LayoutParams(100,100,1.0f);

    LinearLayout.LayoutParams param_2 = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f);

    LinearLayout.LayoutParams param_3 = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            3.0f
    );
    LinearLayout.LayoutParams layoutParams_img = new LinearLayout.LayoutParams( 200, 200);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ads_listings);

        operator = getIntent().getStringExtra("username");

        selectAd = (Button) findViewById(R.id.selectAd);

        Bundle bundle = getIntent().getExtras();

        startingLongitude = bundle.getString("startingLongitude");
        startingLatitude = bundle.getString("startingLatitude");
        endingLongitude = bundle.getString("endingLongitude");
        endingLatitude = bundle.getString("endingLatitude");
        arrivalTime = bundle.getString("arrivalTime");

        Log.e("hello" , startingLatitude + ", " + startingLongitude + ", " + endingLatitude + ", " + endingLongitude + "," + arrivalTime );

        listingsContainer = (LinearLayout) findViewById(R.id.listingsContainer);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar_value = (TextView) findViewById(R.id.seekBarValue);
        listingsContainer = (LinearLayout) findViewById(R.id.listingsContainer);

        param_1.setMargins(10, 10, 10,10);
        param_2.setMargins(10, 10, 10,10);
        param_3.setMargins(10, 10, 10,10);
        layoutParams_img.setMargins(10, 10, 10,10);

        displayAdsListings();
    }

    /**
     * Retrieves advertisement listings synchronously with input parameters passed by the previous
     * activity
     * @return void
     */
    public void getAdsListings_sync() {
        if(adsListings_list != null && adsListings_list.size()>0){
            adsListings_list.clear();
        }
        final RequestParams params = new RequestParams();
        params.add("startingLongitude", startingLongitude);
        params.add("startingLatitude", startingLatitude);
        params.add("endingLongitude", endingLongitude);
        params.add("endingLatitude", endingLatitude);
        params.add("maximumDistance", maximumDistance);
        params.add("arrivalTime", arrivalTime);
        params.add("operator", operator);

        // Instantiate a new Runnable object which will handle http requests asynchronously
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // Instantiate new synchronous http client, set timeout to 5 seconds and
                // perform GET request
                SyncHttpClient client = new SyncHttpClient();
                client.setTimeout(HTTP.maxTimeoutExtended);
                client.get(itinerary_baseURL, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String responseString = new String(responseBody);
                        if(!responseString.isEmpty()){
                            Log.d("Ads_Listings", "itinerary not empty: " + responseString);
                            response = "SUCCESS";
                            try {
                                //Parse JSON object
                                JSONArray jr = new JSONArray(responseString);
                                JSONObject jb;
                                if(jr.length()>0) {
                                    for(int i=0; i<jr.length(); i++){
                                        jb = (JSONObject)jr.getJSONObject(i);
                                        adsListings_list.add(jb);
                                    }
                                }
                            }catch (Exception e) {
                                Log.e("Ads_Listings", "Failed to retrieve itinerary");
                                response = "ERROR";
                            }
                        }else{
                            Log.e("Ads_Listings", "Failed to retrieve itinerary");
                            response = "ERROR";
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          byte[] responseBody, Throwable error) {
                        Log.e("Ads_Listings", "Server error - could not contact server");
                    }
                });
            }
        });
        t.start();

        // Await until thread t has finished its execution before proceeding
        try {
            t.join();
        } catch (InterruptedException e) {
            Log.e("Ads_Listings", "Thread exception in login thread");
        }
    }

    /**
     * Asynchronously fetches the Ads info with more details such as the driver's name and contact
     * info. It also includes car info such as the car make, model, and year.
     * @param tripID TripID is needed to retrieves all these info, as well as uniquely identifying
     *               the layouts.
     * @param collapsableBox This is the layout where the new info will be added to.
     */
    public void getAdsInfo_async(final String tripID, final LinearLayout collapsableBox) {
        if(adsInfo_list != null && adsInfo_list.size()>0){
            adsInfo_list.clear();
        }
        final Context context = getApplicationContext();
        final RequestParams params = new RequestParams();
        //initialize retry flags
        hash.put(tripID,0);
        params.add("tripID", tripID);

        // Instantiate a new Runnable object which will handle http requests asynchronously
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Instantiate new synchronous http client, set timeout to 5 seconds and
                // perform GET request
                AsyncHttpClient client = new AsyncHttpClient();
                client.setTimeout(HTTP.maxTimeoutExtended);
                client.get(adsInfo_baseURL, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String responseString = new String(responseBody);
                        if(!responseString.isEmpty()){
                            Log.d("Ads_Info", "Ads Info not empty" + responseString);
                            response = "SUCCESS";
                            try {
                                JSONObject jb = new JSONObject(responseString);
                                adsInfo_list.add(jb);

                                //create contents
                                TextView textView1 = new TextView(context);
                                TextView textView2 = new TextView(context);
                                TextView textView3 = new TextView(context);
                                TextView textView4 = new TextView(context);

                                //attach contents to collapsable box
                                collapsableBox.addView(textView1);
                                collapsableBox.addView(textView2);
                                collapsableBox.addView(textView3);
                                collapsableBox.addView(textView4);

                                textView1.setText("Driver's name: " + jb.getString("name"));
                                textView2.setText("Tel: " + jb.getString("phone"));
                                textView3.setText("Car info: " + jb.getString("make") + ", " + jb.getString("model") +
                                        ", " + jb.getString("year"));
                                textView4.setText("License plate: " + jb.getString("licensePlate"));

                                //hide it until clicked
                                collapsableBox.setVisibility(View.GONE);

                            }catch (Exception e) {
                                Log.e("Ads_Info", "Failed to retrieve Ads Info");
                                Log.e("Ads_Info", e.toString());

                                response = "ERROR";
                            }
                        }else{
                            Log.e("Ads_Info", "Failed to retrieve Ads Info");
                            response = "ERROR";
                        }
                    }

                    public void onFailure(int statusCode, Header[] headers,
                                          byte[] responseBody, Throwable error) {
                        Log.e("Ads_Info", "Server error - could not contact server");
                        if(hash.containsKey(tripID)){
                            int retry_counts = hash.get(tripID);
                            if(retry_counts < 3){
                                retry_counts++;
                                Log.e("Ads_Info", "Server error - retrying " + retry_counts + " times in thread with trip ID: " + tripID);
                                hash.put(tripID, retry_counts);
                                getAdsInfo_async(tripID, collapsableBox);
                            }else {
                                Log.e("Ads_Info", "Server error - Numbers of retries exceeded. Could not contact server");
                            }
                        }
                    }
                });
            }
        }).run();
    }

    /**
     * Display all driver's ads dynamically after fetching from the database.
     * Create layout and text view elements dynamically as we parse the Ads data.
     */
    private void displayAdsListings (){
        //HTTP GET request to get itineraries near destination
        getAdsListings_sync();

        // Iterate through the list of Ads to parse for the right information to display
        for(int i=0; i<adsListings_list.size(); i++){

            //Create a row for an individual Advertisement
            final LinearLayout listingsRow = new LinearLayout(this);
            listingsRow.setOrientation(LinearLayout.HORIZONTAL);
            listingsRow.setBackgroundColor(Color.WHITE);
            listingsRow.setPadding(10, 10, 10, 10);

            //Create a layout for where the information texts will be displayed
            final LinearLayout textviewBlock = new LinearLayout(this);
            textviewBlock.setOrientation(LinearLayout.VERTICAL);

            //This section will contain some expandable extra information that will toggle as the
            // user clicks on a row
            final LinearLayout textviewBlock_INNER = new LinearLayout(this);
            textviewBlock_INNER.setOrientation(LinearLayout.VERTICAL);

            //These text views will contain the details of each Advertisement
            TextView textView1 = new TextView(this);
            TextView textView2 = new TextView(this);
            TextView textView3 = new TextView(this);
            TextView textView4 = new TextView(this);
            textView1.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            textView2.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            textView3.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
            textView4.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);

            //Icon for each Ad
            ImageView driver_icon_view = new ImageView(this);
            driver_icon_view.setLayoutParams(layoutParams_img);
            driver_icon_view.setImageResource(R.mipmap.driver_logo);

            listingsRow.setLayoutParams(param_2);
            textviewBlock.setLayoutParams(param_3);
            textviewBlock_INNER.setLayoutParams(param_3);

            try{
                int tripID = Integer.parseInt(adsListings_list.get(i).getString("tripID"));
                // Assign a unique identifier for each expandable section
                textviewBlock_INNER.setId(tripID);

                // Find the corresponding address for the geolocations
                ReverseGeocoding reverseGeocoder = new ReverseGeocoding();
                Double startLatitude = Double.parseDouble(adsListings_list.get(i).getString("startingLatitude"));
                Double startLongitude = Double.parseDouble(adsListings_list.get(i).getString("startingLongitude"));
                Double endLatitude = Double.parseDouble(adsListings_list.get(i).getString("endingLatitude"));
                Double endLongitude = Double.parseDouble(adsListings_list.get(i).getString("endingLongitude"));

                String coordinates = "";
                coordinates += startLatitude + "," + startLongitude + "," + endLatitude + "," + endLongitude;
                hash_geolocations.put(tripID, coordinates);
                Log.e("LOGGER", hash_geolocations.toString());

                Address addressStart = reverseGeocoder.reverseLookup(this, startLatitude, startLongitude);
                Address addressEnd = reverseGeocoder.reverseLookup(this, endLatitude, endLongitude);
                Double distance = PriceCalculator.calculateDistance(startLatitude, startLongitude, endLatitude, endLongitude);
                Double price = PriceCalculator.calculatePrice(distance);
                DecimalFormat df = new DecimalFormat("#.00"); //format to only two decimals

                //update front end presentation of values
                textView1.setText("Pick up: " + reverseGeocoder.safeAddressToString(addressStart));
                textView2.setText("Drop-off: " + reverseGeocoder.safeAddressToString(addressEnd));
                textView3.setText("Arrival time: " + SQLCompliance.convertToSQLTimestamp(adsListings_list.get(i).getString("endingTime")));
                textView4.setText("Price: " + df.format(price) + " $");
            }catch(Exception e){
                Log.e("JSON ERROR", "Failed to get JSON field");
            }

            //attach views
            listingsContainer.addView(listingsRow);
            listingsRow.addView(driver_icon_view);
            listingsRow.addView(textviewBlock);
            textviewBlock.addView(textView1);
            textviewBlock.addView(textView2);
            textviewBlock.addView(textView3);
            textviewBlock.addView(textView4);
            textviewBlock.addView(textviewBlock_INNER);

            try {
                getAdsInfo_async(adsListings_list.get(i).getString("tripID"), textviewBlock_INNER);
            }catch(Exception e){
                Log.e("ADS_INFO", "Failed to GET Ads info from server.");
            }

            //clicking on the rows
            listingsRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //get the container for the extendable section
                    View inner_ads_info = ((LinearLayout) listingsRow.getChildAt(1)).getChildAt(4);

                    // expandable_id stores the id of the previously expanded section
                    //since id is set by default to -1, this allows us to identify whether an id has
                    // been overridden or not.
                    if(expandable_id > -1){
                        findViewById(expandable_id).setVisibility(View.GONE);
                    }
                    //override the default Id
                    expandable_id = inner_ads_info.getId();
                    Log.e("LOGGING", Integer.toString(expandable_id));
                    inner_ads_info.setVisibility(View.VISIBLE);

                    selectAd.setText("Request driver");
                    selectAd.setTextColor(Color.WHITE);
                    selectAd.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
                    for(int i=0; i< listingsContainer.getChildCount(); i++){
                        listingsContainer.getChildAt(i).setBackgroundColor(Color.WHITE);
                    }
                    listingsRow.setBackgroundColor(Color.parseColor("#FFC0C3"));
                }
            });

            ConstraintLayout background = (ConstraintLayout) findViewById(R.id.generalBackground);
            background.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // close previously opened expendable sections
                    if(expandable_id > -1){
                        findViewById(expandable_id).setVisibility(View.GONE);
                    }
                    selectAd.setText("Pick a driver");
                    selectAd.setBackgroundColor(Color.parseColor("#d6d7d7"));
                    for(int i=0; i< listingsContainer.getChildCount(); i++){
                        listingsContainer.getChildAt(i).setBackgroundColor(Color.WHITE);
                    }
                }
            });

            selectAd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String startingAddress = ((TextView)((LinearLayout) listingsRow.getChildAt(1)).getChildAt(0)).getText().toString();
                    String endingAddress = ((TextView)((LinearLayout) listingsRow.getChildAt(1)).getChildAt(1)).getText().toString();
                    int tripID = expandable_id;
                    String coordinates = "";
                    Log.e("LOGGER_ONCLICK", "" + tripID);
                    if(hash_geolocations.containsKey(tripID)){
                        Log.e("LOGGER_GEO_HASH", hash_geolocations.toString());
                        coordinates = hash_geolocations.get(tripID);
                    }

                    Intent intent = new Intent(AdsListings.this, MapsPassengerActivity.class);
                    if(coordinates.length() >=4){
                        String[] coords = coordinates.split(",");
                        intent.putExtra("startingLatitude", coords[0]);
                        intent.putExtra("startingLongitude", coords[1]);
                        intent.putExtra("endingLatitude", coords[2]);
                        intent.putExtra("endingLongitude", coords[3]);

                        Log.e("LOGGER_GEO_HASH", coords[0].toString());
                        Log.e("LOGGER_GEO_HASH", coords[1].toString());
                        Log.e("LOGGER_GEO_HASH", coords[2].toString());
                        Log.e("LOGGER_GEO_HASH", coords[3].toString());
                    }
                    intent.putExtra("tripID", tripID);
                    intent.putExtra("isPreviewing", true);
                    intent.putExtra("username", operator);
                    startActivity(intent);
                }
            });

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                int save = 0;

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    save = progress;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    //nothing
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    //update textview
                    seekBar_value.setText("Search radius: " + save + " km");
                    //update variable and send http request
                    maximumDistance = Integer.toString(save * 1000);
                    getAdsListings_sync();

                }
            });

        }
    }

}
