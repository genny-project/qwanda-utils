package life.genny.qwandautils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesReader {
    private Properties properties;

    public PropertiesReader(String propertyFileName)  {
        InputStream is = getClass().getClassLoader()
            .getResourceAsStream(propertyFileName);
        this.properties = new Properties();
        try {
			this.properties.load(is);
		} catch (IOException e) {
			
		}
    }

    public String getProperty(String propertyName) {
    return getProperty(propertyName,null);
    	
    }
    public String getProperty(String propertyName,String defaultValue) {
        String ret = defaultValue;
    	if (this.properties != null) {
    		if (this.properties.containsKey(propertyName)) {
    			return this.properties.getProperty(propertyName);
    		}
    	}
    	return ret;
    	
    }
}