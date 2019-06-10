package life.genny.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import life.genny.qwandautils.KeycloakUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.qwandautils.SecurityUtils;

public class GennyToken {
	String code;
	String token;
	Map<String, Object> adecodedTokenMap = null;
	String realm = null;
	Set<String> userRoles = new HashSet<String>();

	public GennyToken(final String token) {
		if ((token != null) && (!token.isEmpty())) {
			// Getting decoded token in Hash Map from QwandaUtils
			adecodedTokenMap = KeycloakUtils.getJsonMap(token);

			// Extracting realm name from iss value
			final String realm = (adecodedTokenMap.get("iss").toString()
					.substring(adecodedTokenMap.get("iss").toString().lastIndexOf("/") + 1));
			// Adding realm name to the decoded token
			adecodedTokenMap.put("realm", realm);
			this.token = token;
			this.realm = realm;
			
			String username = (String)adecodedTokenMap.get("preferred_username");
			String normalisedUsername = QwandaUtils.getNormalisedUsername(username);
			this.code = "PER_"+normalisedUsername.toUpperCase();
			setupRoles();
		}

	}
	
	
	
	public GennyToken(final String code,final String token) {
		if ((token != null) && (!token.isEmpty())) {
			// Getting decoded token in Hash Map from QwandaUtils
			adecodedTokenMap = KeycloakUtils.getJsonMap(token);

			// Extracting realm name from iss value
			final String realm = (adecodedTokenMap.get("iss").toString()
					.substring(adecodedTokenMap.get("iss").toString().lastIndexOf("/") + 1));
			// Adding realm name to the decoded token
			adecodedTokenMap.put("realm", realm);
			this.token = token;
			this.realm = realm;
			this.code = code;
			setupRoles();
			
			
		}

	}

	public GennyToken(final String code, final String id, final String issuer, final String subject, final long ttl, final String secret,
			final String realm, final String username, final String name, final String role) {
		adecodedTokenMap = new HashMap<String, Object>();
		adecodedTokenMap.put("preferred_username", username);
		adecodedTokenMap.put("name", name);
		adecodedTokenMap.put("realm", realm);
		adecodedTokenMap.put("azp", realm);
		adecodedTokenMap.put("realm_access", "[user," + role + "]");

		userRoles = new HashSet<String>();
		userRoles.add("user");
		userRoles.add(role);

		adecodedTokenMap.put("realm", realm);

		String jwtToken = null;

		jwtToken = SecurityUtils.createJwt(id, issuer, subject, ttl, secret, adecodedTokenMap);
		token = jwtToken;
		this.realm = realm;
		this.code = code;
		setupRoles();
	}

	public GennyToken(final String code,final String realm, final String username, final String name, final String role) {
		this(code,"ABBCD", "Genny Project", "Test JWT", 100000, "IamASecret", realm, username, name, role);
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

	private void setupRoles()
	{
		String realm_accessStr = adecodedTokenMap.get("realm_access").toString();
		Pattern p = Pattern.compile("(?<=\\[)([^\\]]+)(?=\\])");
		Matcher m = p.matcher(realm_accessStr);

		if (m.find()) {
			String[] roles = m.group(1).split(",");
			for (String role : roles) {userRoles.add((String)role.trim()); };
		}


	}
	
	public boolean hasRole(final String role)
	{
		return userRoles.contains(role);
	}
	
	@Override
	public String toString() {
		return "GennyToken [token=" + token + "]";
	}
	
	public String getRealm()
	{
		return realm;
	}
	
	public String getString(final String key)
	{
		return (String)adecodedTokenMap.get(key);
	}
	
	public String getCode()
	{
		return code;
	}

}