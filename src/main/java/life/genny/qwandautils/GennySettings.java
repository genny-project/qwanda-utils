package life.genny.qwandautils;

import org.apache.commons.lang3.StringUtils;
import java.util.Optional;

public class GennySettings {

	public static String defaultLocalIP = System.getenv("DEFAULT_LOCAL_IP") != null ? System.getenv("DEFAULT_LOCAL_IP") :"10.123.123.123";


    //Constants 
    public final static String LOCALHOST = "localhost";
    public final static String DEFAULT_CACHE_SERVER_NAME = "bridge-service";
    public final static String GENNY_REALM = "jenny"; //deliberatly not genny

	public static int ACCESS_TOKEN_EXPIRY_LIMIT_SECONDS = 60;

	public static String hostIP = System.getenv("HOSTIP") != null ? System.getenv("HOSTIP") : System.getenv("MYIP");   // remember to set up this local IP on the host
	public static String myIP = System.getenv("MYIP") != null ? System.getenv("MYIP") : System.getenv("HOSTIP");   // remember to set up this local IP on the host
	public static String cacheApiPort = System.getenv("CACHE_API_PORT") != null ? System.getenv("CACHE_API_PORT") : "8089";
	public static String apiPort = System.getenv("API_PORT") != null ? System.getenv("API_PORT") : "8088";
	public static String pontoonPort = System.getenv("PONTOON_PORT") != null ? System.getenv("PONTOON_PORT") : "8086";
	public static String webhookPort = System.getenv("WEBHOOK_PORT") != null ? System.getenv("WEBHOOK_PORT") : "9123";
	
	public static int  timeoutInSecs = 30;  // used in api timeout

	public static final String projectUrl = System.getenv("PROJECT_URL");

	public static final String qwandaServiceUrl = System.getenv("REACT_APP_QWANDA_API_URL") != null ? System.getenv("REACT_APP_QWANDA_API_URL") : projectUrl;
	public static final String vertxUrl = System.getenv("REACT_APP_VERTX_URL") != null ? System.getenv("REACT_APP_VERTX_URL") :  "http://"+hostIP+":"+apiPort;
	public static final String bridgeServiceUrl = System.getenv("BRIDGE_SERVICE_API") != null ? System.getenv("BRIDGE_SERVICE_API") :  "http://alyson7.genny.life/api/service";
	public static final String pontoonUrl = System.getenv("PONTOON_URL") != null ? System.getenv("PONTOON_URL") :  "http://"+hostIP+":"+pontoonPort;
	public static final Boolean devMode = ("TRUE".equalsIgnoreCase(System.getenv("DEV_MODE"))||"TRUE".equalsIgnoreCase(System.getenv("GENNYDEV"))) ? true : false;
	public static final Boolean miniKubeMode = "TRUE".equalsIgnoreCase(System.getenv("MINIKUBE_MODE"));
	public static final Boolean zipMode = ("TRUE".equalsIgnoreCase(System.getenv("ZIP_MODE"))) ? true : false;

	public static final Boolean multiBridgeMode = ("TRUE".equalsIgnoreCase(System.getenv("MULTI_BRIDGE_MODE"))) ? true : false;
	
