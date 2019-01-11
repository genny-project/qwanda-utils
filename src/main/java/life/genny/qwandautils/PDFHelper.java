package life.genny.qwandautils;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;

import org.json.simple.JSONObject;

import com.google.gson.Gson;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public class PDFHelper {
	
	private static final Logger logger = LoggerFactory
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());
	
	public static final String PDF_GEN_SERVICE_API_URL = System.getenv("PDF_GEN_SERVICE_API_URL") == null ? "http://localhost:7331"
			: System.getenv("PDF_GEN_SERVICE_API_URL");
	
	/**
	 * 
	 * @param htmlUrl - Complete URL of the html-content
	 * @param contextMap - Map of keys and values for merging content with html
	 * @return downloadable URL for PDF
	 */
	public static String getDownloadablePdfLinkForHtml(String htmlUrl, HashMap<String, Object> contextMap){
		
		String content = null;
		String downloadablePdfUrl = null;
		
		try {
			/* Get content from link in String format */
			content = QwandaUtils.apiGet(htmlUrl, null);			
			/* If merge is required, use MergeUtils for merge with context map */
			if(contextMap != null) {
				content = MergeUtil.merge(content, contextMap);
			}	

		} catch (IOException e) {
			logger.fatal("An exception occurred.", e);
		}
				
		String path = getHtmlStringToPdfInByte(content);
		logger.info("path ::"+path);

		if(path != null) {
		    
			downloadablePdfUrl = PDF_GEN_SERVICE_API_URL + path;
			logger.info("download url ::"+downloadablePdfUrl);
			return downloadablePdfUrl;
		}
		
		return downloadablePdfUrl;
	}
	
	/**
	 * 
	 * @param htmlString - Stringified HTML content 
	 * @param contextMap - Map of keys and values for merging content with html
	 * @return downloadable URL for PDF
	 */
	public static String getDownloadablePdfLinkForHtmlString(String htmlString, HashMap<String, Object> contextMap){
		
		String content = null;
		String downloadablePdfUrl = null;
			
		/* If merge is required, use MergeUtils for merge with context map */
		if(contextMap != null) {
			content = MergeUtil.merge(htmlString, contextMap);
		}	
				
		String path = getHtmlStringToPdfInByte(content);
		logger.info("path ::"+path);

		if(path != null) {
		    
			downloadablePdfUrl = PDF_GEN_SERVICE_API_URL + path;
			logger.info("download url ::"+downloadablePdfUrl);
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
			logger.fatal("An exception occurred.", e);
		}
		logger.info("response for attachment ::" + resp);

		if(resp != null) {
			JSONObject respObj = JsonUtils.fromJson(resp, JSONObject.class);
			path = (String) respObj.get("path");
		}
		
		return path;
	}

}
