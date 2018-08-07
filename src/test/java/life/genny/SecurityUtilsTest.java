package life.genny;

import org.junit.Test;

import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.SecurityUtils;

public class SecurityUtilsTest {
	@Test
	public void versionTest()
	{
		        String key        = GennySettings.defaultServiceKey; // 128 bit key
		        String initVector = "PRJ_GENNY*******"; // 16 bytes IV

		        String encrypted = SecurityUtils.encrypt(key, initVector, GennySettings.defaultServicePassword);
		        System.out.println("["+encrypted+"]");;
		        System.out.println(SecurityUtils.decrypt(key, initVector, encrypted
		                ));
		        

	}
}
