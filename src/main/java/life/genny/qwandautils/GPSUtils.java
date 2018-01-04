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
import org.w3c.dom.Document;

import life.genny.qwanda.GPS;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QCmdGeofenceMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;

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
		 
		System.out.println("sourceBaseEntityCode-----> " + sourceBaseEntityCode);
		System.out.println("BEG code ------> " + begCode);
		
		try {
			
			QDataBaseEntityMessage dataBEMessage = QwandaUtils.getDataBEMessage("GRP_APPROVED", "LNK_CORE", token);
			BaseEntity[] beArray = dataBEMessage.getItems();
			
			for(BaseEntity be : beArray) {
				
				if(be.getCode().equals(begCode)) {
					
					be.getBaseEntityAttributes().forEach(attribute -> {
						switch(attribute.getAttributeCode()) {
						case "PRI_FULL_PICKUP_ADDRESS":
							sourceAddress = MergeUtil.getBaseEntityAttrValueAsString(be, "PRI_FULL_PICKUP_ADDRESS");
							System.out.println("source address ::"+sourceAddress);
							break;
						case "PRI_FULL_DROPOFF_ADDRESS":
							destAddress = MergeUtil.getBaseEntityAttrValueAsString(be, "PRI_FULL_DROPOFF_ADDRESS");
							System.out.println("dest address ::"+destAddress);
							break;
						}
					});
					
				}
			}
			
			if(sourceAddress != null && targetAddress.equalsIgnoreCase("Source")) {
				latLong = getLatLong(sourceAddress);
			    entryCode = begCode+"_ENTER_SOURCE";
				exitCode = begCode+"_EXIT_SOURCE";
				System.out.println("The Lat Long of Source is "+latLong[0]+ latLong[1]);
			} else if(destAddress != null && targetAddress.equalsIgnoreCase("Destination")) {
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
