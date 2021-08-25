package life.genny.qwandautils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import life.genny.qwanda.DateTimeDeserializer;
import life.genny.qwanda.MoneyDeserializer;
import life.genny.qwanda.datatype.LocalDateConverter;
import life.genny.qwanda.entity.BaseEntity;
import org.apache.logging.log4j.Logger;
import org.javamoney.moneta.Money;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class JsonUtils {
	
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	static GsonBuilder gsonBuilder = new GsonBuilder();       

	static public Gson gson = gsonBuilder.registerTypeAdapter(Money.class, new MoneyDeserializer())
			.registerTypeAdapter(LocalDateTime.class, new DateTimeDeserializer())
			.registerTypeAdapter(LocalDate.class, new LocalDateConverter())
		//	.excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE)
			.excludeFieldsWithoutExposeAnnotation()
		//    .disableHtmlEscaping()
		    .setPrettyPrinting()
			.create();

	static public Gson gsonFull = gsonBuilder.registerTypeAdapter(Money.class, new MoneyDeserializer())
			.registerTypeAdapter(LocalDateTime.class, new DateTimeDeserializer())
			.registerTypeAdapter(LocalDate.class, new LocalDateConverter())
		//	.excludeFieldsWithModifiers(Modifier.STATIC, Modifier.TRANSIENT, Modifier.VOLATILE)
			.excludeFieldsWithoutExposeAnnotation()
		//    .disableHtmlEscaping()
			.serializeNulls()
		    .setPrettyPrinting()
			.create();

	
	public static <T> T fromJson(final String json, Class clazz)
	{
	        T item = null;
	        if (json != null) {
	                try {
	                	if (clazz.getSimpleName().equalsIgnoreCase(BaseEntity.class.getSimpleName())) {
	                		 item = (T)gson.fromJson(json, clazz);
	                	} else {
	                      item = (T)gson.fromJson(json, clazz);
	                	}
	                } catch (Exception e) {
	                	 log.error("The JSON file received is  :::  "+json);;
	                     log.error("Bad Deserialisation for "+clazz.getSimpleName()+":"+e.getLocalizedMessage());
	                }
	        }
	        return item;
	}
	
	public static <T> T fromJson(final String json, Type clazz)
	{
	        T item = null;
	        if (json != null) {
	                try {
	                      item = (T)gson.fromJson(json, clazz);

	                } catch (Exception e) {
	                	log.error("The JSON file received is  :::  "+json);;
	                	log.error("Bad Deserialisation for "+clazz.getTypeName()+":"+e.getLocalizedMessage());
	                }
	        }
	        return item;
	}
	
	public static String toJson(Object obj)
	{
	
		String ret =  gson.toJson(obj);
		return ret;
	}

	public static String toJsonWithNulls(Object obj)
	{
	
		String ret =  gsonFull.toJson(obj);
		return ret;
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

