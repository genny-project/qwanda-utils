package life.genny;

import java.lang.invoke.MethodHandles;

import org.apache.logging.log4j.Logger;
import org.junit.Test;

import life.genny.qwanda.QwandaVersion;
import life.genny.qwandautils.QwandaUtilsVersion;

public class VersionTest {
	
	/**
	 * Stores logger object.
	 */
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	@Test
	public void versionTest()
	{
		log.info("------------- qwanda-utils version ---------------------------\n");
		
		log.info("Version:\t"+QwandaUtilsVersion.getVersion());
		log.info("Build:  \t"+QwandaUtilsVersion.getBuildDate());
		log.info("Commit: \t"+QwandaUtilsVersion.getCommitDate());
		
		
		log.info("----------------------------------------\n"+QwandaUtilsVersion.getJson());

	}
	
	@Test
	public void qwandaVersionTest()
	{
		log.info("------------- qwanda version ---------------------------\n");
		log.info("Version:\t"+QwandaVersion.getVersion());
		log.info("Build:  \t"+QwandaVersion.getBuildDate());
		log.info("Commit: \t"+QwandaVersion.getCommitDate());
		
		
		log.info("----------------------------------------\n"+QwandaVersion.getJson());

	}
}
