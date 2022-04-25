package life.genny.models;

import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlTransient;
import life.genny.qwandautils.KeycloakUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.qwandautils.SecurityUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

public class GennyToken implements Serializable {
  /** */
  private static final long serialVersionUID = 1L;

  protected static final Logger log =
      org.apache.logging.log4j.LogManager.getLogger(
          MethodHandles.lookup().lookupClass().getCanonicalName());

  String code;
  String userCode;
  String userUUID;
  String token;
  String username;
  Map<String, Object> adecodedTokenMap = null;
  String realm = null;
  String projectCode = null;
  Set<String> userRoles = new HashSet<String>();

  public GennyToken(final String token) {
    if ((token != null) && (!token.isEmpty())) {
      // Getting decoded token in Hash Map from QwandaUtils
      adecodedTokenMap = KeycloakUtils.getJsonMap(token);
      if (adecodedTokenMap == null) {

        log.error("Token is not able to be decoded in GennyToken ..");
      } else {

        // Extracting realm name from iss value

        String realm = null;
        
       
        if (adecodedTokenMap.get("iss") != null) {
          String[] issArray = adecodedTokenMap.get("iss").toString().split("/");
          realm = issArray[issArray.length - 1];
        } else if (adecodedTokenMap.get("azp") != null) {
          realm = (adecodedTokenMap.get("azp").toString()); // clientid
        }
        //				if ((realm.equals("alyson"))) {
        //					String[] issArray = adecodedTokenMap.get("iss").toString().split("/");
        //					realm = issArray[issArray.length-1];
        //					//realm = (adecodedTokenMap.get("aud").toString()); // handle non Keycloak 6+
        //				}

        // Adding realm name to the decoded token
        adecodedTokenMap.put("realm", realm);
        this.token = token;
        this.realm = realm;
        String uuid = adecodedTokenMap.get("sub").toString();
        this.username = (String) adecodedTokenMap.get("preferred_username");
        // String normalisedUsername = QwandaUtils.getNormalisedUsername(this.username);
        this.userUUID = "PER_" + this.getUuid().toUpperCase(); // normalisedUsername.toUpperCase();
        if ("service".equals(username)) {
          this.userCode = "PER_SERVICE";
        } else {
          this.userCode = userUUID; // "PER_" + normalisedUsername.toUpperCase();
          // //normalisedUsername.toUpperCase();
        }
        setupRoles();
      }

    } else {
      log.error("Token is null or zero length in GennyToken ..");
    }
  }

  public GennyToken(final String code, final String token) {

    this(token);
    this.code = code;
    if ("PER_SERVICE".equals(code)) {
      this.userCode = code;
    }
  }

  public GennyToken(
      final String code,
      final String id,
      final String issuer,
      final String subject,
      final long ttl,
      final String secret,
      final String realm,
      final String username,
      final String name,
      final String role) {

    this(
        code,
        id,
        issuer,
        subject,
        ttl,
        secret,
        realm,
        username,
        name,
        role,
        LocalDateTime.now().plusSeconds(24 * 60 * 60)); // 1 day expiry
  }

