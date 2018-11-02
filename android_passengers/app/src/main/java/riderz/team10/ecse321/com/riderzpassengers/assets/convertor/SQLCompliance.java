package riderz.team10.ecse321.com.riderzpassengers.assets.convertor;

public class SQLCompliance {
    /**
     * Converts a timestamp in String into a SQL compliant timestamp.
     * @param timestamp A String representing a timestamp.
     * @return A formatted timestamp.
     */
    public static String convertToSQLTimestamp(String timestamp) {
        timestamp = timestamp.replace("T", " ");
        timestamp = timestamp.replace("+", "0");
        int index = timestamp.indexOf(".");
        timestamp = timestamp.substring(0, index);
        return timestamp;
    }
}
