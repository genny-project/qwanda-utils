package life.genny;

import org.junit.Test;

import life.genny.qwandautils.SecurityUtils;

public class SecurityUtilsTest {
	@Test
	public void versionTest()
	{
		        String key        = "HelloImA121Key@!"; // 128 bit key
		        String initVector = "PRJ_GENNY*******"; // 16 bytes IV

		        String encrypted = SecurityUtils.encrypt(key, initVector, "I am some encrypted text!");
		        System.out.println("["+encrypted+"]");;
		        System.out.println(SecurityUtils.decrypt(key, initVector, encrypted
		                ));
	}
}
