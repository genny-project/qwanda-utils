package life.genny;

import java.lang.invoke.MethodHandles;

import org.apache.logging.log4j.Logger;

//import java.lang.reflect.Type;
//import java.time.LocalDateTime;
//import java.time.ZonedDateTime;
//
//import org.junit.Test;
//
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import com.google.gson.JsonDeserializationContext;
//import com.google.gson.JsonDeserializer;
//import com.google.gson.JsonElement;
//import com.google.gson.JsonParseException;
//
//import life.genny.qwanda.Answer;

public class JTest {
	
	/**
	 * Stores logger object.
	 */
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

//	@Test
//	public void testJ(){
////		Gson gson = new Gson();
////		QDataAskMessage msg = new QDataAskMessage(new Ask[0]);
////		String json = JsonUtils.set(msg);
////		
////				try {
//
//                    Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
//						@Override
//						public LocalDateTime deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext)
//								throws JsonParseException {
//							// TODO Auto-generated method stub
//							
////							ZonedDateTimefor = new DateTimeFormatter(null, null, null, null, null, null, null);
//							log.info(json.getAsJsonPrimitive().getAsString() + " " + "************************" + ZonedDateTime.now());
////							return ZonedDateTime.parse(json.getAsJsonPrimitive().getAsString()).toLocalDateTime();	
//							return null;
//						}
//                    }).create();      
//                    
////                    log.info(json);
//                    String answerStr = "{\"created\":\"2017-03-02 10:11:12\",\"value\":\"Bob2\",\"attributeCode\":\"PRI_FIRSTNAME\",\"targetCode\":22,\"sourceCode\":20,\"expired\":false,\"refused\":false,\"weight\":0.5}";
//                    Answer  answer = gson.fromJson(answerStr, Answer.class);
//                    log.info(answer);
//            } catch (Exception e) {
//                    log.info("dsjfjdsklf");
//            }
//	}
	
//	@Test
//	public void answerMessageTest(){
//		
//		Answer answer = new Answer("PER_ADAMTEST","PER_ADAMTEST","PRI_FIRSTNAME","Adam");
//		Answer answer2 = new Answer("PER_BYRONTEST","PER_BYRONTEST","PRI_FIRSTNAME","Byron");		
//		
//		List<Answer> answerList = new ArrayList<Answer>();
//		answerList.add(answer);
//		answerList.add(answer2);
//		
//		
//		Gson gson = new Gson();
//		QDataAnswerMessage msg = new QDataAnswerMessage(answerList.toArray(new Answer[0]));
//		
//				try {
//					String json = gson.toJson(msg);
//					log.info(json);
//					
//					QDataAnswerMessage item = gson.fromJson(json, QDataAnswerMessage.class);
//					
//					log.info(item);
//
//	
//            } catch (Exception e) {
//                    
//            }
//	}

}
