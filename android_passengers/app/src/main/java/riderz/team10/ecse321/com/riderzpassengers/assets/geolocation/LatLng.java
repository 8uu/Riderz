package riderz.team10.ecse321.com.riderzpassengers.assets.geolocation;

/**
 * Replaces LatLng provided by Google Maps API. No need for API key.
 */
public class LatLng {
    private double latitude;
    private double longitude;

    public LatLng(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }
}
