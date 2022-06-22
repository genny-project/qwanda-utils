package life.genny.qwandautils;

import com.google.gson.Gson;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import life.genny.models.GennyToken;

import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PDFHelper {

    protected static final Logger log = org.apache.logging.log4j.LogManager
            .getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

    final public static String PDF_GEN_SERVICE_API_URL = System.getenv("PDF_GEN_SERVICE_API_URL") == null ? "http://localhost:7331"
            : System.getenv("PDF_GEN_SERVICE_API_URL");

    public static String getJournalPDFHeader(String headerURL) throws IOException {
//		String headerURL = "https://raw.githubusercontent.com/genny-project/layouts/2020-05-25-journal-report-update/internmatch-new/document_templates/journal-header-template.html";
        return QwandaUtils.apiGet(headerURL, null);
    }

    public static String getDownloadablePdfLinkForHtml(String htmlHeaderUrl, String htmlRowUrl, HashMap<String, Object> contextMap, List<HashMap<String, Object>> contextMapList) {

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
            for (HashMap<String, Object> ctxMap : contextMapList) {
                finalContent += MergeUtil.merge(content, ctxMap);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String path = getHtmlStringToPdfInByte(finalContent);
        log.info("path ::" + path);

        if (path != null) {

            downloadablePdfUrl = PDF_GEN_SERVICE_API_URL + path;
            log.info("download url ::" + downloadablePdfUrl);
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

    public static String getDownloadablePdfLinkForHtml(String htmlUrl, HashMap<String, Object> contextMap, GennyToken token) {

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
        /* Fetch file from camelot and post it to MinIO */
        String objectUuid = postCamelotFileToMinio(content, token);
        log.info("objectUuid ::" + objectUuid);

        if (objectUuid != null) {

            downloadablePdfUrl = GennySettings.mediaProxyUrl + "/" + objectUuid;
            log.info("download url ::" + downloadablePdfUrl);
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

        if (resp != null) {
            JSONObject respObj = JsonUtils.fromJson(resp, JSONObject.class);
            path = (String) respObj.get("path");
        }

        return path;
    }

    public static String postCamelotFileToMinio(String htmlString, GennyToken token) {
        String response = null;
        try {
            String camelotLink = getHtmlStringToPdfInByte(htmlString);
            log.info("camelot file path ::" + camelotLink);

            if (camelotLink != null) {
                String projectUrl = GennySettings.projectUrl;
                if (projectUrl == null) projectUrl = "https://m.internmatch.io";
                camelotLink = projectUrl + "/camelot" + camelotLink;
                log.info("Camelot full url: {}", camelotLink);
                /* Fetching PDF Byte array */
                byte[] pdfBytes = QwandaUtils.getForByteArray(camelotLink);
                log.info("pdfBytes length: {}", pdfBytes.length);
                Map<Object, Object> formData = new HashMap<>();
                formData.put("file", pdfBytes);
                /* Posting file to media-proxy */
                String[] linkTokens = camelotLink.split("/");
                log.info("linkTokens: {}", linkTokens);
                if (linkTokens != null && linkTokens.length > 0) {
                    String fileName = linkTokens[linkTokens.length - 1];
                    response = QwandaUtils.postFile(GennySettings.mediaProxyUrl, token, fileName, formData);
                    log.info("response for attachment ::" + response);
                    if (response != null) {
                        JsonObject mediaProxyResponse = new JsonObject(response);
                        JsonArray files = mediaProxyResponse.getJsonArray("files");
                        JsonObject jsonObject = files.getJsonObject(0);
                        String uuid = jsonObject.getString("uuid");
                        return uuid;
                    }
                }
            }
        } catch (Exception ex) {
            log.error("Exception : {}", ex.getMessage());
            ex.printStackTrace();
            return null;
        }
        return null;
    }
}
