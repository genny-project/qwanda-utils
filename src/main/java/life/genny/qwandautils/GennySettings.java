package life.genny.qwandautils;

import org.apache.commons.lang3.StringUtils;

import life.genny.utils.CommonUtils;

import java.util.Optional;

public class GennySettings {

	public static String defaultLocalIP = CommonUtils.getSystemEnv("DEFAULT_LOCAL_IP") != null ? CommonUtils.getSystemEnv("DEFAULT_LOCAL_IP") :"10.123.123.123";


	
    //Constants 
    public final static String LOCALHOST = "localhost";
    public final static String DEFAULT_CACHE_SERVER_NAME = "bridge-service";
    public final static String GENNY_REALM = "jenny"; //deliberatly not genny

	public static int ACCESS_TOKEN_EXPIRY_LIMIT_SECONDS = 60;

	public static int MAX_KEYCLOAK_USER_PER_CALL = 5000;
	

	public static String hostIP = CommonUtils.getSystemEnv("HOSTIP") != null ? CommonUtils.getSystemEnv("HOSTIP") : (CommonUtils.getSystemEnv("MYIP") != null ? CommonUtils.getSystemEnv("MYIP") : "alyson.genny.life");   // remember to set up this local IP on the host
	public static String myIP = CommonUtils.getSystemEnv("MYIP") != null ? CommonUtils.getSystemEnv("MYIP") : CommonUtils.getSystemEnv("HOSTIP");   // remember to set up this local IP on the host
	public static String cacheApiPort = CommonUtils.getSystemEnv("CACHE_API_PORT") != null ? CommonUtils.getSystemEnv("CACHE_API_PORT") : "8280";
	public static String apiPort = CommonUtils.getSystemEnv("API_PORT") != null ? CommonUtils.getSystemEnv("API_PORT") : "8088";
	public static String pontoonPort = CommonUtils.getSystemEnv("PONTOON_PORT") != null ? CommonUtils.getSystemEnv("PONTOON_PORT") : "8086";
	public static String webhookPort = CommonUtils.getSystemEnv("WEBHOOK_PORT") != null ? CommonUtils.getSystemEnv("WEBHOOK_PORT") : "9123";
	
	public static int  timeoutInSecs = 30;  // used in api timeout

	public static final String projectUrl = CommonUtils.getSystemEnv("PROJECT_URL")!=null?CommonUtils.getSystemEnv("PROJECT_URL"):"http://alyson7.genny.life";
	public static final String mediaProxyUrl = CommonUtils.getSystemEnv("MEDIA_PROXY_URL")!=null?CommonUtils.getSystemEnv("MEDIA_PROXY_URL"):(projectUrl+":9898/public");
	public static final String qwandaServiceUrl = CommonUtils.getSystemEnv("REACT_APP_QWANDA_API_URL") != null ? CommonUtils.getSystemEnv("REACT_APP_QWANDA_API_URL") : (projectUrl+":8280");
	public static final String vertxUrl = CommonUtils.getSystemEnv("REACT_APP_VERTX_URL") != null ? CommonUtils.getSystemEnv("REACT_APP_VERTX_URL") :  "http://"+hostIP+":"+apiPort;
	public static final String bridgeServiceUrl = CommonUtils.getSystemEnv("BRIDGE_SERVICE_API") != null ? CommonUtils.getSystemEnv("BRIDGE_SERVICE_API") :  projectUrl+"/api/service/commands";
	public static final String fyodorServiceUrl = CommonUtils.getSystemEnv("FYODOR_SERVICE_API") != null ? CommonUtils.getSystemEnv("FYODOR_SERVICE_API") : (projectUrl+":4242");
	public static final String scheduleServiceUrl = CommonUtils.getSystemEnv("SCHEDULE_SERVICE_API") != null ? CommonUtils.getSystemEnv("SCHEDULE_SERVICE_API") :  projectUrl+"/api/schedule";

	// FUCKING HACK
	public static final String remoteServerUrl = CommonUtils.getSystemEnv("REMOTE_API_URL") != null ? CommonUtils.getSystemEnv("REMOTE_API_URL") : "https://internmatch-interns.gada.io";


