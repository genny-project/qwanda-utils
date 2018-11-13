package life.genny.qwandautils;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.apache.logging.log4j.Logger;
import org.javamoney.moneta.Money;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import life.genny.qwanda.DateTimeDeserializer;
import life.genny.qwanda.MoneyDeserializer;
import life.genny.qwanda.datatype.LocalDateConverter;

public class JsonUtils {
	
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	static GsonBuilder gsonBuilder = new GsonBuilder();       

	public static final Gson GSON = gsonBuilder.registerTypeAdapter(Money.class, new MoneyDeserializer())
			.registerTypeAdapter(LocalDateTime.class, new DateTimeDeserializer()).setPrettyPrinting()
			.registerTypeAdapter(LocalDate.class, new LocalDateConverter())

			.excludeFieldsWithoutExposeAnnotation().create();
	
	private JsonUtils() {}
	
	public static <T> T fromJson(final String json, Class clazz)
	{
	        T item = null;
	        if (json != null) {
	                try {
	                   item = (T)GSON.fromJson(json, clazz);
	                } catch (Exception e) {
	                	     System.out.println("The JSON file received is  :::  "+json);
	                     log.error("Bad Deserialisation for "+clazz.getSimpleName());
	                }
	        }
	        return item;
	}
	
	public static <T> T fromJson(final String json, Type clazz)
	{
	        T item = null;
	        if (json != null) {
	                try {
	                      item = (T)GSON.fromJson(json, clazz);

	                } catch (Exception e) {
	                	     System.out.println("The JSON file received is  :::  "+json);
	                     log.error("Bad Deserialisation for "+clazz.getTypeName());
	                }
	        }
	        return item;
	}
	
	public static String toJson(Object obj)
	{
		return  GSON.toJson(obj);
	}
	
	
	public static org.json.simple.JSONObject jsonStringParser(String stringifiedJsonObject) {
		
		org.json.simple.JSONObject obj = null;
		if(stringifiedJsonObject != null) {
			
			JSONParser parser = new JSONParser();
			try {
				obj = (org.json.simple.JSONObject) parser.parse(stringifiedJsonObject);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		return obj;	
	}
}

