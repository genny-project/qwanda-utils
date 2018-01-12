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

		authObj.put("tenant", "testing");
		authObj.put("token", "hello");
		authObj.put("secret", "testing");

		String encodedAuthString = PaymentUtils.base64Encoder(authObj.toJSONString());

		org.json.simple.JSONObject decodedobj = PaymentUtils.base64Decoder(encodedAuthString);

		System.out.println("decoded object ::" + decodedobj);

	}
	


}
