package life.genny;

import org.junit.Test;
import org.mortbay.log.Log;

import io.vertx.core.json.JsonObject;



//import org.junit.Assert;
//
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class JsonUtilsTest {

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
	
	
	@Test
	public void jsonTest()
	{
		String testStr = "{\"status\":\"ok\",\"value\":\"eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICI2NXFCQUhzaU0xRUZQaXF2ZmFQcVFwelVJT0ZJTXhwZFJ6akxmMWpUVWJVIn0.eyJqdGkiOiIzNTE1N2EwNS1kNDJmLTRkZWUtODIzOS1hODQwNjY5NDg1OTUiLCJleHAiOjE1NTA5MzczMTksIm5iZiI6MCwiaWF0IjoxNTUwOTE5MzE5LCJpc3MiOiJodHRwOi8va2V5Y2xvYWsuZ2VubnkubGlmZTo4MTgwL2F1dGgvcmVhbG1zL2dlbm55IiwiYXVkIjoiZ2VubnkiLCJzdWIiOiIwZGMyYjI3Zi03MzkxLTQ1MDctYjlkNy0xNThhYWNjNGMyMTMiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJnZW5ueSIsImF1dGhfdGltZSI6MCwic2Vzc2lvbl9zdGF0ZSI6IjZjZGIyOWYxLTZhMmQtNGVhZC1iNzMyLWY3OTIwNzU2N2YwNSIsImFjciI6IjEiLCJhbGxvd2VkLW9yaWdpbnMiOlsiaHR0cDovL2FseXNvbi5nZW5ueS5saWZlIiwiaHR0cDovL21lc3NhZ2VzOjgwODAiLCJodHRwOi8vc29jaWFsLmdlbm55LmxpZmUiLCJodHRwOi8vbG9jYWxob3N0OjUwMDAiLCJodHRwOi8vbG9jYWxob3N0OjE1MDAwIiwiaHR0cDovL2FseXNvbjMuZ2VubnkubGlmZSIsImh0dHA6Ly9sb2NhbGhvc3Q6MzAwMCIsImh0dHBzOi8vbG9jYWxob3N0OjgwODAiLCJodHRwOi8vYnJpZGdlLmdlbm55LmxpZmUiLCJodHRwOi8vcXdhbmRhLXNlcnZpY2UuZ2VubnkubGlmZSIsImh0dHA6Ly9zb2NpYWw6ODA4MCIsImh0dHA6Ly9sb2NhbGhvc3Q6ODA4MCIsImh0dHA6Ly9sb2NhbGhvc3Q6ODI4MCIsImh0dHA6Ly9hbHlzb24yLmdlbm55LmxpZmUiLCJodHRwOi8vYnJpZGdlOjgwODgiLCJodHRwOi8vYXBpLmdlbm55LmxpZmUiLCJodHRwOi8vYWx5c29uIiwiaHR0cDovLzEwLjEyMy4xMjMuMTIzOjUwMDAiLCJodHRwOi8vcnVsZXNzZXJ2aWNlOjgwODAiLCJodHRwOi8vbWVzc2FnZXMuZ2VubnkubGlmZSIsImh0dHA6Ly8xMC4xMjMuMTIzLjEyMzozMDAwIiwiaHR0cDovL3J1bGVzc2VydmljZS5nZW5ueS5saWZlIl0sInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJ1bWFfYXV0aG9yaXphdGlvbiIsInVzZXIiXX0sInJlc291cmNlX2FjY2VzcyI6eyJhY2NvdW50Ijp7InJvbGVzIjpbIm1hbmFnZS1hY2NvdW50IiwibWFuYWdlLWFjY291bnQtbGlua3MiLCJ2aWV3LXByb2ZpbGUiXX19LCJuYW1lIjoiU2VydmljZSBVc2VyIiwicHJlZmVycmVkX3VzZXJuYW1lIjoic2VydmljZSIsImdpdmVuX25hbWUiOiJTZXJ2aWNlIiwiZmFtaWx5X25hbWUiOiJVc2VyIiwiZW1haWwiOiJzZXJ2aWNlLmdlbm55K2FkbWluQGdlbm55LmxpdmUifQ.jusSqrhfKaM_KWAi0bvqbalsNMrfwMoRSJHSaKcLCg6_WeNth4nJspH9q5VYVNkBDkHG4p_CiWM2PomVV_lGlt8fwvSkQRI09OCnmHvPBGGSOBwr-VOBQCdbjtdKHWcm-mhbogfU-0HSal6zDXmJFpfXhn6iL4jiXqOXIrmF9QJ6K9rf10WhcxlMgwJJlUYNJMVyIJqxdujSERlUJr5In2UjA1U552mdNdGohL0iC58mpWOlFN2FCFcr84YKFoWjH1bIJO8CyhaqQME73_p-Nk91rJHWjo2y2c856bC8BUHEmhd2qxDqnKvwyr_7XghxUulNKxw7RBdvT6QefZ3N1g\"}";
		JsonObject test = new JsonObject(testStr);
		Log.info("test="+test);
	}
	

}
