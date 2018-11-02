package riderz.team10.ecse321.com.riderzpassengers;

import android.os.Bundle;
import android.widget.TextView;

import riderz.team10.ecse321.com.riderzpassengers.assets.templates.activity.AppCompatActivityBack;

public class About extends AppCompatActivityBack {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ((TextView) findViewById(R.id.about)).setText(
                "Riderz Passengers application version " + BuildConfig.VERSION_NAME);
    }
}
