/**
 * A Class for storing ANSI text colours 
 * used in logging.
 */

package life.genny.qwandautils;

import org.apache.logging.log4j.Logger;
import java.lang.invoke.MethodHandles;

public class ANSIColour {

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

    public static final String RESET = "\033[0m";
    public static final String BLACK = "\033[0;30m";
    public static final String RED = "\033[0;31m";
    public static final String GREEN = "\033[0;32m";
    public static final String YELLOW = "\033[0;33m";
    public static final String BLUE = "\033[0;34m";
    public static final String PURPLE = "\033[0;35m";
    public static final String CYAN = "\033[0;36m";
    public static final String WHITE = "\033[0;37m";

	public static void logError(String msg) {
		log.error(RED+msg+RESET);
	}

	public static void logSuccess(String msg) {
		log.info(GREEN+msg+RESET);
	}

}
