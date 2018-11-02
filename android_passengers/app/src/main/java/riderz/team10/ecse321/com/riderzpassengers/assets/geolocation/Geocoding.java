package riderz.team10.ecse321.com.riderzpassengers.assets.geolocation;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import java.io.IOException;
import java.util.List;

public class Geocoding {
    /**
     * Converts an address into latitude and longitude.
     * @param context An application context
     * @param address A string representing an address
     * @param tag A tag for Logcat logging
     * @return A LatLng object or null.
     */
    public static LatLng getLatLngFromAddress(Context context, String address, String tag) {
        try {
            Geocoder coder = new Geocoder(context);
            List<Address> addressList = coder.getFromLocationName(address, 1);

            // No result found
            if (addressList == null || addressList.size() < 1) {
                return null;
            }

            return new LatLng(addressList.get(0).getLatitude(),
                              addressList.get(0).getLongitude());
        } catch (IOException e) {
            Log.e(tag, "Could not obtain longitude and latitude from address");
            return null;
        }
    }
}



