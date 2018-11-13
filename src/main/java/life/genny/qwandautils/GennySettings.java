package life.genny.qwandautils;

import org.apache.commons.lang3.StringUtils;

public class GennySettings {
  
	public static final String DEFAULT_LOCAL_IP = "10.123.123.123";
	public static final String HOSTIP = System.getenv("HOSTIP") != null ? System.getenv("HOSTIP") : System.getenv("MYIP");   // remember to set up this local IP on the host
	public static final String MYIP = System.getenv("MYIP") != null ? System.getenv("MYIP") : System.getenv("HOSTIP");   // remember to set up this local IP on the host
	public static final String CACHE_API_PORT = System.getenv("CACHE_API_PORT") != null ? System.getenv("CACHE_API_PORT") : "8089";
	public static final String API_PORT = System.getenv("API_PORT") != null ? System.getenv("API_PORT") : "8088";
	public static final String PONTOON_PORT = System.getenv("PONTOON_PORT") != null ? System.getenv("PONTOON_PORT") : "8086";
	public static final String WEBHOOK_PORT = System.getenv("WEBHOOK_PORT") != null ? System.getenv("WEBHOOK_PORT") : "9123";
	
	public static final int  TIMEOUT_IN_SECONDS = 30;  // used in api timeout

	public static final String QWANDA_SERVICE_URL = System.getenv("REACT_APP_QWANDA_API_URL") != null ? System.getenv("REACT_APP_QWANDA_API_URL") : "http://"+HOSTIP+":8280";
	public static final String VERTX_URL = System.getenv("REACT_APP_VERTX_URL") != null ? System.getenv("REACT_APP_VERTX_URL") :  "http://"+HOSTIP+":"+API_PORT;
	public static final String PONTOON_URL = System.getenv("PONTOON_URL") != null ? System.getenv("PONTOON_URL") :  "http://"+HOSTIP+":"+PONTOON_PORT;
	public static final Boolean DEV_MODE = "TRUE".equalsIgnoreCase(System.getenv("DEV_MODE"))||"TRUE".equalsIgnoreCase(System.getenv("GENNYDEV")) ? true : false;
	public static final String PROJECT_URL = System.getenv("PROJECT_URL");
	public static final String PROJECT_REALM = System.getenv("PROJECT_REALM") != null ? System.getenv("PROJECT_REALM") : "genny"; // UGLY
	public static final Boolean IS_RULES_MANAGER = "TRUE".equalsIgnoreCase(System.getenv("RULESMANAGER"));
	public static final Boolean IS_DDTHOST = "TRUE".equalsIgnoreCase(System.getenv("DDTHOST"));

	public static final String DDT_URL = System.getenv("DDT_URL") == null ? "http://" + HOSTIP + ":"+CACHE_API_PORT
			: System.getenv("DDT_URL");

	public static final String USERNAME = System.getenv("USER") == null ? "GENNY" : System.getenv("USER");

	public static final String DEFAULT_SERVICE_KEY = System.getenv("ENV_SECURITY_KEY") == null ?  "WubbaLubbaDubDub" : System.getenv("ENV_SECURITY_KEY");
	public static final String DEFAULT_SERVICE_ENCRYPTED_PASSWORD = System.getenv("ENV_SERVICE_PASSWORD") == null ?  "vRO+tCumKcZ9XbPWDcAXpU7tcSltpNpktHcgzRkxj8o=" : System.getenv("ENV_SERVICE_PASSWORD");
	public static final String DEFAULT_SERVICE_PASSWORD = System.getenv("DENV_SERVICE_UNENCRYPTED_PASSWORD") == null ?  "Wubba!Lubba!Dub!Dub!" : System.getenv("ENV_SERVICE_UNENCRYPTED_PASSWORD");

	public static final String REALM_DIR = System.getenv("REALM_DIR") != null ? System.getenv("REALM_DIR") : "./realm";
	public static final String RULES_DIR = System.getenv("RULES_DIR") != null ? System.getenv("RULES_DIR") : "/rules" ;  

	public static final String STARTUP_WEB_HOOK = System.getenv("STARTUP_WEB_HOOK") != null ? System.getenv("STARTUP_WEB_HOOK") : "http://"+HOSTIP+":"+WEBHOOK_PORT+"/event/"+PROJECT_REALM ;  // trigger any startup webhook notification

	public static final String LAYOUT_CACHE_URL = System.getenv("LAYOUT_CACHE_HOST") != null ? System.getenv("LAYOUT_CACHE_HOST") : "http://"+HOSTIP+":2223";

	private GennySettings() {}
	public static String dynamicRealm()
	{
		return dynamicRealm(GennySettings.PROJECT_REALM);
	}

	public static String dynamicRealm(final String realm) {
		/* In Eclipse dev mode - Keycloak uses "genny" realm */
		if (DEV_MODE) {
			return "genny";
		}
		/* In local docker mode - Keycloak uses "genny" realm */
		if (DEFAULT_LOCAL_IP.equals(HOSTIP)) {
			return "genny";
		}
		return realm;
	}


	public static String dynamicKey(final String realm)
	{
		String envSecurityKey = System.getenv("ENV_SECURITY_KEY"+"_"+realm.toUpperCase());
		if (envSecurityKey==null) {
			return DEFAULT_SERVICE_KEY;
		} else {
			return envSecurityKey;
		}
	}

	public static String dynamicEncryptedPassword(final String realm)
	{
		String envServiceEncryptedPassword = System.getenv("ENV_SERVICE_PASSWORD"+"_"+realm.toUpperCase());
		if (envServiceEncryptedPassword==null) {
			return DEFAULT_SERVICE_ENCRYPTED_PASSWORD;
		} else {
			return envServiceEncryptedPassword;
		}
	}

	public static String dynamicInitVector(final String realm)
	{
		String initVector = "PRJ_" + realm.toUpperCase();
		initVector = StringUtils.rightPad(initVector, 16, '*');
		return initVector;
	}

	public static String dynamicPassword(final String realm)
	{
		String password = SecurityUtils.decrypt(dynamicKey(realm), dynamicInitVector(realm), dynamicEncryptedPassword(realm));
		if (GennySettings.DEV_MODE) {
			password = GennySettings.DEFAULT_SERVICE_PASSWORD;
		}
		return password;
	}
}
