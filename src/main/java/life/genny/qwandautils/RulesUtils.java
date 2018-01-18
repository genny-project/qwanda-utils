package life.genny.qwandautils;

import java.lang.invoke.MethodHandles;

import org.apache.logging.log4j.Logger;

public class RulesUtils {
	
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	
	public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	public static final String ANSI_BOLD = "\u001b[1m";
	
	public static final String qwandaServiceUrl = System.getenv("REACT_APP_QWANDA_API_URL");
	public static final Boolean devMode = System.getenv("GENNY_DEV") == null ? false : true;
	
	
	public static String executeRuleLogger(final String status, final String module, final String topColour, final String bottomColour) {
    	String initialLogger = (devMode ? "":  topColour) + "================================================================================================================================================" + ANSI_RESET;
        String moduleLogger = "\n" + (devMode ? "":  bottomColour) +  status  +  " ::  " +module +  ANSI_RESET;
        return initialLogger + moduleLogger;
    }
    
    public static String terminateRuleLogger(String module) {
    	return executeRuleLogger("TERMINATED", module, ANSI_YELLOW, ANSI_GREEN);
    }
    
    public static String headerRuleLogger(String module) {
    	return executeRuleLogger("EXECUTED", module, ANSI_RED, ANSI_YELLOW);
    }
	

}
