package riderz.team10.ecse321.com.riderzpassengers;

import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class GeoLocation {
    /*
    double latitude;
    double longitude;
    private double getLatitude(){
    }
    */
    public LatLng getLocationFromAddress(String locationAddress, Geocoder coder){


        List<Address> address;
        LatLng point = null;

        try {
            address = coder.getFromLocationName(locationAddress,1);
            if (address==null) {
                return null;
            }
            Address location=address.get(0);

            point = new LatLng(location.getLatitude(), location.getLongitude());

            return point;
        } catch (IOException e){
            Log.e("DebugGeolocation","Exception: getLocationFromAddress");
            return null;
        }
    }
}