  public GennyToken(
      final String code,
      final String id,
      final String issuer,
      final String subject,
      final long ttl,
      final String secret,
      final String realm,
      final String username,
      final String name,
      final String role,
      final LocalDateTime expiryDateTime) {
    adecodedTokenMap = new HashMap<String, Object>();
    adecodedTokenMap.put("preferred_username", username);
    adecodedTokenMap.put("name", name);
    if (username.contains("@")) {
      adecodedTokenMap.put("email", username);
    } else {
      adecodedTokenMap.put("email", username + "@gmail.com");
    }
    String[] names = name.split(" ");
    adecodedTokenMap.put("given_name", names[0].trim());
    adecodedTokenMap.put("family_name", names[1].trim());
    adecodedTokenMap.put("jti", UUID.randomUUID().toString().substring(0, 20));
    adecodedTokenMap.put("sub", id);
    adecodedTokenMap.put("realm", realm);
    adecodedTokenMap.put("azp", realm);
    adecodedTokenMap.put("aud", realm);
    // adecodedTokenMap.put("realm_access", "{ \"roles\": [\"user\",\"" + role +
    // "\"] }");
    adecodedTokenMap.put("exp", expiryDateTime.atZone(ZoneId.of("UTC")).toEpochSecond());
    adecodedTokenMap.put("iat", LocalDateTime.now().atZone(ZoneId.of("UTC")).toEpochSecond());
    adecodedTokenMap.put("auth_time", LocalDateTime.now().atZone(ZoneId.of("UTC")).toEpochSecond());
    adecodedTokenMap.put(
        "session_state", UUID.randomUUID().toString().substring(0, 32)); // TODO set size ot same
    // as keycloak

    userRoles = new HashSet<String>();
    //		  "realm_access": {
    //		    "roles": [
    //		      "test",
    //		      "dev",
    //		      "offline_access",
    //		      "admin",
    //		      "uma_authorization",
    //		      "user",
    //		      "supervisor"
    //		    ]
    //		  },

    ArrayJson rj = new ArrayJson();
    userRoles.add("user");
    rj.roles.add("user");
    String[] roles = role.split(",:;");
    for (String r : roles) {
      userRoles.add(r);
      rj.roles.add(r);
    }

    adecodedTokenMap.put("realm_access", rj);

    String jwtToken = null;

    jwtToken = SecurityUtils.createJwt(id, issuer, subject, ttl, secret, adecodedTokenMap);
    token = jwtToken;
    this.realm = realm;
    if ("service".equals(username)) {
      this.userCode = "PER_SERVICE";
    } else {
      //		String normalisedUsername = QwandaUtils.getNormalisedUsername(id);
      //		if (normalisedUsername.toUpperCase().startsWith("PER_")) {
      //			this.userCode = normalisedUsername.toUpperCase();
      //		} else {
      this.userCode = "PER_" + id.toUpperCase();
      //		}
    }

    this.code = code;
    this.username = username;
    setupRoles();
  }

  public GennyToken(
      final String code,
      final String realm,
      final String username,
      final String name,
      final String role) {
    this(
        code,
        "ABBCD",
        "Genny Project",
        "Test JWT",
        100000,
        "IamASecret",
        realm,
        username,
        name,
        role,
        LocalDateTime.now().plusSeconds(24 * 60 * 60));
  }

  public GennyToken(
      final String uuid,
      final String code,
      final String realm,
      final String username,
      final String name,
      final String role,
      LocalDateTime expiryDateTime) {
    this(
        code,
        uuid,
        "Genny Project",
        "Test JWT",
        100000,
        "IamASecret",
        realm,
        username,
        name,
        role,
        expiryDateTime);
  }

