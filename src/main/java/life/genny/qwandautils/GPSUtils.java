package life.genny.qwandautils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
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
import life.genny.qwanda.GPSLeg;
import life.genny.qwanda.GPSLocation;
import life.genny.qwanda.GPSRoute;
import life.genny.qwanda.GPSRouteStatus;
import life.genny.qwanda.GPSStep;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QCmdGeofenceMessage;

public class GPSUtils {

	/*
	 * Sends QCmdGeofenceMessage for the given beg, address and radius
	 */
	static String loadId = null;
	static JSONArray loadAttributes = new JSONArray();

	public static QCmdGeofenceMessage[] geofenceJob(BaseEntity be, final String driverCode, Double radius,
			final String qwandaServiceUrl, String token, Map<String, Object> decodedToken) {

		if (be != null) {

			String pickupLatitude = null;
			String pickupLongitude = null;

			String deliveryLatitude = null;
			String deliveryLongitude = null;

			Set<EntityAttribute> attributes = be.getBaseEntityAttributes();
			for (EntityAttribute attribute : attributes) {

				switch (attribute.getAttributeCode()) {
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
			if (pickupLatitude != null && pickupLongitude != null && deliveryLatitude != null
					&& deliveryLongitude != null) {

				/* Send geofence CMD to driver for pickup */
				GPS gpsLocation = new GPS(be.getCode(), pickupLatitude, pickupLongitude);
				QCmdGeofenceMessage cmdGeoFence = new QCmdGeofenceMessage(gpsLocation, radius, be.getCode() + "_PICKUP",
						be.getCode() + "_PICKUP");

				/* Send geofence CMD to driver for delivery */
				GPS gpsLocationDelivery = new GPS(be.getCode(), deliveryLatitude, deliveryLongitude);
				QCmdGeofenceMessage cmdGeoFenceDelivery = new QCmdGeofenceMessage(gpsLocationDelivery, radius,
						be.getCode() + "_DELIVERY", be.getCode() + "_DELIVERY");

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
	public static String[] getLatLong(String address) throws Exception {
		int responseCode = 0;
		String api = "http://maps.googleapis.com/maps/api/geocode/xml?address=" + URLEncoder.encode(address, "UTF-8")
				+ "&sensor=true";
		System.out.println("API URL : " + api);
		URL url = new URL(api);
		HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
		httpConnection.connect();
		responseCode = httpConnection.getResponseCode();
		if (responseCode == 200) {
			DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			;
			Document document = dBuilder.parse(httpConnection.getInputStream());
			XPathFactory xPathfactory = XPathFactory.newInstance();
			XPath xpath = xPathfactory.newXPath();
			XPathExpression expr = xpath.compile("/GeocodeResponse/status");
			String status = (String) expr.evaluate(document, XPathConstants.STRING);
			if (status.equals("OK")) {
				expr = xpath.compile("//geometry/location/lat");
				String latitude = (String) expr.evaluate(document, XPathConstants.STRING);
				expr = xpath.compile("//geometry/location/lng");
				String longitude = (String) expr.evaluate(document, XPathConstants.STRING);
				return new String[] { latitude, longitude };
			} else {
				throw new Exception("Error from the API - response status: " + status);
			}
		}
		return null;
	}

	/**
	 * 
	 * @param Double[]
	 *            coordinates1
	 * @param Double[]
	 *            coordinates2
	 * @return the distance between passed coordinates in meters
	 */
	public static Double getDistance(Double[] coordinates1, Double[] coordinates2) {
		try {

			/* Call Google Maps API to know how far the driver is */
			String response = QwandaUtils.apiGet("https://maps.googleapis.com/maps/api/distancematrix/json?origins="
					+ coordinates1[0] + "," + coordinates1[1] + "&destinations=" + coordinates2[0] + ","
					+ coordinates2[1] + "&mode=driving&language=pl-PL", null);

			System.out.println(response);
			if (response != null) {

				JsonObject distanceMatrix = new JsonObject(response);
				JsonArray elements = distanceMatrix.getJsonArray("rows").getJsonObject(0).getJsonArray("elements");
				JsonObject distance = elements.getJsonObject(0).getJsonObject("distance");
				Integer distanceValue = distance.getInteger("value");
				return distanceValue.doubleValue();
			}
		} catch (Exception e) {

		}

		return -1.0;
	}

	/**
	 * 
	 * @param String
	 *            latitude1
	 * @param String
	 *            longitude1
	 * @param String
	 *            latitude2
	 * @param String
	 *            longitude2
	 * @return the distance between passed coordinates in meters
	 */
	public static Double getDistance(String latitude1String, String longitude1String, String latitude2String,
			String longitude2String) {
		try {

			if (latitude1String != null && longitude1String != null && latitude2String != null
					&& longitude2String != null) {

				Double latitude1 = Double.parseDouble(latitude1String);
				Double longitude1 = Double.parseDouble(longitude1String);
				Double latitude2 = Double.parseDouble(latitude2String);
				Double longitude2 = Double.parseDouble(longitude2String);

				/* Call Google Maps API to know how far the driver is */
				String response = QwandaUtils.apiGet("https://maps.googleapis.com/maps/api/distancematrix/json?origins="
						+ latitude1 + "," + longitude1 + "&destinations=" + latitude2 + "," + longitude2
						+ "&mode=driving&language=pl-PL", null);

				if (response != null) {

					JsonObject distanceMatrix = new JsonObject(response);
					JsonArray elements = distanceMatrix.getJsonArray("rows").getJsonObject(0).getJsonArray("elements");
					JsonObject distance = elements.getJsonObject(0).getJsonObject("distance");
					Integer distanceValue = distance.getInteger("value");
					return distanceValue.doubleValue();
				}
			}

		} catch (Exception e) {

		}

		return -1.0;
	}

	public static GPSLocation getGPSLocation(final String address, final String googleApiKey) {
		GPSLocation result = null;
		String addressStr = null;
		try {
			addressStr = URLEncoder.encode(address, "UTF-8");
			final String url = "https://maps.googleapis.com/maps/api/geocode/json?address=" + addressStr + "&key="
					+ googleApiKey;
			String json = QwandaUtils.apiGet(url, null);
			JsonObject jsonObject = new JsonObject(json);
			if ("OK".equalsIgnoreCase(jsonObject.getString("status"))) {
				JsonArray results = jsonObject.getJsonArray("results");
				JsonObject firstResult = results.getJsonObject(0);
				JsonObject geometry = firstResult.getJsonObject("geometry");
				JsonObject location = geometry.getJsonObject("location");
				Double lat = location.getDouble("lat");
				Double lng = location.getDouble("lng");
				System.out.println(lat + "," + lng);
				result = new GPSLocation(lat, lng);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	public static GPSRoute getRoute(final GPSLocation origin, final GPSLocation end, final String apiKey) {
		GPSRoute routeResult = null;
		String originStr = origin.getLatitude() + "," + origin.getLongitude();
		String endStr = end.getLatitude() + "," + end.getLongitude();
		String apiUrl = "https://maps.googleapis.com/maps/api/directions/json?origin=" + originStr + "&destination="
				+ endStr + "&key=" + apiKey;
		String json;
		try {
			json = QwandaUtils.apiGet(apiUrl, null);
			JsonObject jsonObject = new JsonObject(json);
			if ("OK".equalsIgnoreCase(jsonObject.getString("status"))) {
				JsonArray routes = jsonObject.getJsonArray("routes");
				for (Object route : routes) {
					List<GPSLeg> legs = new ArrayList<GPSLeg>();
					JsonObject routeObj = (JsonObject) route;
					JsonArray legsJson = routeObj.getJsonArray("legs");

					routeResult = new GPSRoute(origin, end);

					for (Object legObj : legsJson) {
						JsonObject legJson = (JsonObject) legObj;
						JsonObject distanceJson2 = legJson.getJsonObject("distance");
						Long distanceLong2 = distanceJson2.getLong("value"); // in m
						JsonObject durationJson2 = legJson.getJsonObject("duration");
						Long durationLong2 = durationJson2.getLong("value"); // in s
						Double distance_m2 = Double.valueOf(distanceLong2+"");
						Double duration_s2 = Double.valueOf(durationLong2+"");
						String start_address = legJson.getString("start_address");
						JsonObject start_location = legJson.getJsonObject("start_location");
						GPSLocation startLoc = new GPSLocation(start_location.getDouble("lat"),start_location.getDouble("lng"));
						String end_address = legJson.getString("end_address");
						JsonObject end_location = legJson.getJsonObject("end_location");
						GPSLocation endLoc = new GPSLocation(end_location.getDouble("lat"),end_location.getDouble("lng"));
						JsonArray stepsArray = legJson.getJsonArray("steps");
						
						GPSLeg gpsLeg = new GPSLeg(startLoc, endLoc, distance_m2, duration_s2);

						for (Object step : stepsArray) {
							JsonObject stepJson = (JsonObject) step;
							JsonObject distanceJson3 = stepJson.getJsonObject("distance");
							Long distanceLong3 = distanceJson3.getLong("value"); // in m
							JsonObject durationJson3 = stepJson.getJsonObject("duration");
							Long durationLong3 = durationJson3.getLong("value"); // in s
							Double distance_m3 = Double.valueOf(distanceLong3+"");
							JsonObject start_location3 = stepJson.getJsonObject("start_location");
							JsonObject end_location3 = stepJson.getJsonObject("end_location");
							Double duration_s3 = Double.valueOf(durationLong3+"");
							GPSLocation startLoc3 = new GPSLocation(start_location3.getDouble("lat"),start_location3.getDouble("lng"));
							GPSLocation endLoc3 = new GPSLocation(end_location3.getDouble("lat"),end_location3.getDouble("lng"));
							String html_instructions = stepJson.getString("html_instructions");
							System.out.println(stepJson);
							GPSStep gpsStep = new GPSStep(startLoc3, endLoc3, distance_m3, duration_s3);
							gpsStep.setHtmlInstruction(html_instructions);
							gpsLeg.add(gpsStep);
						}
						
						routeResult.add(gpsLeg);
						
					}
					
					break;
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(apiUrl);
		return routeResult;
	}


	static public GPSRouteStatus fetchCurrentRouteStatusByDuration(final GPSRoute route, final Double currentSeconds)
	{
		GPSRouteStatus result = null;
		GPSLocation currentLocation = null;
		// Loop through route legs and steps until currentSeconds reached
		Double secondsSum = 0.0;
		Double distanceSum = 0.0;
		
		for (GPSLeg leg : route.getLegList()) {
			Double legSecs = leg.getDuration_s();
			if ((secondsSum+legSecs) > currentSeconds) {
				// go into leg
				Double stepSum = 0.0;
				Double stepDistanceSum = 0.0;
				for (GPSStep step : leg.getStepList()) {
					stepSum += step.getDuration();
					if ((secondsSum+stepSum) > currentSeconds) {
						// work out ratio
						// apply dumb time ratio to distance
						Double currentStepSecs = currentSeconds - (secondsSum + stepSum - step.getDuration());
						Double timeRatio =  currentStepSecs / step.getDuration();
						Double stepDuration = timeRatio * step.getDuration();
						Double stepdistance = timeRatio * step.getDistance();
						stepDistanceSum += stepdistance;
						stepSum += stepDuration;
						distanceSum += stepDistanceSum;
						// Ideally a google api could map to an actual GPS location on the route (road).
					//	currentLocation = step.getStart();
						GPSLocation stepEndLoc = step.getEnd();
						// interpolate between the start and end GPS (ignore sticking to road)
						Double latDiff = stepEndLoc.getLatitude() - step.getStart().getLatitude();
						Double lngDiff = stepEndLoc.getLongitude() - step.getStart().getLongitude();
						Double guessLat = step.getStart().getLatitude() + (latDiff * timeRatio);
						Double guessLng = step.getStart().getLongitude() + (lngDiff * timeRatio);
						currentLocation = new GPSLocation(guessLat, guessLng);
						break;
						
					} else {
			
						stepDistanceSum += step.getDistance();
					}
				}
			} else {
				secondsSum += legSecs;
				distanceSum += leg.getDistance_m();
			}
			
			
		}
		Double totalDistance = route.getDistance_m();
		Double percentage = distanceSum/totalDistance;
		result = new GPSRouteStatus(currentLocation, distanceSum, currentSeconds, percentage);
		return result;
	}

	static public GPSRouteStatus fetchCurrentRouteStatusByPercentageDistance(final GPSRoute route, final Double percentage100)
	{
		GPSRouteStatus result = null;
		GPSLocation currentLocation = null;
		// Loop through route legs and steps until currentSeconds reached
		Double durationSum = 0.0;
		Double distanceSum = 0.0;
		
		Double targetDistance = route.getDistance_m()*percentage100/100.0;
		
		for (GPSLeg leg : route.getLegList()) {
			Double legM = leg.getDistance_m();
			if ((distanceSum+legM) > targetDistance) {
				// go into leg
				Double stepSum = 0.0;
				Double stepDurationSum = 0.0;
				for (GPSStep step : leg.getStepList()) {
					stepSum += step.getDistance();
					if ((distanceSum+stepSum) > targetDistance) {
						// work out ratio
						// apply dumb time ratio to distance
						Double currentStepM = targetDistance - (distanceSum + stepSum - step.getDistance());
						Double distanceRatio =  currentStepM / step.getDistance();
						Double stepDuration = distanceRatio * step.getDuration();
						Double stepdistance = distanceRatio * step.getDistance();
						distanceSum += stepdistance + (stepSum - step.getDistance());
						durationSum += stepDuration;

						// Ideally a google api could map to an actual GPS location on the route (road).
					//	currentLocation = step.getStart();
						GPSLocation stepEndLoc = step.getEnd();
						// interpolate between the start and end GPS (ignore sticking to road)
						Double latDiff = stepEndLoc.getLatitude() - step.getStart().getLatitude();
						Double lngDiff = stepEndLoc.getLongitude() - step.getStart().getLongitude();
						Double guessLat = step.getStart().getLatitude() + (latDiff * distanceRatio);
						Double guessLng = step.getStart().getLongitude() + (lngDiff * distanceRatio);
						currentLocation = new GPSLocation(guessLat, guessLng);
						break;
						
					} else {
			
						durationSum += step.getDuration();
					}
				}
			} else {
				distanceSum += legM;
				durationSum += leg.getDuration_s();
			}
			
			
		}
		Double totalDuration = route.getDuration_s();
		Double percentage = durationSum/totalDuration;
		result = new GPSRouteStatus(currentLocation, distanceSum, durationSum, percentage);
		return result;
	}

}
