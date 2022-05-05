package life.genny.utils;

import org.jboss.logging.Logger;

/**
 * A few Common Utils to use throughout Genny.
 *
 * @author Bryn
 * @author Jasper
 */
public class CommonUtils {

    private static final Logger log = Logger.getLogger(CommonUtils.class);


    /**
     * Fetch a System Environment Variable (Effectively {@link System#getenv()} but with logging). Will log if the environment variable is missing
     * @param env - Environment variable to fetch
     * @return - value of env or null if it is missing
     */
    public static String getSystemEnv(String env) {
        return getSystemEnv(env, true);
    }

    /**
     * Fetch a System Environment Variable (Effectively {@link System#getenv()} but with logging)
     * @param env - Environment variable to fetch
     * @param alert whether or not to log if the environment variable is missing (default true)
     * @return - value of env or null if it is missing
     */
    public static String getSystemEnv(String env, boolean alert) {
        String result = System.getenv(env);
        if(result == null && alert) {
            log.error("Cannot get environment variable " + env + ". Is it set?");
        }

        return result;
    }
}
