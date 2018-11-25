package life.genny.qwandautils;

import org.apache.commons.lang3.StringUtils;
import java.util.Optional;

public class GennySettings {

    //Constants 
    public final static String LOCALHOST = "localhost";
    public final static String DEFAULT_CACHE_SERVER_NAME = "keisha-service";

    //Others 
	public static String defaultLocalIP = "10.123.123.123";
	public static String hostIP = System.getenv("HOSTIP") != null ? System.getenv("HOSTIP") : System.getenv("MYIP");   // remember to set up this local IP on the host
	public static String myIP = System.getenv("MYIP") != null ? System.getenv("MYIP") : System.getenv("HOSTIP");   // remember to set up this local IP on the host
	public static String cacheApiPort = System.getenv("CACHE_API_PORT") != null ? System.getenv("CACHE_API_PORT") : "8089";
	public static String apiPort = System.getenv("API_PORT") != null ? System.getenv("API_PORT") : "8088";
	public static String pontoonPort = System.getenv("PONTOON_PORT") != null ? System.getenv("PONTOON_PORT") : "8086";
	public static String webhookPort = System.getenv("WEBHOOK_PORT") != null ? System.getenv("WEBHOOK_PORT") : "9123";
	
	public static int  timeoutInSecs = 30;  // used in api timeout

	public static final String qwandaServiceUrl = System.getenv("REACT_APP_QWANDA_API_URL") != null ? System.getenv("REACT_APP_QWANDA_API_URL") : "http://"+hostIP+":8280";
	public static final String vertxUrl = System.getenv("REACT_APP_VERTX_URL") != null ? System.getenv("REACT_APP_VERTX_URL") :  "http://"+hostIP+":"+apiPort;
	public static final String pontoonUrl = System.getenv("PONTOON_URL") != null ? System.getenv("PONTOON_URL") :  "http://"+hostIP+":"+pontoonPort;
	public static final Boolean devMode = ("TRUE".equalsIgnoreCase(System.getenv("DEV_MODE"))||"TRUE".equalsIgnoreCase(System.getenv("GENNYDEV"))) ? true : false;
	public static final String projectUrl = System.getenv("PROJECT_URL");
	public final static String mainrealm = System.getenv("PROJECT_REALM") != null ? System.getenv("PROJECT_REALM") : "genny"; // UGLY
	public final static Boolean isRulesManager = "TRUE".equalsIgnoreCase(System.getenv("RULESMANAGER"));
	public final static Boolean isDdtHost = "TRUE".equalsIgnoreCase(System.getenv("DDTHOST"));

	public static final String ddtUrl = System.getenv("DDT_URL") == null ? ("http://" + hostIP + ":"+cacheApiPort)
			: System.getenv("DDT_URL");

	public static final String username = (System.getenv("USER") == null ? "GENNY" : System.getenv("USER"));

	public static final String defaultServiceKey = (System.getenv("ENV_SECURITY_KEY") == null ?  "WubbaLubbaDubDub" : System.getenv("ENV_SECURITY_KEY"));
	public static final String defaultServiceEncryptedPassword = (System.getenv("ENV_SERVICE_PASSWORD") == null ?  "vRO+tCumKcZ9XbPWDcAXpU7tcSltpNpktHcgzRkxj8o=" : System.getenv("ENV_SERVICE_PASSWORD"));
	public static final String defaultServicePassword = (System.getenv("DENV_SERVICE_UNENCRYPTED_PASSWORD") == null ?  "Wubba!Lubba!Dub!Dub!" : System.getenv("ENV_SERVICE_UNENCRYPTED_PASSWORD"));

	public static final String realmDir = System.getenv("REALM_DIR") != null ? System.getenv("REALM_DIR") : "./realm";
	public static final String rulesDir = System.getenv("RULES_DIR") != null ? System.getenv("RULES_DIR") : "/rules" ;  // TODO, docker focused

	public static final String startupWebHook = System.getenv("STARTUP_WEB_HOOK") != null ? System.getenv("STARTUP_WEB_HOOK") : "http://"+hostIP+":"+webhookPort+"/event/"+mainrealm ;  // trigger any startup webhook notification

	public static final String layoutCacheUrl = System.getenv("LAYOUT_CACHE_HOST") != null ? System.getenv("LAYOUT_CACHE_HOST") : "http://"+hostIP+":2223";

    public static final String cacheServerName;
    public static final Boolean isCacheServer;

    static{
        Optional<String> cacheServerNameOptional = Optional.ofNullable(System.getenv("CACHE_SERVER_NAME"));
        Optional<String> isCacheServerOptional = Optional.ofNullable(System.getenv("IS_CACHE_SERVER"));
        if(devMode){
            cacheServerName = cacheServerNameOptional.orElse(LOCALHOST);
        }else{
            cacheServerName = cacheServerNameOptional.orElse(DEFAULT_CACHE_SERVER_NAME);
        }
        isCacheServer = isCacheServerOptional.map(env -> Boolean.parseBoolean(env)).orElse(false);
    }

	public static String dynamicRealm()
	{
		return dynamicRealm(GennySettings.mainrealm);
	}

	public static String dynamicRealm(final String realm) {
		/* In Eclipse dev mode - Keycloak uses "genny" realm */
		if (devMode) {
			return "genny";
		}
		/* In local docker mode - Keycloak uses "genny" realm */
		if (defaultLocalIP.equals(hostIP)) {
			return "genny";
		}
		return realm;
	}


	public static String dynamicKey(final String realm)
	{
		String envSecurityKey = System.getenv("ENV_SECURITY_KEY"+"_"+realm.toUpperCase());
		if (envSecurityKey==null) {
			return defaultServiceKey;
		} else {
			return envSecurityKey;
		}
	}

	public static String dynamicEncryptedPassword(final String realm)
	{
		String envServiceEncryptedPassword = System.getenv("ENV_SERVICE_PASSWORD"+"_"+realm.toUpperCase());
		if (envServiceEncryptedPassword==null) {
			return defaultServiceEncryptedPassword;
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
		if (GennySettings.devMode) {
			password = GennySettings.defaultServicePassword;
		}
		return password;
	}
}
