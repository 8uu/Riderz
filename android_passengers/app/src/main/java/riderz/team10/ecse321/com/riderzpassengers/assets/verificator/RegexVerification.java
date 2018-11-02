package riderz.team10.ecse321.com.riderzpassengers.assets.verificator;

import java.util.regex.Pattern;

public class RegexVerification {
    /**
     * Verifies validity of email address using OWASP REGEX validation.
     * @param email An email address
     * @return True if the email address is valid
     */
    public static boolean verifyEmail(String email) {
        if (email == null) {
            return false;
        }
        return Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")
                .matcher(email).matches();
    }

    /**
     * Verifies validity of phone number using OWASP REGEX validation.
     * @param phone A phone number
     * @return True if the phone number is valid
     */
    public static boolean verifyPhone(String phone) {
        if (phone == null) {
            return false;
        }
        return Pattern.compile("^\\D?(\\d{3})\\D?\\D?(\\d{3})\\D?(\\d{4})$")
                .matcher(phone).matches();
    }

    /**
     * Verifies if a string is only composed of alphabets.
     * @param str A String to verify
     * @return True if the string is purely alphabets
     */
    public static boolean isAlphabetical(String str) {
        if (str == null) {
            return false;
        }
        return Pattern.compile("^[a-zA-Z]*$").matcher(str).matches();
    }

    /**
     * Verifies if a string is only composed of numbers.
     * @param str A String to verify
     * @return True if the string is purely numbers
     */
    public static boolean isNumerical(String str) {
        if (str == null) {
            return false;
        }
        return Pattern.compile("^[0-9]*$").matcher(str).matches();
    }

    /**
     * Verifies if a string is only composed of numbers and alphabets.
     * @param str A String to verify
     * @return True if the string is purely numbers and alphabets
     */
    public static boolean isAlphanumerical(String str) {
        if (str == null) {
            return false;
        }
        return Pattern.compile("^[a-zA-Z0-9]*$").matcher(str).matches();
    }
}
