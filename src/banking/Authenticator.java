package banking;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.security.SecureRandom;
import org.apache.commons.codec.binary.Base32;

public class Authenticator {
    
    // referenced : https://web.archive.org/web/20220809054451/https://pthree.org/2014/04/15/time-based-one-time-passwords-how-it-works/
    private static Set<Integer> generateTOTPs(String secret) {
        Base32 base32 = new Base32();
        byte[] key = base32.decode(secret);

        long currentTime = Instant.now().getEpochSecond();
        long timeStep = 30;

        Set<Integer> totpSet = new HashSet<>();

        // Calculate TOTPs for current, previous, and next time steps, as most authenticators implement this to allow for some amount of time desync
        for (int i = -1; i <= 1; i++) {
            long counter = (currentTime / timeStep) + i;
            totpSet.add(generateTOTPForCounter(key, counter));
        }

        return totpSet;
    }

    public static boolean validateTOTP(String secret,int OTP) {
        // just return true if any match
        Set<Integer> validWindow = generateTOTPs(secret);
        for (int code : validWindow) {
            if (code == OTP) {
                return true;
            }
        }
        return false;
    }

    private static int generateTOTPForCounter(byte[] key, long counter) {
        byte[] data = ByteBuffer.allocate(8).putLong(counter).array(); // constructs the data to be hashed, uses UNIX timestamps

        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec signKey = new SecretKeySpec(key, "HmacSHA1");
            mac.init(signKey);
            byte[] hash = mac.doFinal(data);

            // truncates the code to 6 digits
            int offset = hash[hash.length - 1] & 0xF;
            int binary = ((hash[offset] & 0x7F) << 24)
                       | ((hash[offset + 1] & 0xFF) << 16)
                       | ((hash[offset + 2] & 0xFF) << 8)
                       | (hash[offset + 3] & 0xFF);

            return binary % 1_000_000;
        } catch (Exception e) {
            System.out.println("An error occurred during TOTP generation.");
            return -1; // Return an invalid TOTP to prevent abuse
        }
    }

    public static String generateSecureSecret() {
        SecureRandom secureRandom = new SecureRandom();
        byte[] secretBytes = new byte[20];
        secureRandom.nextBytes(secretBytes); // fills with random bytes.
        Base32 base32 = new Base32();
        return base32.encodeToString(secretBytes);
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes());

            // Convert hash bytes to hex string
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            String hashedPassword = sb.toString();
            return hashedPassword;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not found.");
        }
    }
}


