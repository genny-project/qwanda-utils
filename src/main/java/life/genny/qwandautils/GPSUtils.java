package life.genny.qwandautils;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.json.JSONArray;
import org.w3c.dom.Document;

import io.vertx.core.json.JsonObject;
import life.genny.qwanda.GPS;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QCmdGeofenceMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;

import life.genny.qwandautils.RulesUtils;

public class GPSUtils {
	
  /*
   * Sends QCmdGeofenceMessage for the given beg, address and radius
   */
	static 	String loadId = null;
	static JSONArray loadAttributes = new JSONArray();
	
	public static QCmdGeofenceMessage[] geofenceJob(final String begCode, final String driverCode, Double radius,
			final String qwandaServiceUrl, String token, Map<String, Object> decodedToken) {
		
		BaseEntity be = RulesUtils.getBaseEntityByCode(qwandaServiceUrl, decodedToken, token, begCode);
		if(be != null) {
			
			String pickupLatitude = null;
			String pickupLongitude = null;
			
			String deliveryLatitude = null;
			String deliveryLongitude = null;
			
			Set<EntityAttribute> attributes = be.getBaseEntityAttributes();
			for(EntityAttribute attribute: attributes) {
				
				switch(attribute.getAttributeCode()) {
				case "PRI_PICKUP_ADDRESS_LATITUDE":
					pickupLatitude = attribute.getObjectAsString();
					break;
				case "PRI_PICKUP_ADDRESS_LONGITUDE":
					pickupLongitude = attribute.getObjectAsString();
					break;
				case "PRI_DROPOFF_ADDRESS_LATITUDE":
					deliveryLatitude = attribute.getObjectAsString();
					break;
				case "PRI_DROPOFF_ADDRESS_LONGITUDE":
					deliveryLongitude = attribute.getObjectAsString();
					break;
				}
			}
			if(pickupLatitude != null && pickupLongitude != null && deliveryLatitude != null && deliveryLongitude != null) {
				
				/* Send geofence CMD to driver for pickup */
			    GPS gpsLocation = new GPS(begCode, pickupLatitude, pickupLongitude);        
			    QCmdGeofenceMessage cmdGeoFence = new QCmdGeofenceMessage(gpsLocation, 10.0, begCode + "_ENTRY_CODE", begCode + "EXIT_CODE");	 
				
			    /* Send geofence CMD to driver for delivery */
			    GPS gpsLocationDelivery = new GPS(begCode, deliveryLatitude, deliveryLongitude);        
			    QCmdGeofenceMessage cmdGeoFenceDelivery = new QCmdGeofenceMessage(gpsLocation, 10.0, begCode + "_ENTRY_CODE", begCode + "EXIT_CODE");
			    
			    QCmdGeofenceMessage[] cmds = new QCmdGeofenceMessage[2];
			    cmds[0] = cmdGeoFence;
			    cmds[0] = cmdGeoFenceDelivery;
			    return cmds;
			}
		}
		
		return null;
	}
	
	/**
	 * 
	 * @param address
	 * @return the latitude and longitude of the given address
	 * @throws Exception
	 */
	public static String[] getLatLong(String address) throws Exception
	{
		int responseCode=0;
		String api = "http://maps.googleapis.com/maps/api/geocode/xml?address=" + URLEncoder.encode(address, "UTF-8") + "&sensor=true";
	    System.out.println("API URL : "+api);
	    URL url = new URL(api);
	    HttpURLConnection httpConnection = (HttpURLConnection)url.openConnection();
	    httpConnection.connect();
	    responseCode = httpConnection.getResponseCode();
	    if(responseCode == 200)
	    {
	      DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();;
	      Document document = dBuilder.parse(httpConnection.getInputStream());
	      XPathFactory xPathfactory = XPathFactory.newInstance();
	      XPath xpath = xPathfactory.newXPath();
	      XPathExpression expr = xpath.compile("/GeocodeResponse/status");
	      String status = (String)expr.evaluate(document, XPathConstants.STRING);
	      if(status.equals("OK"))
	      {
	         expr = xpath.compile("//geometry/location/lat");
	         String latitude = (String)expr.evaluate(document, XPathConstants.STRING);
	         expr = xpath.compile("//geometry/location/lng");
	         String longitude = (String)expr.evaluate(document, XPathConstants.STRING);
	         return new String[] {latitude, longitude};
	      }
	      else
	      {
	         throw new Exception("Error from the API - response status: "+status);
	      }
	    }
	    return null;
	  }

}
