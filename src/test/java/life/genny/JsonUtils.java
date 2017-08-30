package life.genny;

//import org.junit.Assert;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonUtils {

//	   public static String set(final Object item)  {
//
//	        ObjectMapper mapper = new ObjectMapper();
//	        mapper.registerModule(new JavaTimeModule());
//
//	        String json = null;
//
//	        try {
//	                json = mapper.writeValueAsString(item);
//	        } catch (JsonProcessingException e) {
//	                Assert.assertTrue("Bad Serialization for "+item.getClass().getSimpleName(), true);
//	        }
//	        return json;
//	}
//
//
//
//	public  <T>  T get(Class testClass,final String json)  {
//	        T item = null;
//
//
//	        if (json != null) {
//	                try {
//	                        //item = (T) CoreUtils.deserializeBytes(bytes);
//	                        ObjectMapper mapper = new ObjectMapper();
//	                        mapper.registerModule(new JavaTimeModule());
//
//
//	                        item = (T) mapper.readValue(json.getBytes(), testClass);
//
//	                } catch (Exception e) {
//	                        Assert.assertTrue("Bad Deserialisation for "+testClass.getSimpleName(), true);
//	                }
//	        }
//	        return item;
//	}
}
