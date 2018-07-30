package life.genny.qwandautils;

public class GennySettings {
	public static final String qwandaServiceUrl = System.getenv("REACT_APP_QWANDA_API_URL");
	public static final Boolean devMode = ("TRUE".equalsIgnoreCase(System.getenv("DEV_MODE"))||"TRUE".equalsIgnoreCase(System.getenv("GENNYDEV"))) ? true : false;
	public static final String projectUrl = System.getenv("PROJECT_URL");
	public final static String mainrealm = System.getenv("PROJECT_REALM"); // UGLY
	public final static Boolean isRulesManager = "TRUE".equalsIgnoreCase(System.getenv("RULESMANAGER"));
	public final static Boolean isDdtHost = "TRUE".equalsIgnoreCase(System.getenv("DDTHOST"));

	public static final String ddtUrl = System.getenv("DDT_URL") == null ? ("http://" + System.getenv("HOSTIP") + ":"+System.getenv("CACHE_API_PORT"))
			: System.getenv("DDT_URL");

	public static final String username = (System.getenv("USER") == null ? "GENNY" : System.getenv("USER"));
}
