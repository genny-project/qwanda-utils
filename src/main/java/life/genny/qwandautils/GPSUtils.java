package life.genny.qwandautils;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.json.JSONArray;
import org.json.JSONObject;
import org.w3c.dom.Document;

import life.genny.qwanda.GPS;
import life.genny.qwanda.message.QCmdGeofenceMessage;

public class GPSUtils {
	
  /*
   * Sends QCmdGeofenceMessage for the given beg, address and radius
   */
	static 	String loadId = null;
	static String sourceAddress = null;
	static String destAddress = null;
	static JSONArray loadAttributes = new JSONArray();
    //return the QCmdGeofenceMessage of either source or destination address based on the targetAddress
	public static QCmdGeofenceMessage getGeoFenceOfAddress(String sourceBaseEntityCode, String begCode, Double radius, String targetAddress,
			String token) {
		 String latLong[] = null;
		 String entryCode = null;
		 String exitCode = null;
		 
		String qwandaServiceUrl = System.getenv("REACT_APP_QWANDA_API_URL");
		//QCmdGeofenceMessage cmdGeoFence = new QCmdGeofenceMessage();
		System.out.println("sourceBaseEntityCode-----> " + sourceBaseEntityCode);
		System.out.println("BEG code ------> " + begCode);
		
		try {
			JSONArray begChidrens = new JSONArray(QwandaUtils.apiGet(
					qwandaServiceUrl+"/qwanda/entityentitys/" + begCode + "/linkcodes/LNK_BEG/children", token));
			System.out.println("JSON childrens from GPSUtils: " + begChidrens.toString());

			for (Object obj : begChidrens) {
				if (((JSONObject) obj).get("linkValue").equals("LOAD")) {
					System.out.println(">>>>>> Found!! LOAD <<<<<<<<");
					loadId = (String) ((JSONObject) obj).get("targetCode");
					System.out.println("The LOAD ID is : " + loadId);
				}
			}
			
			JSONObject beg1 = new JSONObject(QwandaUtils.apiGet(qwandaServiceUrl+"/qwanda/baseentitys/" +begCode+ "/linkcodes/LNK_BEG/attributes", token));
			System.out.println("JSON BEG from GPSUtils: " + beg1.toString());
			JSONArray itemsArray = beg1.getJSONArray("items");

			 itemsArray.forEach(items-> { 
				 if( ((JSONObject) items).get("code").equals(loadId) ) {
			       System.out.println(">>>>>> Found!! Load  "+loadId+" <<<<<<<<"); 			      
			       loadAttributes = ((JSONObject) items).getJSONArray("baseEntityAttributes");
			       System.out.println("JSON Array of Load Attributes: "+loadAttributes.toString()); 
			     }			 
			 });
			 
			 loadAttributes.forEach(attributes->{
				 if( ((JSONObject) attributes).get("attributeCode").equals("PRI_FULL_PICKUP_ADDRESS") ) {
				       System.out.println(">>>>>> Found!! attribute <<<<<<<<"); 				       
				       sourceAddress = (String) ((JSONObject) attributes).get("valueString");
				       System.out.println("The Source Address is "+sourceAddress);
				 }
				 if( ((JSONObject) attributes).get("attributeCode").equals("PRI_FULL_DROPOFF_ADDRESS") ) {
				       System.out.println(">>>>>> Found!! attribute <<<<<<<<"); 				       
				       destAddress = (String) ((JSONObject) attributes).get("valueString");
				       System.out.println("The Destination Address is "+destAddress);
				 }
			 });
		if (targetAddress.equals("Source")) {
		      latLong = getLatLong(sourceAddress);
		      entryCode = begCode+"_ENTER_SOURCE";
			  exitCode = begCode+"_EXIT_SOURCE";
			  System.out.println("The Lat Long of Source is "+latLong[0]+ latLong[1]);
		 }else if(targetAddress.equals("Destination")) {
			 latLong = getLatLong(destAddress);
			 entryCode = begCode+"_ENTER_DESTINATION";
			 exitCode = begCode+"_EXIT_DESTINATION";
			 System.out.println("The Lat Long of Destination is "+latLong[0]+ latLong[1]);
		}
			 
		} catch (Exception e) {
			System.out.println("ERROR! " + e.toString());
		}
		//Create GPS	    
	    GPS gpsLocation = new GPS(begCode, latLong[0], latLong[1]);        
	   
	    //Create QCmdGeofenceMessage
	    QCmdGeofenceMessage cmdGeoFence = new QCmdGeofenceMessage(gpsLocation, 10.0, entryCode, exitCode);	 
        
		return cmdGeoFence;
	}
	
	//Gives the latitude and longitude of the given address
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
