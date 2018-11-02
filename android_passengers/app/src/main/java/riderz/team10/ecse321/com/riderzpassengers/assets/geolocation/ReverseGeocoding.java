package riderz.team10.ecse321.com.riderzpassengers.assets.geolocation;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;

public class ReverseGeocoding {
    /**
     * Performs reverse geocoding lookup from latitude and longitude using Android Location API.
     * @param context An application context
     * @param latitude
     * @param longitude
     * @return An address if found, null otherwise.
     * @throws IOException
     */
    public static Address reverseLookup(Context context, double latitude, double longitude) throws IOException, IllegalArgumentException{
        Geocoder geoCoder = new Geocoder(context);
        List<Address> matches = geoCoder.getFromLocation(latitude, longitude, 1);
        return (matches.isEmpty() ? null : matches.get(0));
    }

    /**
     * Safely extracts address line from an Address object.
     * @param address An Android Location Address object
     * @return A String representing the address line or 'No Record Found'
     */
    public static String safeAddressToString(Address address) {
        if (address == null) {
            return "No Record Found";
        }
        String addressLine = address.getAddressLine(0);
        if (addressLine != null) {
            return addressLine;
        }
        return "No Record Found";
    }
}
