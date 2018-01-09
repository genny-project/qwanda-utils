package life.genny;

import org.json.simple.JSONObject;
import org.junit.Test;

import com.google.gson.Gson;

import life.genny.qwandautils.PaymentUtils;

public class PaymentUtilsTest {
	
	@Test
	public void testEncoder() {
		JSONObject authObj = new JSONObject();
		authObj.put("tenant", "test");
		authObj.put("token", "abc");
		authObj.put("secret", "def");

		String encodedAuthString = PaymentUtils.base64Encoder(authObj.toJSONString());
		System.out.println("encodedAuthString ::"+encodedAuthString);
		
		JSONObject obj = PaymentUtils.base64Decoder(encodedAuthString);
		System.out.println("decoded json object ::"+obj);
		
		Gson gson = new Gson();
		gson.toJson(authObj);
	}

}
