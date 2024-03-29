package life.genny.utils;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import com.google.gson.reflect.TypeToken;
import io.vertx.core.json.JsonObject;
import io.vertx.core.shareddata.AsyncMap;
import life.genny.models.GennyToken;
import life.genny.qwanda.Answer;
import life.genny.qwanda.Link;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeText;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.entity.SearchEntity;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.message.QDataAnswerMessage;
import life.genny.qwanda.message.QDataAttributeMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaUtils;

public class RulesUtils {

    protected static final Logger log = org.apache.logging.log4j.LogManager
            .getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

    static public AsyncMap<String, BaseEntity> baseEntityMap;

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";
    public static final String ANSI_BOLD = "\u001b[1m";

    static public Map<String,Map<String, Attribute>> realmAttributeMap = new ConcurrentHashMap<>();
 
    static public Map<String,QDataAttributeMessage> attributesMsg = new ConcurrentHashMap<>();
    static public Map<String,QDataAttributeMessage> defAttributesMap = new ConcurrentHashMap<>();

    static public Map<String,Map<String,BaseEntity>> defs = new ConcurrentHashMap<>();  // realm and DEF lookup

    public static String executeJBPMLogger(final String status, final String module, final String topColour,
                                           final String bottomColour) {
        String moduleLogger = (GennySettings.devMode ? "" : bottomColour) + status + " ::  " + module
                + (GennySettings.devMode ? "" : ANSI_RESET);
        return moduleLogger;
    }

    public static String terminateJBPMLogger(String module) {
        return executeRuleLogger("<<<<<<<<<< END RULE", module, ANSI_YELLOW, ANSI_GREEN) + "\n"
                + (GennySettings.devMode ? "" : ANSI_YELLOW) + (GennySettings.devMode ? "" : ANSI_RESET);

    }

    public static String headerJBPMLogger(String module) {
        return executeRuleLogger(">>>>>>>>>> START RULE", module, ANSI_RED, ANSI_GREEN)
                + (GennySettings.devMode ? "" : ANSI_RED) + (GennySettings.devMode ? "" : ANSI_RESET);
    }

    public static String executeRuleLogger(final String status, final String module, final String topColour,
                                           final String bottomColour) {
        String moduleLogger = (GennySettings.devMode ? "" : bottomColour) + status + " ::  " + module
                + (GennySettings.devMode ? "" : ANSI_RESET);
        return moduleLogger;
    }

    public static String terminateRuleLogger(String module) {
        return executeRuleLogger("<<<<<<<<<< END RULE", module, ANSI_YELLOW, ANSI_GREEN) + "\n"
                + (GennySettings.devMode ? "" : ANSI_YELLOW) + (GennySettings.devMode ? "" : ANSI_RESET);

    }

    public static String headerRuleLogger(String module) {
        return executeRuleLogger(">>>>>>>>>> START RULE", module, ANSI_RED, ANSI_GREEN)
                + (GennySettings.devMode ? "" : ANSI_RED) + (GennySettings.devMode ? "" : ANSI_RESET);
    }

    public static void header(final String module) {
        println(headerRuleLogger(module));
    }

    public static void footer(final String module) {
        println(terminateRuleLogger(module));
    }

    public static String jsonLogger(String module, Object data) {
        String initialLogger = "------------------------------------------------------------------------\n";
        String moduleLogger = ANSI_YELLOW + module + "   ::   " + ANSI_RESET + data + "\n";
        String finalLogger = "------------------------------------------------------------------------\n";
        return initialLogger + moduleLogger + finalLogger;
    }

    public static void ruleLogger(String module, Object data) {
        println(jsonLogger(module, data));
    }

    public static void println(final Object obj, final String colour) {
        Date date = new Date();
        if (GennySettings.devMode) {
            log.info(date + ": " + obj);
        } else {
            log.info((GennySettings.devMode ? "" : colour) + ": " + obj
                    + (GennySettings.devMode ? "" : ANSI_RESET));
        }

    }

    public static void println(final Object obj) {
        println(obj, ANSI_RESET);
    }

    public static String getLayoutCacheURL(String realm, final String path) {

        String host = GennySettings.layoutCacheUrl;

        return String.format("%s/%s", host, path);
    }

