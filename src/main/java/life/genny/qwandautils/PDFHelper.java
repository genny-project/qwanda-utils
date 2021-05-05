package life.genny.qwandautils;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import com.google.gson.Gson;

public class PDFHelper {
	
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	final public static String PDF_GEN_SERVICE_API_URL = System.getenv("PDF_GEN_SERVICE_API_URL") == null ? "http://localhost:7331"
			: System.getenv("PDF_GEN_SERVICE_API_URL");

	public static String getJournalPDFHeader(String headerURL) throws IOException {
//		String headerURL = "https://raw.githubusercontent.com/genny-project/layouts/2020-05-25-journal-report-update/internmatch-new/document_templates/journal-header-template.html";
		return QwandaUtils.apiGet(headerURL, null);
	}
	
	public static String getDownloadablePdfLinkForHtml(String htmlHeaderUrl, String htmlRowUrl, HashMap<String, Object> contextMap, List<HashMap<String, Object>> contextMapList){
		
		String content = null;
		String downloadablePdfUrl = null;
		String finalContent = "";
		String headerContent = "";

		log.info("htmlRowUrl :::"+htmlRowUrl);
		try {
			/* Get content from link in String format */
			content = QwandaUtils.apiGet(htmlRowUrl, null);
			
		  log.info("content :::"+content);
			// Merge the header
			headerContent = QwandaUtils.apiGet(htmlHeaderUrl, null);
		  log.info("headerContent :::"+headerContent);
			headerContent = MergeUtil.merge(headerContent, contextMap);
			
			finalContent += headerContent;
			/* If merge is required, use MergeUtils for merge with context map */
			for(HashMap<String, Object> ctxMap : contextMapList) {
				finalContent += MergeUtil.merge(content, ctxMap);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		String path = getHtmlStringToPdfInByte(finalContent);
		log.info("path ::"+path);

		if(path != null) {
		    
			downloadablePdfUrl = PDF_GEN_SERVICE_API_URL + path;
			log.info("download url ::"+downloadablePdfUrl);
			return downloadablePdfUrl;
		}
		
		return downloadablePdfUrl;
	}
	
	public static String getDownloadablePdfLinkForHtml(String htmlUrl, HashMap<String, Object> contextMap){
		
		String content = null;
		String downloadablePdfUrl = null;
		
		try {
			/* Get content from link in String format */
			content = QwandaUtils.apiGet(htmlUrl, null);			
			/* If merge is required, use MergeUtils for merge with context map */
		  log.info("content :::"+content);
			content = MergeUtil.merge(content, contextMap);

		  log.info("content after merge :::"+content);
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

		log.info("htmlString::"+htmlString);
		JSONObject postObj = new JSONObject();
		postObj.put("html", htmlString);
		Gson gson = new Gson();
		String resp = null;
		String path = null;
		try {

			/* Camelot htmlToPdfConverter service */ 
			resp = QwandaUtils.apiPostEntity(PDF_GEN_SERVICE_API_URL + "/raw", gson.toJson(postObj), null);
		  log.info("resp::"+resp);
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
