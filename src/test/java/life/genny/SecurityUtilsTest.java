package life.genny;

import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.Logger;
import org.junit.Test;

import com.google.common.base.Utf8;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.SecurityUtils;

public class SecurityUtilsTest {
	
	/**
	 * Stores logger object.
	 */
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	@Test
	public void versionTest()
	{
		        String key        = GennySettings.defaultServiceKey; // 128 bit key
		        String initVector = "PRJ_GENNY*******"; // 16 bytes IV

		        String encrypted = SecurityUtils.encrypt(key, initVector, GennySettings.defaultServicePassword);
		        log.info("["+encrypted+"]");;
		        log.info(SecurityUtils.decrypt(key, initVector, encrypted
		                ));
		        

	}
	
	@Test
	public void createJwtTest()
	{
		String secret = "IamAnApiSecret";
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime expiryTime = LocalDateTime.now().plusSeconds(24 * 60 * 60);

		Map<String,Object> claims = new HashMap<String, Object>();
		claims = new HashMap<String, Object>();
		try {
			claims.put("preferred_username","user1");
			claims.put("name", "user1".getBytes("UTF-8"));
			claims.put("realm", "genny");
			claims.put("azp", "genny");
			claims.put("aud", "genny");
			claims.put("realm_access", "[user," + "service" + "]");
			claims.put("exp", expiryTime.atZone(ZoneId.of("UTC")).toEpochSecond());
			claims.put("iat", LocalDateTime.now().atZone(ZoneId.of("UTC")).toEpochSecond());
			claims.put("auth_time", LocalDateTime.now().atZone(ZoneId.of("UTC")).toEpochSecond());
			claims.put("session_state", UUID.randomUUID().toString().substring(0, 32).getBytes("UTF-8")); // TODO set size ot same
																									// as keycloak
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

		String jws = Jwts.builder().setId("ABCD").setSubject("Genny Project").setAudience("Test JWT").addClaims(claims).signWith(key).compact();
	//	String jwt = SecurityUtils.createJwt("ABBCD", "Genny Project", "Test JWT", 100000, secret.getBytes(Utf8),claims);
//		log.info("JwtTest = "+jwt);
		
		   //This line will throw an exception if it is not a signed JWS (as expected)
		assert Jwts.parser().setSigningKey(key).parseClaimsJws(jws).getBody().getSubject().equals("Genny Project");
		
//	    Claims decodedClaims = Jwts.parser()         
//	       .setSigningKey(DatatypeConverter.parseBase64Binary(secret))
//	       .parseClaimsJws(jwt).getBody();
//	    log.info("ID: " + decodedClaims.getId());
//	    log.info("Subject: " + decodedClaims.getSubject());
//	    log.info("Issuer: " + decodedClaims.getIssuer());
//	    log.info("Expiration: " + decodedClaims.getExpiration());
//	    log.info("Username: "+ decodedClaims.get("preferred_username"));
//	    log.info("realm: "+ decodedClaims.get("realm"));	    
	}
}
