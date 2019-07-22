package life.genny.models;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;

import org.json.JSONArray;
import org.json.JSONObject;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import life.genny.qwandautils.KeycloakUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.qwandautils.SecurityUtils;

public class GennyToken implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String code;
	String userCode;
	String token;
	Map<String, Object> adecodedTokenMap = null;
	String realm = null;
	Set<String> userRoles = new HashSet<String>();

	public GennyToken(final String token) {
		if ((token != null) && (!token.isEmpty())) {
			// Getting decoded token in Hash Map from QwandaUtils
			adecodedTokenMap = KeycloakUtils.getJsonMap(token);

			// Extracting realm name from iss value
			final String realm = (adecodedTokenMap.get("aud").toString());
			// Adding realm name to the decoded token
			adecodedTokenMap.put("realm", realm);
			this.token = token;
			this.realm = realm;
			String username = (String)adecodedTokenMap.get("preferred_username");
			String normalisedUsername = QwandaUtils.getNormalisedUsername(username);
			this.userCode = "PER_"+normalisedUsername.toUpperCase();

			setupRoles();
			
			
		}
		
	}
	
	
	
	public GennyToken(final String code,final String token) {

			this(token);
			this.code = code;
	}

	public GennyToken(final String code, final String id, final String issuer, final String subject, final long ttl, final String secret,
			final String realm, final String username, final String name, final String role) {
		adecodedTokenMap = new HashMap<String, Object>();
		adecodedTokenMap.put("preferred_username", username);
		adecodedTokenMap.put("name", name);
		adecodedTokenMap.put("realm", realm);
		adecodedTokenMap.put("azp", realm);
		adecodedTokenMap.put("aud", realm);
		adecodedTokenMap.put("realm_access", "[user," + role + "]");
		adecodedTokenMap.put("session_state", UUID.randomUUID().toString().substring(0, 32)); // TODO set size ot same as keycloak
		

		userRoles = new HashSet<String>();
		userRoles.add("user");
		userRoles.add(role);

		adecodedTokenMap.put("realm", realm);

		String jwtToken = null;

		jwtToken = SecurityUtils.createJwt(id, issuer, subject, ttl, secret, adecodedTokenMap);
		token = jwtToken;
		this.realm = realm;
		String normalisedUsername = QwandaUtils.getNormalisedUsername(username);
		this.userCode = "PER_"+normalisedUsername.toUpperCase();

		this.code = code;
		setupRoles();
	}

	public GennyToken(final String code,final String realm, final String username, final String name, final String role) {
		this(code,"ABBCD", "Genny Project", "Test JWT", 100000, "IamASecret", realm, username, name, role);
	}

	public String getToken() {
		return token;
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
		return getRealm()+": "+getCode()+": "+getUserCode()+": "+this.userRoles;
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

	public String getSessionCode()
	{
		return getString("session_state");
	}
	
	public String getClientCode()
	{
		return getString("aud");
	}

	/**
	 * @return the userCode
	 */
	public String getUserCode() {
		return userCode;
	}

	@XmlTransient
	@Transient
	public LocalDateTime getAuthDateTime()
	{
		long auth_timestamp = (long)adecodedTokenMap.get("auth_time");
		LocalDateTime authTime =
			       LocalDateTime.ofInstant(Instant.ofEpochSecond(auth_timestamp),
			                               TimeZone.getDefault().toZoneId());
		return authTime;
	}
	
	@XmlTransient
	@Transient
	public LocalDateTime getExpiryDateTime()
	{
		long exp_timestamp = (long)adecodedTokenMap.get("exp");
		LocalDateTime expTime =
			       LocalDateTime.ofInstant(Instant.ofEpochSecond(exp_timestamp),
			                               TimeZone.getDefault().toZoneId());
		return expTime;
	}
	
	
	// JWT Issue DateTime
	@XmlTransient
	@Transient
	public LocalDateTime getiatDateTime()
	{
		long iat_timestamp = (long)adecodedTokenMap.get("iat");
		LocalDateTime iatTime =
			       LocalDateTime.ofInstant(Instant.ofEpochSecond(iat_timestamp),
			                               TimeZone.getDefault().toZoneId());
		return iatTime;
	}

	
	// Unique token  id
	@XmlTransient
	@Transient
	public String getUniqueId()
	{
		return  (String)adecodedTokenMap.get("jti");
	}


	

}