	// 2^19-1 = 524287 2^23-1=8388607
	public static final Integer zipMinimumThresholdBytes = System.getenv("ZIP_MIN_THRESHOLD_BYTES")==null?8388607:(Integer.parseInt(System.getenv("ZIP_MIN_THRESHOLD_BYTES")));
	public final static String mainrealm = System.getenv("PROJECT_REALM") != null ? System.getenv("PROJECT_REALM") : "genny"; // UGLY
	public final static Boolean isRulesManager = "TRUE".equalsIgnoreCase(System.getenv("RULESMANAGER"));
	public final static Boolean isDdtHost = "TRUE".equalsIgnoreCase(System.getenv("DDTHOST"));
	public final static Boolean forceEventBusApi = "TRUE".equalsIgnoreCase(System.getenv("FORCE_EVENTBUS_USE_API"));
	public final static Boolean forceCacheApi = "TRUE".equalsIgnoreCase(System.getenv("FORCE_CACHE_USE_API"));	
	public final static Boolean disableLayoutLoading = "TRUE".equalsIgnoreCase(System.getenv("DISABLE_LAYOUT_LOADING"));
	public final static Boolean enableSlackSending = "TRUE".equalsIgnoreCase(System.getenv("ENABLE_SLACK_SENDING"));
	public final static Boolean loadDdtInStartup = "TRUE".equalsIgnoreCase(System.getenv("LOAD_DDT_IN_STARTUP"));
	public final static Boolean skipGoogleDocInStartup = "TRUE".equalsIgnoreCase(System.getenv("SKIP_GOOGLE_DOC_IN_STARTUP"));
	public final static Boolean skipGithubInStartup = "TRUE".equalsIgnoreCase(System.getenv("SKIP_GITHUB_IN_STARTUP"));
	public final static String  githubLayoutsUrl = System.getenv("GITHUB_LAYOUTS_URL") == null ? ("http://github.com/genny-project/layouts.git")
			: System.getenv("GITHUB_LAYOUTS_URL");
	
	public final static Boolean detectRuleChanges = "TRUE".equalsIgnoreCase(System.getenv("DETECT_RULE_CHANGES"));
	public final static Boolean hideRuleStates = "TRUE".equalsIgnoreCase(System.getenv("HIDE_RULE_STATES"));
	public static final String ddtUrl = System.getenv("DDT_URL") == null ? ("http://" + hostIP + ":"+cacheApiPort)
			: projectUrl;

	public static final String username = System.getenv("USER") == null ? "GENNY" : System.getenv("USER");

	public static final String defaultServiceKey = System.getenv("ENV_SECURITY_KEY") == null ?  "WubbaLubbaDubDub" : System.getenv("ENV_SECURITY_KEY");
	public static final String defaultServiceEncryptedPassword = System.getenv("ENV_SERVICE_PASSWORD") == null ?  "vRO+tCumKcZ9XbPWDcAXpU7tcSltpNpktHcgzRkxj8o=" : System.getenv("ENV_SERVICE_PASSWORD");
	public static final String defaultServicePassword = System.getenv("DENV_SERVICE_UNENCRYPTED_PASSWORD") == null ?  "Wubba!Lubba!Dub!Dub!" : System.getenv("ENV_SERVICE_UNENCRYPTED_PASSWORD");

	public static final String realmDir = System.getenv("REALM_DIR") != null ? System.getenv("REALM_DIR") : "./realm" ;
	public static final String rulesDir = System.getenv("RULES_DIR") != null ? System.getenv("RULES_DIR") : "/rules" ;  // TODO, docker focused

	public static final String startupWebHook = System.getenv("STARTUP_WEB_HOOK") != null ? System.getenv("STARTUP_WEB_HOOK") : "http://"+hostIP+":"+webhookPort+"/event/"+mainrealm ;  // trigger any startup webhook notification

	public static final String layoutCacheUrl = System.getenv("LAYOUT_CACHE_HOST") != null ? System.getenv("LAYOUT_CACHE_HOST") : "http://"+hostIP+":2223";

    public static final String cacheServerName;
    public static final Boolean isCacheServer;
    public static final String KEYCLOAK_JSON = "keycloak.json";

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
	  
	  return GennySettings.mainrealm;
		/* In Eclipse dev mode - Keycloak uses "genny" realm */
		//if (devMode) {
		//	return "genny";
		//}
		/* In local docker mode - Keycloak uses "genny" realm */
		//if (defaultLocalIP.equals(hostIP)) {
		//	return "genny";
		//}
		//return realm;
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
		if (GennySettings.devMode || GennySettings.miniKubeMode || GennySettings.defaultLocalIP.equals(GennySettings.hostIP)) {
			password = GennySettings.defaultServicePassword;
		}
		return password;
	}
}
