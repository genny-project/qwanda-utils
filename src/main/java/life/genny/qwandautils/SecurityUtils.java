package life.genny.qwandautils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import io.jsonwebtoken.Jwts;

import java.io.IOException;
import java.nio.file.*;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.*;
import java.util.*;

import static io.jsonwebtoken.SignatureAlgorithm.RS256;
import static java.lang.Boolean.TRUE;

public class SecurityUtils {
    public static String encrypt(String key, String initVector, String value) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

            byte[] encrypted = cipher.doFinal(value.getBytes());
            System.out.println("encrypted string: "
                    + Base64.encodeBase64String(encrypted));

            return Base64.encodeBase64String(encrypted);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static String decrypt(String key, String initVector, String encrypted) {
        try {
            IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
            SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] original = cipher.doFinal(Base64.decodeBase64(encrypted));

            return new String(original);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    
    
    public static void main(String[] args) {
        String key = "Bar12345Bar12345"; // 128 bit key
        String initVector = "RandomInitVector"; // 16 bytes IV

        System.out.println(decrypt(key, initVector,
                encrypt(key, initVector, "Hello World")));
    }
    
  

    public static String createJWT() 
                throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {

            // Sample JWT creation. The example uses SHA256withRSA signature algorithm.

            // API key information (substitute actual credential values)
            String orgId = "048ABCD8562023457F000101@AdobeOrg";
            String technicalAccountId = "AABCD8DB57F4B32801234033@techacct.adobe.com";
            String apiKey = "ec9a2091e2c64f0492c612344700abcd";

            // Set expirationDate in milliseconds since epoch to 24 hours ahead of now 
            Long expirationTime = System.currentTimeMillis() / 1000 + 86400L;

            // Metascopes associated to key
            String metascopes[] = new String[]{"genny"};

            // Secret key as byte array. Secret key file should be in DER encoded format.
            byte[] privateKeyFileContent = Files.readAllBytes(Paths.get("/path/to/secret/key"));

            String imsHost = "app.genny.life";

            // Create the private key
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            KeySpec ks = new PKCS8EncodedKeySpec(privateKeyFileContent);
            RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(ks);

            // Create JWT payload
            Map jwtClaims = new HashMap<>();
            jwtClaims.put("iss", orgId);
            jwtClaims.put("sub", technicalAccountId);
            jwtClaims.put("exp", expirationTime);
            jwtClaims.put("aud", "https://" + imsHost + "/c/" + apiKey);
            for (String metascope : metascopes) {
                jwtClaims.put("https://" + imsHost + "/s/" + metascope, TRUE);
            }

            // Create the final JWT token
            String jwtToken = Jwts.builder().setClaims(jwtClaims).signWith(RS256, privateKey).compact();

            return jwtToken;
        }


}