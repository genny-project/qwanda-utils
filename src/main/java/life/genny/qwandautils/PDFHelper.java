package life.genny.qwandautils;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;

import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import com.google.gson.Gson;

public class PDFHelper {
	
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	final public static String PDF_GEN_SERVICE_API_URL = System.getenv("PDF_GEN_SERVICE_API_URL") == null ? "http://localhost:7331"
			: System.getenv("PDF_GEN_SERVICE_API_URL");
	
	public static String getDownloadablePdfLinkForHtml(String htmlUrl, HashMap<String, Object> contextMap){
		
		String content = null;
		String downloadablePdfUrl = null;
		
		try {
			/* Get content from link in String format */
			content = QwandaUtils.apiGet(htmlUrl, null);			
			/* If merge is required, use MergeUtils for merge with context map */
			content = MergeUtil.merge(content, contextMap);

		} catch (IOException e) {
			e.printStackTrace();
		}
				
		String path = getHtmlStringToPdfInByte(content);
		log.info("path ::"+path);

		if(path != null) {
		    
			downloadablePdfUrl = PDF_GEN_SERVICE_API_URL + path;
			log.info("download url ::"+downloadablePdfUrl);
			return downloadablePdfUrl;
		}
		
		return downloadablePdfUrl;
	}
	
	/* Converts HTML String into PDF and return a downloadable link */
	public static String getHtmlStringToPdfInByte(String htmlString) {

		JSONObject postObj = new JSONObject();
		postObj.put("html", htmlString);
		Gson gson = new Gson();
		String resp = null;
		String path = null;
		try {

			/* Camelot htmlToPdfConverter service */ 
			resp = QwandaUtils.apiPostEntity(PDF_GEN_SERVICE_API_URL + "/raw", gson.toJson(postObj), null);
		} catch (IOException e) {
			e.printStackTrace();
		}
		log.info("response for attachment ::" + resp);

		if(resp != null) {
			JSONObject respObj = JsonUtils.fromJson(resp, JSONObject.class);
			path = (String) respObj.get("path");
		}
		
		return path;
	}

}
