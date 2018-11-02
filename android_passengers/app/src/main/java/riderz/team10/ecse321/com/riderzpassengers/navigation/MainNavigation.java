package riderz.team10.ecse321.com.riderzpassengers.navigation;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;

import cz.msebera.android.httpclient.Header;
import riderz.team10.ecse321.com.riderzpassengers.About;
import riderz.team10.ecse321.com.riderzpassengers.MapsPassengerActivity;
import riderz.team10.ecse321.com.riderzpassengers.R;
import riderz.team10.ecse321.com.riderzpassengers.RiderzPassengers;
import riderz.team10.ecse321.com.riderzpassengers.UserProfile;
import riderz.team10.ecse321.com.riderzpassengers.constants.HTTP;
import riderz.team10.ecse321.com.riderzpassengers.constants.TAG;
import riderz.team10.ecse321.com.riderzpassengers.constants.URL;
import riderz.team10.ecse321.com.riderzpassengers.http.HttpRequestClient;
import riderz.team10.ecse321.com.riderzpassengers.reservation.Reservation;
import riderz.team10.ecse321.com.riderzpassengers.settings.MainSetting;
import riderz.team10.ecse321.com.riderzpassengers.trip.EditTrip;
import riderz.team10.ecse321.com.riderzpassengers.trip.PastTrip;

public class MainNavigation extends AppCompatActivity implements HttpRequestClient {
    // Used to track if back button was pressed twice
    private boolean doubleBackPressed = false;

    // Contains credentials for login
    private File file;

    // Login credentials
    private String username;
    private String password;

    // Contains a list of generated IDs for TextViews
    final private ArrayList<Integer> generatedId = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_navigation);
        file = new File(getApplicationContext().getFilesDir(), "login.txt");
        if (!file.exists()) {
            Log.e(TAG.mainNavTag, "Cannot find file");
            navigateToLogin();
        } else {
            syncHttpRequest();
        }

        populateLinearLayout();
        mapButtons();
    }

    @Override
    public void onBackPressed() {
        // Exit application since back button pressed twice
        if (doubleBackPressed) {
            super.onBackPressed();
            return;
        }

        // Display a toast to screen
        this.doubleBackPressed = true;
        Toast.makeText(this, "Press again to exit application", Toast.LENGTH_SHORT).show();

        // Delay doubleBackPressed from setting to false for 1s
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackPressed = false;
            }
        }, 1000);
    }

    /**
     * Populate linear layout with programmatically generated TextViews
     */
    private void populateLinearLayout() {
        String[] textArr = {"View Ads", "Remove Reservations",
                "View Your Profile", "Settings", "About", "Log Out"};
        LinearLayout layout = findViewById(R.id.mainNavLayout);
        createTextViews(layout, textArr);
    }

    /**
     * Creates new TextView components and inserts them into a specific layout.
     * @param layout LinearLayout which will contain the TextView
     * @param text Text displayed by the TextView
     */
    private void createTextViews(LinearLayout layout, String[] text) {
        for (int i = 0; i < text.length; i++) {
            // Generate a new unique View ID
            int id = View.generateViewId();
            generatedId.add(id);

            // Instantiate a new TextView, set text and id
            TextView tv = new TextView(this);
            tv.setText(text[i]);
            tv.setId(id);

            // Convert padding from dp to px and set
            int padding_in_dp = 16;
            final float scale = getResources().getDisplayMetrics().density;
            int padding_in_px = (int) (padding_in_dp * scale + 0.5f);
            tv.setPadding(padding_in_px, padding_in_px, padding_in_px, padding_in_px);

            // Set font size to 14dp
            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);

            // Add TextView to layout
            layout.addView(tv);
        }
    }

    @Override
    public void mapButtons() {
        // Mapped to view ads
        (findViewById(generatedId.get(0))).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainNavigation.this, MapsPassengerActivity.class);
                intent.putExtra("isPreviewing", false);
                intent.putExtra("username", username);
                MainNavigation.this.startActivity(intent);
            }
        });

        // Mapped to view reservations
        (findViewById(generatedId.get(1))).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainNavigation.this, Reservation.class);
                intent.putExtra("username", username);
                MainNavigation.this.startActivity(intent);
            }
        });

        // Mapped to viewing user profile
        (findViewById(generatedId.get(2))).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainNavigation.this, UserProfile.class);
                intent.putExtra("username", username);
                MainNavigation.this.startActivity(intent);
            }
        });

        // Mapped to viewing Settings Activity
        (findViewById(generatedId.get(3))).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainNavigation.this, MainSetting.class);
                intent.putExtra("username", username);
                MainNavigation.this.startActivity(intent);
            }
        });

        // Mapped to viewing About Activity
        (findViewById(generatedId.get(4))).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainNavigation.this, About.class);
                MainNavigation.this.startActivity(intent);
            }
        });

        // Mapped to log out
        (findViewById(generatedId.get(5))).setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                logout();
            }
        });
    }

    @Override
    public void syncHttpRequest() {
        try {
            // Reads each line of the file using a BufferedReader object
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isr);
            String line;
            int lineNum = 0;
            while ((line = reader.readLine()) != null) {
                // First line should contain the username
                if (lineNum == 0) {
                    username = line;
                }
                // Second line should contain the password
                else if (lineNum == 1) {
                    password = line;
                }
                else {
                    break;
                }
                lineNum++;
            }
        } catch (Exception e) {
            // On any error navigate back to login page
            Log.e(TAG.mainNavTag, "Could not find login credentials text file");
            navigateToLogin();
        }

        // Instantiate RequestParams object
        // Add username and password strings
        final RequestParams params = new RequestParams();
        params.add("username", username);
        params.add("password", password);

        // Instantiate a new Runnable object which will handle http requests asynchronously
        // Then await until thread is finished to make the request synchronous.
        Thread t = new Thread(new Runnable() {
            // URL to target
            final String loginUrl = URL.baseUrl + "login";

            @Override
            public void run() {
                // Instantiate new synchronous http client, set timeout and perform post request
                // to perform login
                SyncHttpClient client = new SyncHttpClient();
                client.setTimeout(HTTP.maxTimeout);
                client.post(loginUrl, params, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        String responseString = new String(responseBody);
                        switch (responseString) {
                            case "User session has been established!":
                                // Do nothing session is valid
                                Log.v(TAG.mainNavTag, "User signed in from cache");
                                break;
                            default:
                                // Go back to login screen
                                Log.e(TAG.mainNavTag, "User could not be authenticated");
                                navigateToLogin();
                                break;
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          byte[] responseBody, Throwable error) {
                        // Go back to login screen
                        Log.e(TAG.loginTag, "Server error - could not contact server");
                        navigateToLogin();
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

    /**
     * Creates a menu in the action bar.
     * @param menu Menu object for this class
     * @return A modified menu object
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater findMenuItems = getMenuInflater();
        findMenuItems.inflate(R.menu.settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Kills off current activity and navigate to login screen.
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, RiderzPassengers.class);
        finish();
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Perform logout for user
        if (id == R.id.action_logout) {
            logout();
            return true;
        }

        return false;
    }

    /**
     * Logs out the user and deletes cached credentials.
     */
    private void logout() {
        file = new File(getApplicationContext().getFilesDir(), "login.txt");
        if (file.exists()) {
            file.delete();
        }
        navigateToLogin();
    }
}
