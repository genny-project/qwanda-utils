package life.genny;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.DatatypeConverter;

import org.junit.Test;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.SecurityUtils;

public class SecurityUtilsTest {
	@Test
	public void versionTest()
	{
		        String key        = GennySettings.DEFAULT_SERVICE_KEY; // 128 bit key
		        String initVector = "PRJ_GENNY*******"; // 16 bytes IV

		        String encrypted = SecurityUtils.encrypt(key, initVector, GennySettings.DEFAULT_SERVICE_PASSWORD);
		        System.out.println("["+encrypted+"]");;
		        System.out.println(SecurityUtils.decrypt(key, initVector, encrypted
		                ));
		        

	}
	
	@Test
	public void createJwtTest()
	{
		String secret = "IamAnApiSecret";
		Map<String,Object> claims = new HashMap<String, Object>();
		claims.put("preferred_username", "user1");
		claims.put("realm", "genny");
		String jwt = SecurityUtils.createJwt("ABBCD", "Genny Project", "Test JWT", 100000, secret,claims);
		System.out.println("JwtTest = "+jwt);
		
		   //This line will throw an exception if it is not a signed JWS (as expected)
	    Claims decodedClaims = Jwts.parser()         
	       .setSigningKey(DatatypeConverter.parseBase64Binary(secret))
	       .parseClaimsJws(jwt).getBody();
	    System.out.println("ID: " + decodedClaims.getId());
	    System.out.println("Subject: " + decodedClaims.getSubject());
	    System.out.println("Issuer: " + decodedClaims.getIssuer());
	    System.out.println("Expiration: " + decodedClaims.getExpiration());
	    System.out.println("Username: "+ decodedClaims.get("preferred_username"));
	    System.out.println("realm: "+ decodedClaims.get("realm"));	    
	}
}
