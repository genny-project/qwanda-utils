package life.genny;

import io.vertx.core.json.JsonObject;
import life.genny.qwandautils.KeycloakUtils;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class KeycloakTokenTest {

	@Test
	public void JsonKeycloakTest()
	{
		String keycloakJson = "{"+
			  "\"realm\": \"internmatch\","+
			  "\"auth-server-url\": \"https://keycloak.gada.io/auth\","+
			  "\"ssl-required\": \"external\","+
			  "\"resource\": \"internmatch\","+
			  "\"credentials\": {"+
			    "\"secret\": \"dc7d0960-2e1d-4a78-9eef-77472066dbd3\""+ 
			  "},"+
			  "\"policy-enforcer\": {}"+
			"}";
		
		JsonObject json = new JsonObject(keycloakJson);
		JsonObject credentials = json.getJsonObject("credentials");
		String secret = credentials.getString("secret");
		System.out.println(secret);
		
	}
	
	@Test
	public void keycloakTokenTest()
	{
		String keycloakUrl = System.getenv("KEYCLOAKURL");
		String clientId = "internmatch";
		
		String secret = System.getenv("CLIENT_SECRET");
		String master_admin_secret = "9e6dedaf-46f1-4f79-8a76-4bd2a942fb2f";
		String uuid = "5a666e64-021f-48ce-8111-be3d66901f9c";  // christopher.pyke@gada.io
		String uuid2 = "21a6da4a-1e35-4352-be65-70dcdb8494d5";
		String uuid3 = "a6445e32-a037-4e50-94a8-e8e1cee99905";
		String uuid5 = "66b4732c-2c73-4115-9c2c-da4bdfeb7176"; //adamcrow63+1111@gmail.com
		
		String servicePassword = System.getenv("SERVICE_PASSWORD");
		String adminPassword = System.getenv("KEYCLOAK_PASSWORD");
		String realm = "internmatch";
		try {
			String accessToken = KeycloakUtils.getAccessToken(keycloakUrl, "internmatch", "internmatch",secret,
					"service", servicePassword);

//    	String userTokenStr = KeycloakUtils.getUserToken(keycloakUrl,uuid, serviceTokenStr, "internmatch");
//    	System.out.println(userTokenStr);
//    			    	String accessToken = null;
//			accessToken = KeycloakUtils.getAccessToken(keycloakUrl, "master",
//			 "admin-cli", null, "admin",
//			 System.getenv("KEYCLOAK_PASSWORD"));

			 String url = keycloakUrl + "/admin/realms/" + realm + "/users/" + uuid;
			 String result = KeycloakUtils.sendGET(url,accessToken);

			 JsonObject userJson = new JsonObject(result);

			 String username = userJson.getString("username");

			String exchangedToken =accessToken;
			
			
			String userToken =null;//KeycloakUtils.getImpersonatedToken(keycloakUrl,
					//realm,uuid, accessToken);
		//	System.out.println("IMPERSONATE 1 "+userToken);
//			userToken = KeycloakUtils.getImpersonatedToken(keycloakUrl,
//			realm,clientId,secret, uuid, accessToken);

			System.out.println("IMPERSONATE 2 "+userToken);
			
			
			List<String> actions = new ArrayList<>();
			actions.add("UPDATE_PASSWORD");
			actions.add("VERIFY_EMAIL");
		/*	actions.add("UPDATE_PROFILE");*/
			//actions.add("CONFIGURE_TOTP");
		/*	actions.add("terms_and_conditions");*/
			
//			CloseableHttpClient httpclient = HttpClients.createDefault();
//			String urlResetPassword = keycloakUrl+"/auth/admin/realms/"+realm+"/users/"+uuid2+"/execute-actions-email?redirect_uri=https://internmatch-dev.gada.io&client_id=alyson&lifespan=600";
//			HttpPut putRequest = new HttpPut(urlResetPassword);
//			putRequest.addHeader("Authorization", "bearer "+exchangedToken);
//			putRequest.addHeader("content-type", MediaType.APPLICATION_JSON);
//			putRequest.setHeader("Accept", MediaType.APPLICATION_JSON);
//			String actionsArray = "[";
//			for (String action : actions) {
//				actionsArray += "\""+action+"\",";
//			}
//			actionsArray = actionsArray.substring(0,actionsArray.length()-1);
//			actionsArray += "]";
//			StringEntity jSonEntity = new StringEntity(actionsArray);
//			putRequest.setEntity(jSonEntity);
//			CloseableHttpResponse response2 = httpclient.execute(putRequest);
			
//			KeycloakUtils.executeActions(keycloakUrl,realm, clientId, secret, 600, uuid, "https://internmatch-dev.gada.io", actions, exchangedToken);
//			KeycloakUtils.executeActions(keycloakUrl,realm, clientId, secret, 600, uuid2, "https://internmatch-dev.gada.io", actions, exchangedToken);
//
			KeycloakUtils.executeActions(keycloakUrl,realm, clientId, null, 600, uuid, "https://internmatch-dev.gada.io/home/UVVFX0ZBS0VfUEFSRU5U/UVVFX0ZJTklTSF9JTlRFUk4=", actions, exchangedToken);
		//	KeycloakUtils.executeActions(keycloakUrl,realm, clientId, secret, 600, uuid3, "https://internmatch-dev.gada.io/home/UVVFX0ZBS0VfUEFSRU5U/UVVFX0ZJTklTSF9JTlRFUk4=", actions, exchangedToken);


		System.out.println("Keylcoak Token text");
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
}
