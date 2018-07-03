package life.genny.qwandautils;

import java.io.IOException;
import java.util.HashMap;

import org.json.simple.JSONObject;

import com.google.gson.Gson;

public class PDFHelper {
	
	final public static String PDF_GEN_SERVICE_API_URL = System.getenv("PDF_GEN_SERVICE_API_URL") == null ? "http://localhost:7331"
			: System.getenv("PDF_GEN_SERVICE_API_URL");
	
	public static String getDownloadablePdfLinkForHtml(String htmlUrl, HashMap<String, Object> contextMap){
		
		String content = null;
		String downloadablePdfUrl = null;
		
		try {
			/* Get content from link in String format */
			content = QwandaUtils.apiGet(htmlUrl, null);
			System.out.println("link string ::"+content);
			
			/* If merge is required, use MergeUtils for merge with context map */
			content = MergeUtil.merge(content, contextMap);

		} catch (IOException e) {
			e.printStackTrace();
		}
				
		String path = getHtmlStringToPdfInByte(content);
		System.out.println("path ::"+path);

		if(path != null) {
		    
			downloadablePdfUrl = PDF_GEN_SERVICE_API_URL + path;
			System.out.println("download url ::"+downloadablePdfUrl);
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
		System.out.println("response for attachment ::" + resp);

		if(resp != null) {
			JSONObject respObj = JsonUtils.fromJson(resp, JSONObject.class);
			path = (String) respObj.get("path");
		}
		
		return path;
	}

}
