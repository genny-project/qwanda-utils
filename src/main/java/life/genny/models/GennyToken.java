package life.genny.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import life.genny.qwandautils.KeycloakUtils;
import life.genny.qwandautils.SecurityUtils;

public class GennyToken {
	
	String token;
	Map<String, Object> adecodedTokenMap = null;
	
	public GennyToken(final String id, final String issuer, final String subject, final long ttl, final String secret, final String realm, final String username, final String name, final String role)
	{
		adecodedTokenMap = new HashMap<String, Object>();
		adecodedTokenMap.put("preferred_username",username);
		adecodedTokenMap.put("name", name);
		adecodedTokenMap.put("realm", realm);
		adecodedTokenMap.put("azp", realm);
		adecodedTokenMap.put("realm_access", "[user," + role + "]");

		Set<String> auserRoles = KeycloakUtils.getRoleSet(adecodedTokenMap.get("realm_access").toString());

		String realm2 = adecodedTokenMap.get("realm").toString();
			adecodedTokenMap.put("realm", realm);

		String jwtToken = null;

		jwtToken = SecurityUtils.createJwt(id, issuer, subject, ttl, secret,
				adecodedTokenMap);
		token = jwtToken;
	}
	
	public GennyToken(final String realm, final String username, final String name, final String role)
	{
		this("ABBCD", "Genny Project", "Test JWT", 100000, "IamASecret",realm, username, name, role );
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public Map<String, Object> getAdecodedTokenMap() {
		return adecodedTokenMap;
	}

	public void setAdecodedTokenMap(Map<String, Object> adecodedTokenMap) {
		this.adecodedTokenMap = adecodedTokenMap;
	}

	@Override
	public String toString() {
		return "GennyToken [token=" + token + "]";
	}
	
	

}