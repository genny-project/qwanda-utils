package life.genny;

import java.time.LocalDateTime;

import org.junit.Test;

import life.genny.models.GennyToken;


public class GennyTokenTest {

	
@Test
public void gennyTokenTest()
{
	GennyToken tokenUser = createGennyToken("ABCDEFGH","internmatch", "adam.crow@gada.io", "Adam Crow", "intern");
	GennyToken tokenSupervisor = createGennyToken("BCDEFGSHS","internmatch", "kanika.gulati@gada.io", "Kanika Gulati", "supervisor");
	System.out.println(tokenUser.getToken());
	System.out.println(tokenSupervisor.getToken());


}
	
public GennyToken createGennyToken(final String uuid, final String realm, String username, String name, String role)
{
	return createGennyToken(uuid,realm, username, name, role,24*60*60);
}



public  GennyToken createGennyToken(final String realm, String username, String name, String role)
{
	return createGennyToken(realm, username, name, role,24*60*60);
}

public  GennyToken createGennyToken(final String realm, String username, String name, String role, long expirysecs)
{
	String normalisedUsername = null;
	if (!username.startsWith("PER_")) {
		normalisedUsername = "PER_"+username.toUpperCase();
	} else {
		normalisedUsername = username.toUpperCase();
	}
	LocalDateTime now = LocalDateTime.now();
	LocalDateTime expiryTime = now.plusSeconds(expirysecs);
	GennyToken gennyToken = new GennyToken(normalisedUsername,realm,username,name,role,expiryTime);
	return gennyToken;
}			
public  GennyToken createGennyToken(String uuid,final String realm, String username, String name, String role, long expirysecs)
{
	String normalisedUsername = null;
	if (!username.startsWith("PER_")) {
		normalisedUsername = "PER_"+username.toUpperCase();
	} else {
		normalisedUsername = username.toUpperCase();
	}
	LocalDateTime now = LocalDateTime.now();
	LocalDateTime expiryTime = now.plusSeconds(expirysecs);
	GennyToken gennyToken = new GennyToken(uuid,normalisedUsername,realm,username,name,role,expiryTime);
	return gennyToken;
}		

}
