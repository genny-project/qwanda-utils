package life.genny.qwandautils;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.Logger;
import org.javamoney.moneta.Money;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import life.genny.qwanda.DateTimeDeserializer;
import life.genny.qwanda.MoneyDeserializer;
import life.genny.qwanda.entity.BaseEntity;

public class JsonUtils {
	
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	static GsonBuilder gsonBuilder = new GsonBuilder();       

	static public Gson gson = gsonBuilder.registerTypeAdapter(Money.class, new MoneyDeserializer())
			.registerTypeAdapter(LocalDateTime.class, new DateTimeDeserializer()).registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
			    @Override
			    public LocalDate deserialize(JsonElement json, java.lang.reflect.Type typeOfT,
			        JsonDeserializationContext context) throws JsonParseException {

			      return LocalDate.parse(json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ISO_DATE);     
			    }
			}).setPrettyPrinting()
			.registerTypeAdapter(LocalDate.class, new JsonDeserializer<LocalDate>() {
				@Override
				public LocalDate deserialize(final JsonElement json, final Type type,
						final JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
					LocalDate ret = null;
					try {
						String str = json.getAsJsonPrimitive().getAsString();
					} catch (Exception e) {
						return null;
					}
					ret = LocalDate.parse(json.getAsJsonPrimitive().getAsString(), DateTimeFormatter.ISO_LOCAL_DATE);
					return ret;
				}

				public JsonElement serialize(final LocalDate date, final Type typeOfSrc,
						final JsonSerializationContext context) {
					return new JsonPrimitive(date.format(DateTimeFormatter.ISO_LOCAL_DATE));
				}
			})
			// .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
			.excludeFieldsWithoutExposeAnnotation().create();
	
	
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
	                	     System.out.println("The JSON file received is  :::  "+json);;
	                     log.error("Bad Deserialisation for "+clazz.getSimpleName());
	                }
	        }
	        return item;
	}
	
	public static String toJson(Object obj)
	{
	
		String ret =  gson.toJson(obj);
		return ret;
	}
}