  public GennyToken(
      final String code,
      final String realm,
      final String username,
      final String name,
      final String role,
      LocalDateTime expiryDateTime) {
    this(
        code,
        "ABBCD",
        "Genny Project",
        "Test JWT",
        100000,
        "IamASecret",
        realm,
        username,
        name,
        role,
        expiryDateTime);
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

  private void setupRoles() {
    String realm_accessStr = "";
    if (adecodedTokenMap.get("realm_access") == null) {
      userRoles.add("user");
    } else {
      realm_accessStr = adecodedTokenMap.get("realm_access").toString();
      Pattern p = Pattern.compile("(?<=\\[)([^\\]]+)(?=\\])");
      Matcher m = p.matcher(realm_accessStr);

      if (m.find()) {
        String[] roles = m.group(1).split(",");
        for (String role : roles) {
          userRoles.add((String) role.trim());
        }
        ;
      }
    }
  }

  public boolean hasRole(final String role) {
    return userRoles.contains(role);
  }

  @Override
  public String toString() {
    return getRealm() + ": " + getCode() + ": " + getUserCode() + ": " + this.userRoles;
  }

  public String getRealm() {
	  if (!StringUtils.isBlank(projectCode)) {
		  return projectCode;
	  }
		String clientId = adecodedTokenMap.get("azp").toString();

		if ((clientId.equals("internmatch"))||(clientId.equals("alyson"))) {
			return "internmatch";
		} 
		else {
			return clientId;
		}
  }

  public String getString(final String key) {
    return (String) adecodedTokenMap.get(key);
  }

  public String getCode() {
    return code;
  }

  public String getJti() {
    return getString("jti");
  }
  
  public String getJTI() {
	    return getString("jti");
	  }

  
  
  public String getProjectCode() {
	return projectCode;
}

public void setProjectCode(String projectCode) {
	this.projectCode = projectCode;
}

public String getUsername() {
    return this.username;
    // return getString("preferred_username");
  }

  public String getKeycloakUrl() {
    String fullUrl = getString("iss");
    URI uri;
    try {
      uri = new URI(fullUrl);
      String domain = uri.getHost();
      String proto = uri.getScheme();
      Integer port = uri.getPort();
      return proto + "://" + domain + ":" + port;
    } catch (URISyntaxException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return "http://keycloak.genny.life";
  }

  public String getClientCode() {
    return getString("aud");
  }

  public String getEmail() {
    return getString("email");
  }

  /** @return the userCode */
  public String getUserCode() {
    return userCode;
    // return "PER_"+this.userUUID.toUpperCase();
  }

  public String setUserCode(String userCode) {
    return this.userCode = userCode;
  }

  public String getUserUUID() {
    return userUUID;
  }

  @XmlTransient
  @Transient
  public LocalDateTime getAuthDateTime() {
    Long auth_timestamp = ((Number) adecodedTokenMap.get("auth_time")).longValue();
    LocalDateTime authTime =
        LocalDateTime.ofInstant(
            Instant.ofEpochSecond(auth_timestamp), TimeZone.getDefault().toZoneId());
    return authTime;
  }

  @XmlTransient
  @Transient
  public LocalDateTime getExpiryDateTime() {
    Long exp_timestamp = ((Number) adecodedTokenMap.get("exp")).longValue();
    LocalDateTime expTime =
        LocalDateTime.ofInstant(
            Instant.ofEpochSecond(exp_timestamp), TimeZone.getDefault().toZoneId());
    return expTime;
  }

  @XmlTransient
  @Transient
  public OffsetDateTime getExpiryDateTimeInUTC() {

    Long exp_timestamp = ((Number) adecodedTokenMap.get("exp")).longValue();
    LocalDateTime expTime =
        LocalDateTime.ofInstant(
            Instant.ofEpochSecond(exp_timestamp), TimeZone.getDefault().toZoneId());
    ZonedDateTime ldtZoned = expTime.atZone(ZoneId.systemDefault());
    ZonedDateTime utcZoned = ldtZoned.withZoneSameInstant(ZoneId.of("UTC"));

    return utcZoned.toOffsetDateTime();
  }

  @XmlTransient
  @Transient
  public Integer getSecondsUntilExpiry() {

    OffsetDateTime expiry = getExpiryDateTimeInUTC();
    LocalDateTime now = LocalDateTime.now(ZoneId.of("UTC"));
    Long diff = expiry.toEpochSecond() - now.toEpochSecond(ZoneOffset.UTC);
    return diff.intValue();
  }

  // JWT Issue DateTime
  @XmlTransient
  @Transient
  public LocalDateTime getiatDateTime() {
    Long iat_timestamp = ((Number) adecodedTokenMap.get("iat")).longValue();
    LocalDateTime iatTime =
        LocalDateTime.ofInstant(
            Instant.ofEpochSecond(iat_timestamp), TimeZone.getDefault().toZoneId());
    return iatTime;
  }

  // Unique token id
  @XmlTransient
  @Transient
  public String getUniqueId() {
    return (String) adecodedTokenMap.get("jti");
  }

  @XmlTransient
  @Transient
  public String getUuid() {
    String uuid = null;

    try {
      uuid = (String) adecodedTokenMap.get("sub");
    } catch (Exception e) {
      log.info("Not a valid user");
    }

    return uuid;
  }

  @XmlTransient
  @Transient
  public String getEmailUserCode() {
    String username = (String) adecodedTokenMap.get("preferred_username");
    String normalisedUsername = QwandaUtils.getNormalisedUsername(username);
    return "PER_" + normalisedUsername.toUpperCase();
  }

  @XmlTransient
  @Transient
  public Boolean checkUserCode(String userCode) {
    if (getUserCode().equals(userCode)) {
      return true;
    }
    if (getEmailUserCode().equals(userCode)) {
      return true;
    }
    return false;
  }

  /** @return the userRoles */
  public Set<String> getUserRoles() {
    return userRoles;
  }

  public String getRealmUserCode() {
    return getRealm() + "+" + getUserCode();
  }
}
