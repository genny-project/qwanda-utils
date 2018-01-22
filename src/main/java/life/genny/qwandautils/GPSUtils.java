package life.genny.qwandautils;
import java.io.IOException;
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

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.w3c.dom.Document;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import life.genny.qwanda.GPS;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QCmdGeofenceMessage;

public class GPSUtils {
	
  /*
   * Sends QCmdGeofenceMessage for the given beg, address and radius
   */
	static 	String loadId = null;
	static JSONArray loadAttributes = new JSONArray();
	
	public static QCmdGeofenceMessage[] geofenceJob(BaseEntity be, final String driverCode, Double radius,
			final String qwandaServiceUrl, String token, Map<String, Object> decodedToken) {
		
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
			    GPS gpsLocation = new GPS(be.getCode(), pickupLatitude, pickupLongitude);        
			    QCmdGeofenceMessage cmdGeoFence = new QCmdGeofenceMessage(gpsLocation, radius, be.getCode() + "_PICKUP", be.getCode() + "_PICKUP");	 
				
			    /* Send geofence CMD to driver for delivery */
			    GPS gpsLocationDelivery = new GPS(be.getCode(), deliveryLatitude, deliveryLongitude);        
			    QCmdGeofenceMessage cmdGeoFenceDelivery = new QCmdGeofenceMessage(gpsLocationDelivery, radius, be.getCode() + "_DELIVERY", be.getCode() + "_DELIVERY");
			    
			    QCmdGeofenceMessage[] cmds = new QCmdGeofenceMessage[2];
			    cmds[0] = cmdGeoFence;
			    cmds[1] = cmdGeoFenceDelivery;
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
	
	/**
	 * 
	 * @param Double[] coordinates1
	 * @param Double[] coordinates2
	 * @return the distance between passed coordinates in meters
	 */
	public static Double getDistance(Double[] coordinates1, Double[] coordinates2)
	{
		try {
			
			/* Call Google Maps API to know how far the driver is */
			String response = QwandaUtils.apiGet("https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + coordinates1[0] + "," + coordinates1[1] + "&destinations=" + coordinates2[0] + "," + coordinates2[1] + "&mode=driving&language=pl-PL", null);

			System.out.println(response);
			if(response != null) {
				
				JsonObject distanceMatrix = new JsonObject(response);
				JsonArray elements = distanceMatrix.getJsonArray("rows").getJsonObject(0).getJsonArray("elements");
				JsonObject distance = elements.getJsonObject(0).getJsonObject("distance");
				Integer distanceValue = distance.getInteger("value");
				return distanceValue.doubleValue();
			}
		}
		catch (Exception e) {
			
		}
			
	    return -1.0;
	}
	
	/**
	 * 
	 * @param String latitude1
	 * @param String longitude1
	 * @param String latitude2
	 * @param String longitude2
	 * @return the distance between passed coordinates in meters
	 */
	public static Double getDistance(String latitude1String, String longitude1String, String latitude2String, String longitude2String)
	{
		try {
			
			if(latitude1String != null && longitude1String != null && latitude2String != null && longitude2String != null) {
           		
           		Double latitude1 = Double.parseDouble(latitude1String);
           		Double longitude1 = Double.parseDouble(longitude1String);
           		Double latitude2 = Double.parseDouble(latitude2String);
           		Double longitude2 = Double.parseDouble(longitude2String);
           		
           		/* Call Google Maps API to know how far the driver is */
	    			String response = QwandaUtils.apiGet("https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + latitude1 + "," + longitude1 + "&destinations=" + latitude2 + "," + longitude2 + "&mode=driving&language=pl-PL", null);
	
	    			if(response != null) {
	    				
	    				JsonObject distanceMatrix = new JsonObject(response);
	    				JsonArray elements = distanceMatrix.getJsonArray("rows").getJsonObject(0).getJsonArray("elements");
	    				JsonObject distance = elements.getJsonObject(0).getJsonObject("distance");
	    				Integer distanceValue = distance.getInteger("value");
	    				return distanceValue.doubleValue();
	    			}
           	}

		}
		catch (Exception e) {
			
		}
			
	    return -1.0;
	}

}
