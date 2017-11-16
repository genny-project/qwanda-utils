package life.genny;

import org.junit.Test;

import life.genny.qwanda.QwandaVersion;
import life.genny.qwandautils.QwandaUtilsVersion;

public class VersionTest {
	@Test
	public void versionTest()
	{
		System.out.println("------------- qwanda-utils version ---------------------------\n");
		
		System.out.println("Version:\t"+QwandaUtilsVersion.getVersion());
		System.out.println("Build:  \t"+QwandaUtilsVersion.getBuildDate());
		System.out.println("Commit: \t"+QwandaUtilsVersion.getCommitDate());
		
		
		System.out.println("----------------------------------------\n"+QwandaUtilsVersion.getJson());

	}
	
	@Test
	public void qwandaVersionTest()
	{
		System.out.println("------------- qwanda version ---------------------------\n");
		System.out.println("Version:\t"+QwandaVersion.getVersion());
		System.out.println("Build:  \t"+QwandaVersion.getBuildDate());
		System.out.println("Commit: \t"+QwandaVersion.getCommitDate());
		
		
		System.out.println("----------------------------------------\n"+QwandaVersion.getJson());

	}
}
