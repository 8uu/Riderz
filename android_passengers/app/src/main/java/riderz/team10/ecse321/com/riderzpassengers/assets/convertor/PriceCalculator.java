package riderz.team10.ecse321.com.riderzpassengers.assets.convertor;

public class PriceCalculator {

    public static Double calculateDistance(Double originLatitude, Double originLongitude, Double destinationLatitude, Double destinationLongitude ){
        // Calculate the distance between origin and destination (m)
        final int R = 6371000; // Radius of the earth (m)

        double latDistance = Math.toRadians(destinationLatitude - originLatitude);
        double lonDistance = Math.toRadians(destinationLongitude - originLongitude);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(originLatitude)) * Math.cos(Math.toRadians(originLongitude))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (1.7 * R * c ); // In meters
    }

    public static String calculateDuration (Double distance){
        double speed = 30000.0; // Default speed(30km/h)
        if (distance > 100000){
            speed = 100000;
        }
        // Calculate the time between origin and destination (ms)
        return String.valueOf((distance/speed)*3600 * 1000);
    }

    public static Double calculatePrice( Double distance) {
        // Price
        return (2.5 + 0.75 * distance/1000);
    }
}
