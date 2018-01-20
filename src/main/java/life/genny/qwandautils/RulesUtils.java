package life.genny.qwandautils;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import life.genny.qwanda.Answer;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QDataBaseEntityMessage;

public class RulesUtils {

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_BLUE = "\u001B[34m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";
	public static final String ANSI_YELLOW = "\u001B[33m";
	public static final String ANSI_PURPLE = "\u001B[35m";
	public static final String ANSI_CYAN = "\u001B[36m";
	public static final String ANSI_WHITE = "\u001B[37m";
	public static final String ANSI_BOLD = "\u001b[1m";

	public static final String qwandaServiceUrl = System.getenv("REACT_APP_QWANDA_API_URL");
	public static final Boolean devMode = System.getenv("GENNY_DEV") == null ? false : true;

	final static Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
              @Override
              public LocalDate deserialize(final JsonElement json, final Type type,
                  final JsonDeserializationContext jsonDeserializationContext)
                  throws JsonParseException {
                return LocalDate.parse(json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ISO_LOCAL_DATE);
              }

              public JsonElement serialize(final LocalDate date, final Type typeOfSrc,
                  final JsonSerializationContext context) {
                return new JsonPrimitive(date.format(DateTimeFormatter.ISO_LOCAL_DATE)); 
              }
            }).registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
                  @Override
                  public LocalDateTime deserialize(final JsonElement json, final Type type,
                      final JsonDeserializationContext jsonDeserializationContext)
                      throws JsonParseException {
                    return LocalDateTime.parse(json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                  }

                  public JsonElement serialize(final LocalDateTime date, final Type typeOfSrc,
                      final JsonSerializationContext context) {
                    return new JsonPrimitive(date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)); 
                  }
                }).create();
	public static String executeRuleLogger(final String status, final String module, final String topColour,
			final String bottomColour) {
		String moduleLogger = "\n" + (devMode ? "" : bottomColour) + status + " ::  " + module
				+ (devMode ? "" : ANSI_RESET)  ;
		return moduleLogger;
	}

	public static String terminateRuleLogger(String module) {
		return executeRuleLogger("TERMINATED", module, ANSI_YELLOW, ANSI_GREEN) + "\n"+ (devMode ? "" : ANSI_YELLOW)	
				+"======================================================================================================="
				+ (devMode ? "" : ANSI_RESET);
	
	}

	public static String headerRuleLogger(String module) {
		return "======================================================================================================="
				+ executeRuleLogger("EXECUTED", module, ANSI_RED, ANSI_GREEN) + "\n"+ (devMode ? "" : ANSI_RED)	
				+ (devMode ? "" : ANSI_RESET);
	}

	public static void header(final String module) {
		println(headerRuleLogger(module));
	}

	public static void footer(final String module) {
		println(terminateRuleLogger(module));
	}
	
	public static String jsonLogger(String module, Object data) {
        String initialLogger = "------------------------------------------------------------------------\n";
        String moduleLogger =  ANSI_YELLOW + module + "   ::   " + ANSI_RESET +  data + "\n";
        String finalLogger = "------------------------------------------------------------------------\n";
        return initialLogger + moduleLogger + finalLogger;
    }  

	public static void ruleLogger(String module, Object data) {
        println(jsonLogger(module, data));
    }

	public static void println(final Object obj, final String colour) {
		if (devMode) {
			System.out.println(obj);
		} else {
			log.info((devMode ? "" : colour) + obj + (devMode ? "" : ANSI_RESET));
		}

	}
	
	public static void println(final Object obj) {
		println(obj,ANSI_RESET);

	}

	public static JsonObject createDataAnswerObj(Answer answer, String token) {

		JsonObject data = new JsonObject();
		data.put("value", answer.getValue());

		String jsonAnswerStr = gson.toJson(answer);
		JsonObject jsonAnswer = new JsonObject(jsonAnswerStr);

		JsonArray items = new JsonArray();
		items.add(jsonAnswer);

		/* Creating Answer DATA_MSG */
		JsonObject obj = new JsonObject();
		obj.put("msg_type", "DATA_MSG");
		obj.put("data_type", "Answer");
		obj.put("items", items);
		obj.put("token", token);

		return obj;

	}

	/**
	 * 
	 * @param qwandaServiceUrl
	 * @param decodedToken
	 * @param token
	 * @return baseEntity user for the decodedToken passed
	 */
	public static BaseEntity getUser(final String qwandaServiceUrl, Map<String, Object> decodedToken,
			final String token) {

		try {
			String beJson = null;
			String username = (String) decodedToken.get("preferred_username");
			String uname = QwandaUtils.getNormalisedUsername(username);
			String code = "PER_" + uname.toUpperCase();
			// CHEAT TODO
			beJson = QwandaUtils.apiGet(qwandaServiceUrl + "/qwanda/baseentitys/"+code, token);
			BaseEntity be = gson.fromJson(beJson, BaseEntity.class);

//			if (username != null) {
//				beJson = QwandaUtils.apiGet(qwandaServiceUrl + "/qwanda/baseentitys/GRP_USERS/linkcodes/LNK_CORE/attributes?PRI_USERNAME=" + username+"&pageSize=1", token);
//			} else {
//				String keycloakId = (String) decodedToken.get("sed");
//				beJson = QwandaUtils.apiGet(qwandaServiceUrl + "/qwanda/baseentitys/GRP_USERS/linkcodes/LNK_CORE/attributes?PRI_KEYCLOAKID=" + keycloakId+"&pageSize=1",
//						token);
//
//			}
//			QDataBaseEntityMessage msg = gson.fromJson(beJson, QDataBaseEntityMessage.class);
//			BaseEntity be = msg.getItems()[0];
////			List<BaseEntity> bes = Arrays.asList(gson.fromJson(beJson, BaseEntity[].class));
//			BaseEntity be = bes.get(0);

			return be;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

	/**
	 * 
	 * @param qwandaServiceUrl
	 * @param decodedToken
	 * @param token
	 * @return baseEntity user for the decodedToken passed
	 */
	public static BaseEntity getBaseEntityByCode(final String qwandaServiceUrl, Map<String, Object> decodedToken,
			final String token, final String code) {

		try {
			String beJson = null;
			beJson = QwandaUtils.apiGet(qwandaServiceUrl + "/qwanda/baseentitys/" + code, token);
			BaseEntity be = gson.fromJson(beJson, BaseEntity.class);
			return be;

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}

}
