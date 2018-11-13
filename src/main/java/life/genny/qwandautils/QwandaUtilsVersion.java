package life.genny.qwandautils;

import java.io.IOException;
import java.util.Properties;
import com.google.gson.Gson;

public class QwandaUtilsVersion {

    private QwandaUtilsVersion() {}
    
    public static String getVersion()
	{
		return getProperties().getProperty("git.build.version");
	}
	
	public static String getBuildDate()
	{
		return getProperties().getProperty("git.build.time");
	}
	
	public static String getCommitDate()
	{
		    return getProperties().getProperty("git.commit.time");
	}
	
	public static Properties getProperties()
	{
		   Properties properties = new Properties();
		    try {
		    	Class qv = QwandaUtilsVersion.class;
		    	properties.load(qv.getClass().getResourceAsStream("/qwanda-utils-git.properties" ));
		    } catch (IOException e) {
		      e.printStackTrace();
		    }
		    return properties;
	}
	
	public static String getJson()
	{
		Gson gsonObj = new Gson();
		return  gsonObj.toJson(getProperties());
	}
}
