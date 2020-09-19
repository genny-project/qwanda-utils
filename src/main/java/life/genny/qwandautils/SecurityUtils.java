package life.genny.qwandautils;

import java.lang.invoke.MethodHandles;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.binary.Base64;
import org.apache.logging.log4j.Logger;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.InvalidKeyException;
//import io.jsonwebtoken.impl.crypto.MacProvider;
//import io.jsonwebtoken.impl.crypto.MacProvider;
//import io.jsonwebtoken.impl.TextCodec;
//import io.jsonwebtoken.impl.crypto.MacProvider;
import io.jsonwebtoken.security.Keys;

public class SecurityUtils {
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	public static String encrypt(String key, String initVector, String value) {
		try {
			value = value.substring(0,value.length()>16?16:value.length());
			IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
			SecretKeySpec skeySpec = new SecretKeySpec(key.getBytes("UTF-8"), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);

			byte[] encrypted = cipher.doFinal(value.getBytes());
			log.info("encrypted string: " + Base64.encodeBase64String(encrypted));

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

		log.info(decrypt(key, initVector, encrypt(key, initVector, "Hello World")));
	}

	public static String createJwt(String id, String issuer, String subject, long ttlMillis, String apiSecret,
			Map<String, Object> claims) {

		// The JWT signature algorithm we will be using to sign the token
		SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
		String aud = issuer;
		if (claims.containsKey("aud")) {
			aud = (String) claims.get("aud");
			claims.remove("aud");
		}
		long nowMillis = System.currentTimeMillis();
		Date now = new Date(nowMillis);

		// We will sign our JWT with our ApiKey secret
		byte[] apiKeySecretBytes = DatatypeConverter.parseBase64Binary(apiSecret);

	//	Map<String, Object> claims2 = new HashMap<String, Object>();
	//	claims2.put("preferred_username", (String) claims.get("preferred_username"));

//		claims2.put("name", name.getBytes("UTF-8"));
//		claims2.put("realm", realm);
//		claims2.put("azp", realm);
//		claims2.put("aud", realm);
//		claims2.put("realm_access", "[user," + role + "]");
//		claims2.put("exp", expiryDateTime.atZone(ZoneId.of("UTC")).toEpochSecond());
//		claims2.put("iat", LocalDateTime.now().atZone(ZoneId.of("UTC")).toEpochSecond());
//		claims2.put("auth_time", LocalDateTime.now().atZone(ZoneId.of("UTC")).toEpochSecond());
//		claims2.put("session_state", UUID.randomUUID().toString().substring(0, 32).getBytes("UTF-8")); // TODO set size ot same

//	    SecretKey key = MacProvider.generateKey(SignatureAlgorithm.HS256);
//	    String base64Encoded = TextCodec.BASE64.encode(key.getEncoded());

		// Let's set the JWT Claims
		JwtBuilder builder = Jwts.builder().setId(id).setIssuedAt(now).setSubject(subject).setIssuer(issuer)
				.setAudience(aud).setClaims(claims);
		// .signWith(signatureAlgorithm, signingKey);

		Key key = null;

		try {
			key = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
//	key  = Keys.secretKeyFor(SignatureAlgorithm.HS256);
			builder.signWith(SignatureAlgorithm.HS256, key);
		} catch (Exception e) {
			try {
				key = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
				builder.signWith(SignatureAlgorithm.HS256, key);
			} catch (Exception e1) {
// TODO Auto-generated catch block
				try {
					Key signingKey = new SecretKeySpec(apiKeySecretBytes, signatureAlgorithm.getJcaName());
					builder.signWith(signatureAlgorithm, signingKey);
				} catch (InvalidKeyException e2) {
//log.error("Cannot creating key foor JWT");
				}
			}
		}

		// if it has been specified, let's add the expiration
		if (ttlMillis >= 0) {
			long expMillis = nowMillis + ttlMillis;
			Date exp = new Date(expMillis);
			builder.setExpiration(exp);
		}

		// Builds the JWT and serializes it to a compact, URL-safe string
		return builder.compact();
	}

}