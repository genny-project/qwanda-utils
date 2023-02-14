package life.genny.qwandautils;

import com.google.gson.Gson;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;

public class PDFHelper {
	
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	final public static String PDF_GEN_SERVICE_API_URL = System.getenv("PDF_GEN_SERVICE_API_URL") == null ? "http://localhost:7331"
			: System.getenv("PDF_GEN_SERVICE_API_URL");

// k8s svc name
	final public static String PDF_GEN_SVC_URL =  System.getenv("PDF_GEN_SVC_URL");

	public static String getJournalPDFHeader(String headerURL) throws IOException {
//		String headerURL = "https://raw.githubusercontent.com/genny-project/layouts/2020-05-25-journal-report-update/internmatch-new/document_templates/journal-header-template.html";
		return QwandaUtils.apiGet(headerURL, null);
	}
	
	public static String getDownloadablePdfLinkForHtml(String htmlHeaderUrl, String htmlRowUrl, HashMap<String, Object> contextMap, List<HashMap<String, Object>> contextMapList){
		
		String content = null;
		String downloadablePdfUrl = null;
		String finalContent = "";
		String headerContent = "";

		try {
			/* Get content from link in String format */
			content = QwandaUtils.apiGet(htmlRowUrl, null);
			
			// Merge the header
			headerContent = QwandaUtils.apiGet(htmlHeaderUrl, null);
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
		return getDownloadablePdfLinkForHtml(htmlUrl,contextMap,true);
	}

	/**
	 * This method can directly fetch the template from the given url in htmlUrl
	 * as well as use the template content passed in htmlUrl based on the flag isUrl
	 * @param htmlUrl
	 * @param contextMap
	 * @param isUrl
	 * @return
	 */
	public static String getDownloadablePdfLinkForHtml(String htmlUrl, HashMap<String, Object> contextMap, Boolean isUrl){
		
		String content = null;
		String downloadablePdfUrl = null;
		
		try {
			if (isUrl){
				/* Get content from link in String format */
				content = QwandaUtils.apiGet(htmlUrl, null);
			}else{
				content = htmlUrl;
			}
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
			if (PDF_GEN_SVC_URL == null){
				resp = QwandaUtils.apiPostEntity(PDF_GEN_SERVICE_API_URL + "/raw", gson.toJson(postObj), null);
			} else {
			// Internal deployment use self-signed certificate, use http://k8s-svc-name instead of https call
				resp = QwandaUtils.apiPostEntity(PDF_GEN_SVC_URL + "/raw", gson.toJson(postObj), null);
			}
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
