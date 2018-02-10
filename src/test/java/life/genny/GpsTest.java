package life.genny;

import org.junit.Test;

import life.genny.qwanda.GPSLocation;
import life.genny.qwanda.GPSRoute;
import life.genny.qwandautils.GPSUtils;

public class GpsTest {
	@Test
	public void getApiUrlTest()
	{
		final String googleApiKey = "AIzaSyCtaue7XGY-pNto0DgjZUyMudu2o0NkI88";
		GPSLocation origin = GPSUtils.getGPSLocation("64A Fakenham Rd, Ashburton, VIC, 3147", googleApiKey);
		GPSLocation end = GPSUtils.getGPSLocation("121 Cardigan St, Carlton, VIC, 3053", googleApiKey);
		GPSRoute route = GPSUtils.getRoute(origin, end, googleApiKey);
		System.out.println(route);
	}
}