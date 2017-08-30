package life.genny;

//import java.io.IOException;
//import java.time.LocalDateTime;
//
//import org.junit.Test;
//
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//
//import life.genny.qwanda.Answer;
//import life.genny.qwanda.DateTimeDeserializer;
//import life.genny.qwandautils.KeycloakUtils;
//import life.genny.qwandautils.QwandaUtils;

public class QwandaTest {

//	@Test 
//	// run the keycloak docker located in the root of the project runKeycloak.sh
//	public void tokenTest() {
//		try {
//		String tokenString = KeycloakUtils.getToken("http://localhost:8180",
//					"wildfly-swarm-keycloak-example", "curl", "056b73c1-7078-411d-80ec-87d41c55c3b4", "user1", "password1");
//			System.out.println(tokenString);
//			
//			// fetch qwanda details
//			String json = QwandaUtils.apiGet("http://localhost:8180/qwanda/asks", tokenString);
//			System.out.println("asks:"+json);
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
//	
//	@Test
//	public void JsonArrayTest()
//	{
//		try {
//		String tokenString = KeycloakUtils.getToken("http://localhost:8180",
//				"wildfly-swarm-keycloak-example", "curl", "056b73c1-7078-411d-80ec-87d41c55c3b4", "user1", "password1");
//			String json = QwandaUtils.apiGet("http://localhost:8180/qwanda/asks", tokenString);
////			Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
////				@Override
////				public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
////				    return ZonedDateTime.parse(json.getAsJsonPrimitive().getAsString()).toLocalDateTime();
////				}
////				}).create();			
////			System.out.println(json);
//			GsonBuilder gsonBuilder = new GsonBuilder();
//			gsonBuilder.registerTypeAdapter(LocalDateTime.class, new DateTimeDeserializer());
//
//			Gson gson = gsonBuilder.create();			
//			Answer ans = new Answer("PER_BOB", "PER_BOB", "PRI_FIRSTNAME","Bob");
//			String answerString = gson.toJson(ans);
//	
////			String answerStr = "{\"created\":\"2017-03-02T10:11:12.000+10:00\",\"value\":\"Bob2\",\"attributeCode\":\"PRI_FIRSTNAME\",\"targetCode\":22,\"sourceCode\":20,\"expired\":false,\"refused\":false,\"weight\":0.5}";
////			Answer  answer = gson.fromJson(answerStr, Answer.class);
//			System.out.println(answerString);
////			QDataAskMessage askMsg = new QDataAskMessage(asks);
////			
////	
////			System.out.println(askMsg);
//////			System.out.println(jsonArray.get(0));
//	} catch (IOException e) {
//		// TODO Auto-generated catch block
//		e.printStackTrace();
//	}
//	}
}
