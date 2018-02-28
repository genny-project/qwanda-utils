package life.genny;

import java.util.Base64;

import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.google.gson.Gson;

import life.genny.qwandautils.PaymentUtils;

public class PaymentUtilsTest {
	
	@Test
	public void testEncoder() throws ParseException {
		org.json.simple.JSONObject authObj = new org.json.simple.JSONObject();

		authObj.put("tenant", "test");
		authObj.put("token", "YzQ2MzY4NTQ3ZThiNDc5ZTg4MTg3OTQ0NWFmYTUxOTI=");
		authObj.put("secret", "UdZ5fx63LyJUBpfKw0EEkHXF7FD60FxO");

		String encodedAuthString = PaymentUtils.base64Encoder(authObj.toJSONString());
		
		System.out.println("encoded ::"+encodedAuthString);

		org.json.simple.JSONObject decodedobj = PaymentUtils.base64Decoder(encodedAuthString);

		System.out.println("decoded object ::" + decodedobj);

	}
	


}