    public static String getTodaysDate(final String dateFormat) {

        DateFormat dateFormatter = new SimpleDateFormat(dateFormat);
        Date date = new Date();
        return dateFormatter.format(date);
    }

    public static String getLayout(String realm, String path) {

        String jsonStr = "";
        String finalPath = "";
        try {

            if (path.startsWith("/") == false && realm.endsWith("/") == false) {
                finalPath = realm + "/" + path;
            } else {
                finalPath = realm + path;
            }

            String url = getLayoutCacheURL(realm, finalPath);
            println("Trying to load url.....");
            println(url);

            /* we make a GET request */
            jsonStr = QwandaUtils.apiGet(url, null);

            if (jsonStr != null) {

                /* we serialise the layout into a JsonObject */
                JsonObject layoutObject = new JsonObject(jsonStr);
                if (layoutObject != null) {

                    /* we check if an error happened when grabbing the layout */
                    if ((layoutObject.containsKey("Error") || layoutObject.containsKey("error"))
                            && realm.equals("genny") == false) {

                        /* we try to grab the layout using the genny realm */
                        return RulesUtils.getLayout("genny", path);
                    } else {

                        /* otherwise we return the layout */
                        return jsonStr;
                    }
                }
            }
        } catch (Exception e) {
            log.info(jsonStr);
            return jsonStr;
        }

        return null;
    }

    public static JsonObject createDataAnswerObj(Answer answer, String token) {

        QDataAnswerMessage msg = new QDataAnswerMessage(answer);
        msg.setToken(token);

        return toJsonObject(msg);
    }

    public static String generateServiceToken(String realm, String token) {

        /* we get the service token currently stored in the cache */
        String serviceToken = VertxUtils.getObject(realm, "CACHE", "SERVICE_TOKEN", String.class, token);

        return serviceToken;

    }

    /**
     *
     * @param qwandaServiceUrl
     * @param decodedToken
     * @param token
     * @return baseEntity user for the decodedToken passed
     */
    public static BaseEntity getUser(final String qwandaServiceUrl, Map<String, Object> decodedToken,
                                     final String token) {

        // try {
        String beJson = null;
        String username = (String) decodedToken.get("preferred_username");
        String uuid = (String) decodedToken.get("sub");
        //	String uname = //QwandaUtils.getNormalisedUsername(uuid);
        String code = "PER_" + uuid.toUpperCase(); //uname.toUpperCase();
        String realm = (String) decodedToken.get("aud");
        // CHEAT TODO
        BaseEntity be = VertxUtils.readFromDDT(realm,code, token);
        return be;
    }

    /**
     *
     * @param qwandaServiceUrl
     * @param decodedToken
     * @param token
     * @return baseEntity user for the decodedToken passed
     */
    public static String getBaseEntityJsonByCode(final String qwandaServiceUrl, Map<String, Object> decodedToken,
                                                 final String token, final String code) {
        return getBaseEntityJsonByCode(qwandaServiceUrl, decodedToken, token, code, true);
    }

