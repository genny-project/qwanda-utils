package life.genny;

import org.junit.Test;

import io.vertx.core.json.JsonObject;
import life.genny.qwandautils.KeycloakUtils;

public class KeycloakTokenTest {

	
	@Test
	public void keycloakTokenTest()
	{
		String keycloakUrl = System.getenv("KEYCLOAKURL");
		String clientId = "internmatch";
		
		String secret = System.getenv("CLIENT_SECRET");
		//secret = null;
		String uuid = "5a666e64-021f-48ce-8111-be3d66901f9c";
		String servicePassword = System.getenv("SERVICE_PASSWORD");
		String adminPassword = System.getenv("KEYCLOAK_PASSWORD");
		String realm = "internmatch";
		try {
			String accessToken = KeycloakUtils.getAccessToken(keycloakUrl, "internmatch", "internmatch", secret,
					"service", servicePassword);

//    	String userTokenStr = KeycloakUtils.getUserToken(keycloakUrl,uuid, serviceTokenStr, "internmatch");
//    	System.out.println(userTokenStr);
//    			    	String accessToken = null;
//			accessToken = KeycloakUtils.getAccessToken(keycloakUrl, "master",
//			 "admin-cli", null, "admin",
//			 System.getenv("KEYCLOAK_PASSWORD"));

			 String url = keycloakUrl + "/auth/admin/realms/" + realm + "/users/" + uuid;
			 String result = KeycloakUtils.sendGET(url,accessToken);

			 JsonObject userJson = new JsonObject(result);

			 String username = userJson.getString("username");

			String exchangedToken =accessToken;
			
			
			String userToken = KeycloakUtils.getImpersonatedToken(keycloakUrl,
					realm,uuid, accessToken);
			System.out.println("IMPERSONATE 1 "+userToken);
			userToken = KeycloakUtils.getImpersonatedToken(keycloakUrl,
			realm,clientId,secret, uuid, accessToken);

			System.out.println("IMPERSONATE 2 "+userToken);

		System.out.println("Keylcoak Token text");
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
}
