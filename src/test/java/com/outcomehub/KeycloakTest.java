package com.outcomehub;

import java.io.IOException;

import org.junit.Test;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessTokenResponse;

import com.outcomehub.qwandautils.KeycloakUtils;

public class KeycloakTest {

	@Test 
	// run the keycloak docker located in the root of the project runKeycloak.sh
	public void tokenTest() {
		try {
			AccessTokenResponse accessToken = KeycloakUtils.getAccessToken("http://10.64.0.6:8180",
					"wildfly-swarm-keycloak-example", "curl", "056b73c1-7078-411d-80ec-87d41c55c3b4", "user1", "password1");
			String tokenString = accessToken.getToken();
			System.out.println(tokenString);
	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