	public static final String pontoonUrl = CommonUtils.getSystemEnv("PONTOON_URL") != null ? CommonUtils.getSystemEnv("PONTOON_URL") :  "http://"+hostIP+":"+pontoonPort;
	public static final Boolean devMode = ("TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("DEV_MODE"))||"TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("GENNYDEV"))) ? true : false;
	public static final Boolean miniKubeMode = "TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("MINIKUBE_MODE"));
	public static final Boolean zipMode = ("TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("ZIP_MODE"))) ? true : false;
	public static final Boolean gzipMode = ("TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("MODE_GZIP"))) ? true : false;
	public static final Boolean gzip64Mode = ("TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("MODE_GZIP64"))) ? true : false;
	public static final Boolean multiBridgeMode = ("TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("MULTI_BRIDGE_MODE"))) ? true : false;
	public static final Boolean bulkPull = ("TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("BULKPULL"))) ? true : false;
	public static final Long pontoonMinimumThresholdBytes = CommonUtils.getSystemEnv("PONTOON_MIN_THRESHOLD_BYTES")==null?10000L:(Integer.parseInt(CommonUtils.getSystemEnv("PONTOON_MIN_THRESHOLD_BYTES")));	
	public static final Integer pontoonTimeout = CommonUtils.getSystemEnv("PONTOON_TIMEOUT")==null?43200:(Integer.parseInt(CommonUtils.getSystemEnv("PONTOON_TIMEOUT")));  // 12 hours
	
	// 2^19-1 = 524287 2^23-1=8388607
	public static final Integer zipMinimumThresholdBytes = CommonUtils.getSystemEnv("ZIP_MIN_THRESHOLD_BYTES")==null?8388607:(Integer.parseInt(CommonUtils.getSystemEnv("ZIP_MIN_THRESHOLD_BYTES")));
	public final static String  mainrealm = CommonUtils.getSystemEnv("PROJECT_REALM") != null ? CommonUtils.getSystemEnv("PROJECT_REALM") : "genny"; // UGLY
	public final static Boolean isRulesManager = "TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("RULESMANAGER"));
	public final static Boolean isDdtHost = "TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("DDTHOST"));
	public       static Boolean forceEventBusApi = CommonUtils.getSystemEnv("FORCE_EVENTBUS_USE_API")==null?false:"TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("FORCE_EVENTBUS_USE_API"));
	public final static Boolean noCache = CommonUtils.getSystemEnv("NO_CACHE")==null?false:"TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("NO_CACHE"));
	public       static Boolean forceCacheApi = CommonUtils.getSystemEnv("FORCE_CACHE_USE_API")==null?false:"TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("FORCE_CACHE_USE_API"));	
	public final static Boolean disableLayoutLoading = "TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("DISABLE_LAYOUT_LOADING"));
	public final static Boolean enableSlackSending = "TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("ENABLE_SLACK_SENDING"));
	public final static Boolean loadDdtInStartup = "TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("LOAD_DDT_IN_STARTUP"));
	public final static Boolean skipGoogleDocInStartup = "TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("SKIP_GOOGLE_DOC_IN_STARTUP"));
	public final static Boolean skipGithubInStartup = "TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("SKIP_GITHUB_IN_STARTUP"));
	public final static String  githubLayoutsUrl = CommonUtils.getSystemEnv("GITHUB_LAYOUTS_URL") == null ? ("http://github.com/genny-project/layouts.git")
			: CommonUtils.getSystemEnv("GITHUB_LAYOUTS_URL");
	// Load google sheets and process it during qwanda service startup
	public final static Boolean disableBatchLoading = CommonUtils.getSystemEnv("DISABLE_BATCH_LOADING") !=null ? "TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("DISABLE_BATCH_LOADING")):false;

	public final static Boolean isTestServer = "TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("IS_TEST_SERVER"));

	public final static Boolean logWorkflows = CommonUtils.getSystemEnv("LOG_WORKFLOWS") !=null ? "TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("LOG_WORKFLOWS")):true;	
	public final static Boolean useSingleton = CommonUtils.getSystemEnv("USE_SINGLETON") !=null ? "TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("USE_SINGLETON")):true;
	public final static Boolean useExecutor = CommonUtils.getSystemEnv("USE_EXECUTOR") !=null ? "TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("USE_EXECUTOR")):true;
	public final static Boolean useJMS = CommonUtils.getSystemEnv("USE_JMS") !=null ? "TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("USE_JMS")):false;
	public final static Boolean detectRuleChanges = CommonUtils.getSystemEnv("DETECT_RULE_CHANGES") !=null ?"TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("DETECT_RULE_CHANGES")):false;
	public final static Boolean persistRules = CommonUtils.getSystemEnv("PERSIST_RULES") !=null ?"TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("PERSIST_RULES")):false;
	public final static Boolean framesOnDemand= CommonUtils.getSystemEnv("FRAMES_ON_DEMAND") !=null ? "TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("FRAMES_ON_DEMAND")):true;
	public static final Boolean useApiRules = ("TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("USE_API_RULES"))) ? true : false;
	public static final Boolean useConcurrencyMsgs = ("TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("USE_CONCURRENCY_MSGS"))) ? true : false;
	public static final Boolean searchAlt = ("FALSE".equalsIgnoreCase(CommonUtils.getSystemEnv("SEARCH_ALT"))) ? false : true;
	public static final Boolean useEventQueue = ("FALSE".equalsIgnoreCase(CommonUtils.getSystemEnv("USE_EVENT_QUEUE"))) ? false : true;  // default to use

	
	public final static Boolean hideRuleStates = "TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("HIDE_RULE_STATES"));
	public static final String ddtUrl = CommonUtils.getSystemEnv("DDT_URL") == null ? ("http://" + hostIP + ":"+cacheApiPort): CommonUtils.getSystemEnv("DDT_URL");

	public final static String  gitProjectUrls = CommonUtils.getSystemEnv("GIT_PROJECT_URLS") == null ? ("https://github.com/genny-project/prj_genny.git")
			: CommonUtils.getSystemEnv("GIT_PROJECT_URLS"); // comma separated urls, in order of loading with subsequent overwriting

	public final static String gitPassword = CommonUtils.getSystemEnv("GIT_PASSWORD") ==null ? "":CommonUtils.getSystemEnv("GIT_PASSWORD");
	public final static String gitUsername = CommonUtils.getSystemEnv("GIT_USERNAME") ==null ? "git":CommonUtils.getSystemEnv("GIT_USERNAME");	
	public final static String gitRulesBranch = CommonUtils.getSystemEnv("GIT_RULES_BRANCH") ==null ? "v3.1.0":CommonUtils.getSystemEnv("GIT_RULES_BRANCH");	

	
	// This is public
	public final static String gennyPublicPassword = CommonUtils.getSystemEnv("PASSWORD") ==null ? "WelcomeToTheHub121!":CommonUtils.getSystemEnv("PASSWORD");
	public final static String gennyPublicUsername = CommonUtils.getSystemEnv("USERNAME") ==null ? "user1":CommonUtils.getSystemEnv("USERNAME");	
	public static final String username = CommonUtils.getSystemEnv("USER") == null ? "GENNY" : CommonUtils.getSystemEnv("USER"); 

	public static final String defaultServiceKey = CommonUtils.getSystemEnv("ENV_SECURITY_KEY") == null ?  "WubbaLubbaDubDub" : CommonUtils.getSystemEnv("ENV_SECURITY_KEY");
	public static final String defaultServiceEncryptedPassword = CommonUtils.getSystemEnv("ENV_SERVICE_PASSWORD") == null ?  "vRO+tCumKcZ9XbPWDcAXpU7tcSltpNpktHcgzRkxj8o=" : CommonUtils.getSystemEnv("ENV_SERVICE_PASSWORD");
	public static final String defaultServicePassword = CommonUtils.getSystemEnv("DENV_SERVICE_UNENCRYPTED_PASSWORD") == null ?  "Wubba!Lubba!Dub!Dub!" : CommonUtils.getSystemEnv("ENV_SERVICE_UNENCRYPTED_PASSWORD");

	public static final String realmDir = CommonUtils.getSystemEnv("REALM_DIR") != null ? CommonUtils.getSystemEnv("REALM_DIR") : "./realm" ;
	public static final String rulesDir = CommonUtils.getSystemEnv("RULES_DIR") != null ? CommonUtils.getSystemEnv("RULES_DIR") : "/rules" ;  // TODO, docker focused

	public static final String startupWebHook = CommonUtils.getSystemEnv("STARTUP_WEB_HOOK") != null ? CommonUtils.getSystemEnv("STARTUP_WEB_HOOK") : "http://"+hostIP+":"+webhookPort+"/event/"+mainrealm ;  // trigger any startup webhook notification

	public static final String layoutCacheUrl = CommonUtils.getSystemEnv("LAYOUT_CACHE_HOST") != null ? CommonUtils.getSystemEnv("LAYOUT_CACHE_HOST") : "http://"+hostIP+":2223";

    public static final String cacheServerName;
    public static final Boolean isCacheServer;
    public static final String KEYCLOAK_JSON = "keycloak.json";
    public static final String keycloakUrl = CommonUtils.getSystemEnv("KEYCLOAKURL") != null ? CommonUtils.getSystemEnv("KEYCLOAKURL") : "http://keycloak.genny.life";
    public static final Integer defaultPageSize  = CommonUtils.getSystemEnv("DEFAULT_PAGE_SIZE")==null?10:(Integer.parseInt(CommonUtils.getSystemEnv("DEFAULT_PAGE_SIZE")));
    public static final Integer defaultDropDownPageSize  = CommonUtils.getSystemEnv("DEFAULT_DROPDOWN_PAGE_SIZE")==null?25:(Integer.parseInt(CommonUtils.getSystemEnv("DEFAULT_DROPDOWN_PAGE_SIZE")));
    public static final Integer defaultBucketSize  = CommonUtils.getSystemEnv("DEFAULT_BUCKET_SIZE")==null?8:(Integer.parseInt(CommonUtils.getSystemEnv("DEFAULT_BUCKET_SIZE")));
    
    public static final String emailSmtpHost = CommonUtils.getSystemEnv("EMAIL_SMTP_HOST") != null ? CommonUtils.getSystemEnv("EMAIL_SMTP_HOST") : "http://keycloak.genny.life";
    public static final String emailSmtpUser = CommonUtils.getSystemEnv("EMAIL_SMTP_USER") != null ? CommonUtils.getSystemEnv("EMAIL_SMTP_USER") : "http://keycloak.genny.life";
    public static final String emailSmtpPassword = CommonUtils.getSystemEnv("EMAIL_SMTP_PASS") != null ? CommonUtils.getSystemEnv("EMAIL_SMTP_PASS") : "http://keycloak.genny.life";
    public static final String emailSmtpPort = CommonUtils.getSystemEnv("EMAIL_SMTP_PORT") != null ? CommonUtils.getSystemEnv("EMAIL_SMTP_PORT") : "http://keycloak.genny.life";
    public static final String emailSmtpStartTls = CommonUtils.getSystemEnv("EMAIL_SMTP_STARTTLS") != null ? CommonUtils.getSystemEnv("EMAIL_SMTP_STARTTLS") : "http://keycloak.genny.life";
    public static final String emailSmtpAuth = CommonUtils.getSystemEnv("EMAIL_SMTP_AUTH") != null ? CommonUtils.getSystemEnv("EMAIL_SMTP_HOST") : "http://keycloak.genny.life";   

    public static final String twilioAccountSid = CommonUtils.getSystemEnv("TWILIO_ACCOUNT_SID") != null ? CommonUtils.getSystemEnv("TWILIO_ACCOUNT_SID") : "TWILIO_ACCOUNT_SID"; 
    public static final String twilioAuthToken = CommonUtils.getSystemEnv("TWILIO_AUTH_TOKEN") != null ? CommonUtils.getSystemEnv("TWILIO_AUTH_TOKEN") : "TWILIO_AUTH_TOKEN";   
    public static final String twilioSenderMobile = CommonUtils.getSystemEnv("TWILIO_SENDER_MOBILE") != null ? CommonUtils.getSystemEnv("TWILIO_SENDER_MOBILE") : "TWILIO_SENDER_MOBILE";
	public static final Boolean CleanupTaskAndBeAttrForm = CommonUtils.getSystemEnv("CLEANUP_TASK_AND_BEATTRFORM") !=null ? "TRUE".equalsIgnoreCase(CommonUtils.getSystemEnv("CLEANUP_TASK_AND_BEATTRFORM")):true;
	// API POST timeout
	// 1 second by default
	public static final Integer apiPostTimeOut= CommonUtils.getSystemEnv("API_POST_TIMEOUT")==null?1:(Integer.parseInt(CommonUtils.getSystemEnv("API_POST_TIMEOUT")));
	// 3 times by default
	public static final Integer apiPostRetryTimes= CommonUtils.getSystemEnv("API_POST_RETRY_TIMES")==null?3:(Integer.parseInt(CommonUtils.getSystemEnv("API_POST_RETRY_TIMES")));


	static{
        Optional<String> cacheServerNameOptional = Optional.ofNullable(CommonUtils.getSystemEnv("CACHE_SERVER_NAME"));
        Optional<String> isCacheServerOptional = Optional.ofNullable(CommonUtils.getSystemEnv("IS_CACHE_SERVER"));
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
		String envSecurityKey = CommonUtils.getSystemEnv("ENV_SECURITY_KEY"+"_"+realm.toUpperCase());
		if (envSecurityKey==null) {
			return defaultServiceKey;
		} else {
			return envSecurityKey;
		}
	}

	public static String dynamicEncryptedPassword(final String realm)
	{
		String envServiceEncryptedPassword = CommonUtils.getSystemEnv("ENV_SERVICE_PASSWORD"+"_"+realm.toUpperCase());
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
