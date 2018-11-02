package riderz.team10.ecse321.com.riderzpassengers.assets.templates.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public class AppCompatActivityBack extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable back button on action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // Allows navigation back to previous screen
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return false;
    }
}