    /**
     *
     * @param qwandaServiceUrl
     * @param decodedToken
     * @param token
     * @return baseEntity user for the decodedToken passed
     */
    public static String getBaseEntityJsonByCode(final String qwandaServiceUrl, Map<String, Object> decodedToken,
                                                 final String token, final String code, Boolean includeAttributes) {

        try {
            String beJson = null;
            String attributes = "";
            if (includeAttributes) {
                attributes = "/attributes";
            }
            beJson = QwandaUtils.apiGet(qwandaServiceUrl + "/qwanda/baseentitys/" + code + attributes, token);
            return beJson;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     *
     * @param qwandaServiceUrl
     * @param decodedToken
     * @param token
     * @return baseEntity user for the decodedToken passed
     */
    public static String getBaseEntityJsonByAttributeAndValue(final String qwandaServiceUrl,
                                                              Map<String, Object> decodedToken, final String token, final String attributeCode, final String value) {

        return getBaseEntityJsonByAttributeAndValue(qwandaServiceUrl, decodedToken, token, attributeCode, value, 1);

    }

    /**
     *
     * @param qwandaServiceUrl
     * @param decodedToken
     * @param token
     * @return baseEntity user for the decodedToken passed
     */
    public static String getBaseEntityJsonByAttributeAndValue(final String qwandaServiceUrl,
                                                              Map<String, Object> decodedToken, final String token, final String attributeCode, final String value,
                                                              final Integer pageSize) {

        try {
            String beJson = null;
            beJson = QwandaUtils.apiGet(qwandaServiceUrl + "/qwanda/baseentitys/test2?pageSize=" + pageSize + "&"
                    + attributeCode + "=" + value, token);
            // println("BE"+beJson);
            return beJson;

        } catch (IOException e) {
            log.error("Error in fetching Base Entity from Qwanda Service");
        }
        return null;

    }

    /**
     *
     * @param qwandaServiceUrl
     * @param decodedToken
     * @param token
     * @return baseEntity user for the decodedToken passed
     */
    public static List<BaseEntity> getBaseEntitysByAttributeAndValue(final String qwandaServiceUrl,
                                                                     Map<String, Object> decodedToken, final String token, final String attributeCode, final String value) {

        String beJson = getBaseEntityJsonByAttributeAndValue(qwandaServiceUrl, decodedToken, token, attributeCode,
                value, 1000);
        if (StringUtils.isBlank(beJson)) {
            return new ArrayList<BaseEntity>();
        }

        QDataBaseEntityMessage be = fromJson(beJson, QDataBaseEntityMessage.class);

        List<BaseEntity> items = null;

        try {
            items = new ArrayList<BaseEntity>(Arrays.asList(be.getItems()));
        } catch (Exception e) {
            println("Warning: items is null: (json=" + beJson + ")");
        }

        return items;

    }

    /**
     *
     * @param qwandaServiceUrl
     * @param decodedToken
     * @param token
     * @return baseEntity user for the decodedToken passed
     */
    public static BaseEntity getBaseEntityByAttributeAndValue(final String qwandaServiceUrl,
                                                              Map<String, Object> decodedToken, final String token, final String attributeCode, final String value) {

        List<BaseEntity> items = getBaseEntitysByAttributeAndValue(qwandaServiceUrl, decodedToken, token, attributeCode,
                value);

        if ((items != null) && (items.size() > 0)) {
            if (!items.isEmpty())
                return items.get(0);
        }

        return null;

    }

    /**
     *
     * @param qwandaServiceUrl
     * @param decodedToken
     * @param token
     * @return baseEntity user for the decodedToken passed
     */
    public static BaseEntity getBaseEntityByCode(final String qwandaServiceUrl, Map<String, Object> decodedToken,
                                                 final String token, final String code) {

        // String beJson = getBaseEntityJsonByCode(qwandaServiceUrl, decodedToken,
        // token, code, true);
        // BaseEntity be = fromJson(beJson, BaseEntity.class);

        final String realm = (String) decodedToken.get("aud");

        BaseEntity be = VertxUtils.readFromDDT(realm,code, token);

        return be;
    }

    /**
     *
     * @param qwandaServiceUrl
     * @param decodedToken
     * @param token
     * @return baseEntity user for the decodedToken passed
     */
    public static BaseEntity getBaseEntityByCode(final String qwandaServiceUrl, Map<String, Object> decodedToken,
                                                 final String token, final String code, Boolean includeAttributes) {

        return getBaseEntityByCode(qwandaServiceUrl, decodedToken, token, code);
    }

    public static <T> T fromJson(final String json, Class clazz) {
        return JsonUtils.fromJson(json, clazz);
    }

    public static String toJson(Object obj) {

        String ret = JsonUtils.toJson(obj);
        return ret;
    }

    public static JsonObject toJsonObject(Object obj) {
        String json = toJson(obj);
        JsonObject jsonObj = new JsonObject(json);
        return jsonObj;
    }

    public static JsonObject toJsonObject2(Object obj) {
        String json = JsonUtils.toJson(obj);
        JsonObject jsonObj = new JsonObject(json);
        return jsonObj;
    }

    public static String getBaseEntitysJsonByParentAndLinkCode(final String qwandaServiceUrl,
                                                               Map<String, Object> decodedToken, final String token, final String parentCode, final String linkCode) {

        try {
            String beJson = null;
            beJson = QwandaUtils.apiGet(
                    qwandaServiceUrl + "/qwanda/baseentitys/" + parentCode + "/linkcodes/" + linkCode + "/attributes",
                    token);
            return beJson;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static String getBaseEntitysJsonByParentAndLinkCode(final String qwandaServiceUrl,
                                                               Map<String, Object> decodedToken, final String token, final String parentCode, final String linkCode,
                                                               final Integer pageStart, final Integer pageSize) {

        try {
            String beJson = null;
            beJson = QwandaUtils.apiGet(qwandaServiceUrl + "/qwanda/baseentitys/" + parentCode + "/linkcodes/"
                    + linkCode + "/attributes?pageStart=" + pageStart + "&pageSize=" + pageSize, token);
            return beJson;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    /* added because of the bug */
    public static String getBaseEntitysJsonByParentAndLinkCode2(final String qwandaServiceUrl,
                                                                Map<String, Object> decodedToken, final String token, final String parentCode, final String linkCode,
                                                                final Integer pageStart, final Integer pageSize) {

        try {
            String beJson = null;
            beJson = QwandaUtils.apiGet(qwandaServiceUrl + "/qwanda/baseentitys/" + parentCode + "/linkcodes/"
                    + linkCode + "?pageStart=" + pageStart + "&pageSize=" + pageSize, token);
            return beJson;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static String getBaseEntitysJsonByParentAndLinkCodeAndLinkValue(final String qwandaServiceUrl,
                                                                           Map<String, Object> decodedToken, final String token, final String parentCode, final String linkCode,
                                                                           final String linkValue, final Integer pageStart, final Integer pageSize) {

        try {
            String beJson = null;
            beJson = QwandaUtils.apiGet(
                    qwandaServiceUrl + "/qwanda/baseentitys2/" + parentCode + "/linkcodes/" + linkCode + "/linkValue/"
                            + linkValue + "/attributes?pageStart=" + pageStart + "&pageSize=" + pageSize,
                    token);
            return beJson;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static String getBaseEntitysJsonByParentAndLinkCodeWithAttributes(final String qwandaServiceUrl,
                                                                             Map<String, Object> decodedToken, final String token, final String parentCode, final String linkCode) {

        try {
            String beJson = null;
            beJson = QwandaUtils.apiGet(
                    qwandaServiceUrl + "/qwanda/baseentitys/" + parentCode + "/linkcodes/" + linkCode + "/attributes",
                    token);
            return beJson;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static String getBaseEntitysJsonByChildAndLinkCodeWithAttributes(final String qwandaServiceUrl,
                                                                            Map<String, Object> decodedToken, final String token, final String childCode, final String linkCode) {

        try {
            String beJson = null;
            beJson = QwandaUtils.apiGet(qwandaServiceUrl + "/qwanda/baseentitys/" + childCode + "/linkcodes/" + linkCode
                    + "/parents/attributes", token);
            return beJson;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    public static String getBaseEntitysJsonByParentAndLinkCodeWithAttributesAndStakeholderCode(
            final String qwandaServiceUrl, Map<String, Object> decodedToken, final String token,
            final String parentCode, final String linkCode, final String stakeholderCode) {

        try {
            String beJson = null;
            log.info("stakeholderCode is :: " + stakeholderCode);
            beJson = QwandaUtils.apiGet(qwandaServiceUrl + "/qwanda/baseentitys/" + parentCode + "/linkcodes/"
                    + linkCode + "/attributes/" + stakeholderCode, token);
            return beJson;

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     *
     * @param qwandaServiceUrl
     * @param decodedToken
     * @param token
     * @param parentCode
     * @param linkCode
     * @return baseEntitys
     */
    public static BaseEntity[] getBaseEntitysArrayByParentAndLinkCodeWithAttributes(final String qwandaServiceUrl,
                                                                                    Map<String, Object> decodedToken, final String token, final String parentCode, final String linkCode) {

        String beJson = getBaseEntitysJsonByParentAndLinkCode(qwandaServiceUrl, decodedToken, token, parentCode,
                linkCode);
        QDataBaseEntityMessage msg = JsonUtils.fromJson(beJson, QDataBaseEntityMessage.class);
        return msg.getItems();

    }

    /**
     *
     * @param qwandaServiceUrl
     * @param decodedToken
     * @param token
     * @param parentCode
     * @param linkCode
     * @param pageStart
     * @param pageSize
     * @return baseEntitys
     */
    public static BaseEntity[] getBaseEntitysArrayByParentAndLinkCodeWithAttributes(final String qwandaServiceUrl,
                                                                                    Map<String, Object> decodedToken, final String token, final String parentCode, final String linkCode,
                                                                                    final Integer pageStart, final Integer pageSize) {

        String beJson = getBaseEntitysJsonByParentAndLinkCode(qwandaServiceUrl, decodedToken, token, parentCode,
                linkCode, pageStart, pageSize);

        QDataBaseEntityMessage msg = null;
        if (StringUtils.isBlank(beJson)) {
            BaseEntity[] beArray = new BaseEntity[0];
            msg = new QDataBaseEntityMessage(beArray);

        } else {
            msg = JsonUtils.fromJson(beJson, QDataBaseEntityMessage.class);
        }
        if (msg == null) {
            log.error("No BaseEntitys found for parentCode:" + parentCode + " and linkCode:" + linkCode);
            return new BaseEntity[0];
        }
        return msg.getItems();

    }

    /**
     * added because of bug / /**
     *
     * @param qwandaServiceUrl
     * @param decodedToken
     * @param token
     * @param parentCode
     * @param linkCode
     * @param pageStart
     * @param pageSize
     * @return baseEntitys
     */
    public static BaseEntity[] getBaseEntitysArrayByParentAndLinkCodeWithAttributes2(final String qwandaServiceUrl,
                                                                                     Map<String, Object> decodedToken, final String token, final String parentCode, final String linkCode,
                                                                                     final Integer pageStart, final Integer pageSize) {

        String beJson = getBaseEntitysJsonByParentAndLinkCode2(qwandaServiceUrl, decodedToken, token, parentCode,
                linkCode, pageStart, pageSize);
        QDataBaseEntityMessage msg = JsonUtils.fromJson(beJson, QDataBaseEntityMessage.class);
        return msg.getItems();

    }

    /**
     *
     * @param qwandaServiceUrl
     * @param decodedToken
     * @param token
     * @param parentCode
     * @param linkCode
     * @param linkValue
     * @param pageStart
     * @param pageSize
     * @return baseEntitys
     */
    public static BaseEntity[] getBaseEntitysArrayByParentAndLinkCodeAndLinkValueWithAttributes(
            final String qwandaServiceUrl, Map<String, Object> decodedToken, final String token,
            final String parentCode, final String linkCode, final String linkValue, final Integer pageStart,
            final Integer pageSize) {

        String beJson = getBaseEntitysJsonByParentAndLinkCodeAndLinkValue(qwandaServiceUrl, decodedToken, token,
                parentCode, linkCode, linkValue, pageStart, pageSize);
        QDataBaseEntityMessage msg = JsonUtils.fromJson(beJson, QDataBaseEntityMessage.class);
        return msg.getItems();

    }

    /**
     *
     * @param qwandaServiceUrl
     * @param decodedToken
     * @param token
     * @param parentCode
     * @param linkCode
     * @return baseEntitys
     */
    public static List<BaseEntity> getBaseEntitysByParentAndLinkCodeWithAttributes(final String qwandaServiceUrl,
                                                                                   Map<String, Object> decodedToken, final String token, final String parentCode, final String linkCode) {

        BaseEntity[] beArray = getBaseEntitysArrayByParentAndLinkCodeWithAttributes(qwandaServiceUrl, decodedToken,
                token, parentCode, linkCode);
        ArrayList<BaseEntity> arrayList = new ArrayList<BaseEntity>(Arrays.asList(beArray));
        return arrayList;

    }

    /**
     *
     * @param qwandaServiceUrl
     * @param decodedToken
     * @param token
     * @param parentCode
     * @param linkCode
     * @param pageStart
     * @param pageSize
     * @return baseEntitys
     */
    public static List<BaseEntity> getBaseEntitysByParentAndLinkCodeWithAttributes(final String qwandaServiceUrl,
                                                                                   Map<String, Object> decodedToken, final String token, final String parentCode, final String linkCode,
                                                                                   final Integer pageStart, final Integer pageSize) {

        BaseEntity[] beArray = getBaseEntitysArrayByParentAndLinkCodeWithAttributes(qwandaServiceUrl, decodedToken,
                token, parentCode, linkCode, pageStart, pageSize);
        ArrayList<BaseEntity> arrayList = new ArrayList<BaseEntity>(Arrays.asList(beArray));
        return arrayList;

    }

    /**
     *
     * @param qwandaServiceUrl
     * @param decodedToken
     * @param token
     * @param parentCode
     * @param linkCode
     * @param pageStart
     * @param pageSize
     * @return baseEntitys
     */

    /* added because of bug */
    public static List<BaseEntity> getBaseEntitysByParentAndLinkCodeWithAttributes2(final String qwandaServiceUrl,
                                                                                    Map<String, Object> decodedToken, final String token, final String parentCode, final String linkCode,
                                                                                    final Integer pageStart, final Integer pageSize) {

        BaseEntity[] beArray = getBaseEntitysArrayByParentAndLinkCodeWithAttributes2(qwandaServiceUrl, decodedToken,
                token, parentCode, linkCode, pageStart, pageSize);
        ArrayList<BaseEntity> arrayList = new ArrayList<BaseEntity>(Arrays.asList(beArray));
        return arrayList;

    }

    /**
     *
     * @param qwandaServiceUrl
     * @param decodedToken
     * @param token
     * @param parentCode
     * @param linkCode
     * @param linkValue
     * @param pageStart
     * @param pageSize
     * @return baseEntitys
     */
    public static List<BaseEntity> getBaseEntitysByParentAndLinkCodeAndLinkValueWithAttributes(
            final String qwandaServiceUrl, Map<String, Object> decodedToken, final String token,
            final String parentCode, final String linkCode, final String linkValue, final Integer pageStart,
            final Integer pageSize) {

        BaseEntity[] beArray = getBaseEntitysArrayByParentAndLinkCodeAndLinkValueWithAttributes(qwandaServiceUrl,
                decodedToken, token, parentCode, linkCode, linkValue, pageStart, pageSize);
        ArrayList<BaseEntity> arrayList = new ArrayList<BaseEntity>(Arrays.asList(beArray));
        return arrayList;

    }

    /**
     *
     * @param qwandaServiceUrl
     * @param decodedToken
     * @param token
     * @param parentCode
     * @param linkCode
     * @return baseEntitys
     */
    public static List<BaseEntity> getBaseEntitysByChildAndLinkCodeWithAttributes(final String qwandaServiceUrl,
                                                                                  Map<String, Object> decodedToken, final String token, final String childCode, final String linkCode) {

        String beJson = getBaseEntitysJsonByChildAndLinkCodeWithAttributes(qwandaServiceUrl, decodedToken, token,
                childCode, linkCode);
        QDataBaseEntityMessage msg = JsonUtils.fromJson(beJson, QDataBaseEntityMessage.class);
        BaseEntity[] beArray = msg.getItems();
        ArrayList<BaseEntity> arrayList = new ArrayList<BaseEntity>(Arrays.asList(beArray));
        return arrayList;

    }

    /**
     *
     * @param qwandaServiceUrl
     * @param decodedToken
     * @param token
     * @param parentCode
     * @param linkCode
     * @param stakeholderCode
     * @return baseEntitys
     */
    public static List<BaseEntity> getBaseEntitysByParentAndLinkCodeWithAttributesAndStakeholderCode(
            final String qwandaServiceUrl, Map<String, Object> decodedToken, final String token,
            final String parentCode, final String linkCode, final String stakeholderCode) {
        if (parentCode.equalsIgnoreCase("GRP_NEW_ITEMS")) {
            println("Group New Items Debug");
        }
        println("stakeholderCode is :: " + stakeholderCode);
        String beJson = getBaseEntitysJsonByParentAndLinkCodeWithAttributesAndStakeholderCode(qwandaServiceUrl,
                decodedToken, token, parentCode, linkCode, stakeholderCode);
        QDataBaseEntityMessage msg = fromJson(beJson, QDataBaseEntityMessage.class);
        if (msg == null) {
            log.error("Error in fetching BE from Qwanda Service");
        } else {
            BaseEntity[] beArray = msg.getItems();
            ArrayList<BaseEntity> arrayList = new ArrayList<BaseEntity>(Arrays.asList(beArray));
            return arrayList;
        }
        return null; // TODO get exception =s in place

    }

    /**
     *
     * @param qwandaServiceUrl
     * @param decodedToken
     * @param token
     * @param parentCode
     * @param linkCode
     * @param stakeholderCode
     * @return baseEntitys
     */
    public static List<Link> getLinks(final String qwandaServiceUrl, Map<String, Object> decodedToken,
                                      final String token, final String parentCode, final String linkCode) {

        String linkJson = null;
        List<Link> linkList = null;

        try {

            linkJson = QwandaUtils.apiGet(
                    qwandaServiceUrl + "/qwanda/entityentitys/" + parentCode + "/linkcodes/" + linkCode + "/children",
                    token);
            return JsonUtils.fromJson(linkJson, new TypeToken<List<Link>>() {
            }.getType());

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // BaseEntity[] beArray = msg.getItems();
        // ArrayList<BaseEntity> arrayList = new
        // ArrayList<BaseEntity>(Arrays.asList(beArray));
        // return arrayList;
        return linkList;
    }

    public static QDataAttributeMessage loadAllAttributesIntoCache(final GennyToken token) {
        try {
            boolean cacheWorked = false;
            QDataAttributeMessage ret = null;
            String realm = token.getRealm();
            println("All the attributes about to become loaded ... for realm "+realm);
            JsonObject json = VertxUtils.readCachedJson(realm,"attributes",token.getToken());
            if ("ok".equals(json.getString("status"))) {
                //	println("LOADING ATTRIBUTES FROM CACHE!");
                QDataAttributeMessage attMsg = JsonUtils.fromJson(json.getString("value"), QDataAttributeMessage.class);
                ret = attMsg;
                Attribute[] attributeArray = attMsg.getItems();
                // Now set the return message to the DEF attributes (these are the ones used */
                if (!realmAttributeMap.containsKey(realm)) {
                	realmAttributeMap.put(realm, new ConcurrentHashMap<String,Attribute>());
                }
                Map<String,Attribute> attributeMap = realmAttributeMap.get(realm);
                for (Attribute attribute : attributeArray) {
                    attributeMap.put(attribute.getCode(), attribute);
                }
                if (!defAttributesMap.containsKey(realm)) {
                	//setUpDefs(token);                    	
                }
                ret = defAttributesMap.get(realm);
                if (ret == null) {
                	//setUpDefs(token);
                	//ret = defAttributesMap.get(realm);
                } else {
                	ret.setToken(token.getToken());
                }

               // realmAttributeMap.put(realm, attributeMap);
                println("All the attributes have been loaded in "+attributeMap.size()+" attributes");
            } else {
                println("LOADING ATTRIBUTES FROM API");
                String jsonString = QwandaUtils.apiGet(GennySettings.qwandaServiceUrl + "/qwanda/attributes", token.getToken());
                if (!StringUtils.isBlank(jsonString)) {

                	 VertxUtils.writeCachedJson(token.getRealm(), "attributes", jsonString, token.getToken());
                	 
                	 QDataAttributeMessage attMsg  = JsonUtils.fromJson(jsonString, QDataAttributeMessage.class);
                	 ret = attMsg;
                    Attribute[] attributeArray = attMsg.getItems();
 
                    if (!realmAttributeMap.containsKey(realm)) {
                    	realmAttributeMap.put(realm, new ConcurrentHashMap<String,Attribute>());
                    }
                    Map<String,Attribute> attributeMap = realmAttributeMap.get(realm);
      
                    
                    for (Attribute attribute : attributeArray) {
                        attributeMap.put(attribute.getCode(), attribute);
                    }
                   // realmAttributeMap.put(realm, attributeMap);
                   
                    if (!defAttributesMap.containsKey(realm)) {
                  //  	setUpDefs(token);                    	
                    }
                    ret = defAttributesMap.get(realm);
                    if (ret != null) {
                    	ret.setToken(token.getToken());
                    }

                    println("All the attributes have been loaded from api in" + attributeMap.size() + " attributes");
                } else {
                    log.error("NO ATTRIBUTES LOADED FROM API");
                }
            }

            return ret;
        } catch (Exception e) {
            log.error("Attributes API not available, exception:" + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    public static QDataAttributeMessage loadAllAttributesIntoCache(final String token) {
        return loadAllAttributesIntoCache(new GennyToken(token));
    }

    public static Map<String,BaseEntity> getDefMap(final GennyToken userToken) {
    	if ((defs == null) || (defs.isEmpty())) {
    		log.error("No defs present");
    	}
    		// Load in Defs
//    		try {
//				//setUpDefs(userToken);
//			} catch (BadDataException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//        }

        if (defs != null) {
            return defs.getOrDefault(userToken.getRealm(), null);
        }
        return Collections.emptyMap();
    }
    
 
    public static Attribute getAttribute(final String attributeCode, final String token) {
    	GennyToken gennyToken = new GennyToken(token);
    	return getAttribute(attributeCode, gennyToken);
    }
    
    public static Attribute getAttribute(final String attributeCode, final GennyToken gennyToken) {
    	String realm = gennyToken.getRealm();
    	if (!realmAttributeMap.containsKey(realm)) {
    		//loadAllAttributesIntoCache(gennyToken);
    	}
    	if (realmAttributeMap.get(gennyToken.getRealm())==null) {
    		loadAllAttributesIntoCache(gennyToken);
    	}
        Attribute ret = realmAttributeMap.get(gennyToken.getRealm()).get(attributeCode);
        if ((ret == null)&&(!attributeCode.startsWith("PRI_APP_"))) { // ignore the dynamic attributes
        	if (attributeCode.substring(3).startsWith("_")) {
            if (attributeCode.startsWith("SRT_") || attributeCode.startsWith("RAW_")) {
                ret = new AttributeText(attributeCode, attributeCode);
            } else {
            	   loadAllAttributesIntoCache(gennyToken);
                ret = realmAttributeMap.get(gennyToken.getRealm()).get(attributeCode);
                if (ret == null) {
                    log.error("Attribute NOT FOUND :"+realm+":"+attributeCode);
                }
            }
        	} else {
        		log.error("Bad Attribute --> "+attributeCode);
        	}
        }
        return ret;
    }


    public static String getChildren(final String sourceCode, final String linkCode, final String linkValue,
                                     String token) {

        try {
            String beJson = QwandaUtils.apiGet(GennySettings.qwandaServiceUrl + "/qwanda/entityentitys/" + sourceCode
                    + "/linkcodes/" + linkCode + "/children/" + linkValue, token);
            Link[] linkArray = RulesUtils.fromJson(beJson, Link[].class);
            if (linkArray.length > 0) {
                ArrayList<Link> arrayList = new ArrayList<Link>(Arrays.asList(linkArray));
                Link first = arrayList.get(0);
                RulesUtils.println("The Child BaseEnity code is   ::  " + first.getTargetCode());
                return first.getTargetCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static BaseEntity duplicateBaseEntity(BaseEntity oldBe, String prefix, String name, String qwandaUrl,
                                                 String token) {

        BaseEntity newBe = new BaseEntity(QwandaUtils.getUniqueId(prefix, oldBe.getCode()), name);

        println("Size of oldBe Links   ::   " + oldBe.getLinks().size());
        println("Size of oldBe Attributes   ::   " + oldBe.getBaseEntityAttributes().size());

        for (EntityEntity ee : oldBe.getLinks()) {
            ee.getLink().setSourceCode(newBe.getCode());
        }
        newBe.setLinks(oldBe.getLinks());

        // newBe.setBaseEntityAttributes(oldBe.getBaseEntityAttributes());
        println("New BE before hitting api  ::   " + newBe);

        String jsonBE = JsonUtils.toJson(newBe);
        try {
            // save BE
            QwandaUtils.apiPostEntity(qwandaUrl + "/qwanda/baseentitys", jsonBE, token);
        } catch (Exception e) {
            e.printStackTrace();
        }
        println(newBe.getCode());
        return newBe;

    }

}
