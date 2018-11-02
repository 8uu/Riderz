package riderz.team10.ecse321.com.riderzpassengers.settings;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import riderz.team10.ecse321.com.riderzpassengers.R;
import riderz.team10.ecse321.com.riderzpassengers.assets.templates.activity.AppCompatActivityBack;

public class MainSetting extends AppCompatActivityBack {
    // Login credentials
    private String username;

    // Contains a list of generated IDs for TextViews
    final private ArrayList<Integer> generatedId = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_setting);

        // Obtain username
        username = getIntent().getStringExtra("username");

        populateLinearLayout();
        mapTextViews();
    }

    /**
     * Populate linear layout with programmatically generated TextViews
     */
    private void populateLinearLayout() {
        String[] textArr = {"Change Your Password", "Modify Your Email",
                "Update Contact Information"};
        LinearLayout layout = findViewById(R.id.mainSettingLayout);
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

    /**
     * Maps TextViews to an appropriate action on click
     */
    private void mapTextViews () {
        // Mapped to resetting password
        (findViewById(generatedId.get(0))).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) { navigate(ResetPassword.class); }
        });

        // Mapped to changing email
        (findViewById(generatedId.get(1))).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { navigate(ModifyEmail.class); }
        });

        // Mapped to modifying contact information
        (findViewById(generatedId.get(2))).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) { navigate(ModifyContactInfo.class); }
        });

    }

    /**
     * Creates an intent to the specified class, terminates current activity and starts
     * a new activity.
     * @param c A Class object
     */
    private void navigate(Class c) {
        Intent intent = new Intent(MainSetting.this, c);
        intent.putExtra("username", username);
        startActivity(intent);
    }
}
