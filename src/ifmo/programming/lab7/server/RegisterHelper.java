package ifmo.programming.lab7.server;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class RegisterHelper {

    static byte[] hashPassword(String password) throws NoSuchAlgorithmException, UnsupportedEncodingException/*, GeneralSecurityException*/ {

        byte[] pswdBytes = password.getBytes("UTF-8");
        java.security.MessageDigest digest = MessageDigest.getInstance("MD5");
        digest.reset();
        byte[] array = digest.digest(pswdBytes);

        return array;
    }

    static final String AB = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    static SecureRandom rnd = new SecureRandom();

    static String randomString( int len ){
        StringBuilder sb = new StringBuilder( len );
        for( int i = 0; i < len; i++ )
            sb.append( AB.charAt( rnd.nextInt(AB.length()) ) );
        return sb.toString();
    }
}
