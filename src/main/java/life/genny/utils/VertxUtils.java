package life.genny.utils;

import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;
import com.google.gson.reflect.TypeToken;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.eventbus.MessageProducer;
import life.genny.eventbus.EventBusInterface;
import life.genny.eventbus.EventBusMock;
import life.genny.message.QMessageGennyMSG;
import life.genny.models.GennyToken;
import life.genny.qwanda.Answer;
import life.genny.qwanda.Ask;
import life.genny.qwanda.datatype.DataType;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.SearchEntity;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.message.*;
import life.genny.qwandautils.GennyCacheInterface;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.qwandautils.ANSIColour;
import life.genny.utils.BaseEntityUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.logging.log4j.Logger;

import javax.naming.NamingException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import java.time.LocalDateTime;
import java.time.LocalDate;

public class VertxUtils {

    protected static final Logger log = org.apache.logging.log4j.LogManager
            .getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

    static public boolean cachedEnabled = false;

    static public EventBusInterface eb;

    static final String DEFAULT_TOKEN = "DUMMY";
    static final String[] DEFAULT_FILTER_ARRAY = { "PRI_FIRSTNAME", "PRI_LASTNAME", "PRI_MOBILE", "PRI_IMAGE_URL",
            "PRI_CODE", "PRI_NAME", "PRI_USERNAME" };

    public enum ESubscriptionType {
        DIRECT, TRIGGER;

    }

    static public List<Answer> answerBuffer = new ArrayList<Answer>();

    public static GennyCacheInterface cacheInterface = null;

    public static GennyCacheInterface getCacheInterface()
    {
        return cacheInterface;
    }

    public static void init(EventBusInterface eventBusInterface, GennyCacheInterface gennyCacheInterface) {
        if (gennyCacheInterface == null) {
            log.error("NULL CACHEINTERFACE SUPPLUED IN INIT");
        }
        eb = eventBusInterface;
        cacheInterface = gennyCacheInterface;
        if (eb instanceof EventBusMock) {
            GennySettings.forceCacheApi = true;
            GennySettings.forceEventBusApi = true;
        }

    }

    static Map<String, String> localCache = new ConcurrentHashMap<String, String>();
    static Map<String, MessageProducer<JsonObject>> localMessageProducerCache = new ConcurrentHashMap<String, MessageProducer<JsonObject>>();

    static public void setRealmFilterArray(final String realm, final String[] filterArray) {
        putStringArray(realm, "FILTER", "PRIVACY", filterArray);
    }

    static public String[] getRealmFilterArray(final String realm) {
        String[] result = getStringArray(realm, "FILTER", "PRIVACY");
        if (result == null) {
            return DEFAULT_FILTER_ARRAY;
        } else {
            return result;
        }
    }

    static public <T> T getObject(final String realm, final String keyPrefix, final String key, final Class clazz) {
        return getObject(realm, keyPrefix, key, clazz, DEFAULT_TOKEN);
    }

    static public <T> T getObject(final String realm, final String keyPrefix, final String key, final Class clazz,
                                  final String token) {
        T item = null;
        String prekey = (StringUtils.isBlank(keyPrefix)) ? "" : (keyPrefix + ":");
        JsonObject json = readCachedJson(realm, prekey + key, token);
        if (json.getString("status").equalsIgnoreCase("ok")) {
            String data = json.getString("value");
            try {
                item = (T) JsonUtils.fromJson(data, clazz);
            } catch (Exception e) {
                log.error("Bad JsonUtils " + realm + ":" + key + ":" + clazz.getTypeName());
            }
            return item;
        } else {
            return null;
        }

    }

    static public <T> T getObject(final String realm, final String keyPrefix, final String key, final Type clazz) {
        return getObject(realm, keyPrefix, key, clazz, DEFAULT_TOKEN);
    }

    static public <T> T getObject(final String realm, final String keyPrefix, final String key, final Type clazz,
                                  final String token) {
        T item = null;
        String prekey = (StringUtils.isBlank(keyPrefix)) ? "" : (keyPrefix + ":");
        JsonObject json = readCachedJson(realm, prekey + key, token);
        if (json.getString("status").equalsIgnoreCase("ok")) {
            String data = json.getString("value");
            try {
                item = (T) JsonUtils.fromJson(data, clazz);
            } catch (Exception e) {
                log.info("Bad JsonUtils " + realm + ":" + key + ":" + clazz.getTypeName());
            }
            return item;
        } else {
            return null;
        }
    }

    static public JsonObject putObject(final String realm, final String keyPrefix, final String key, final Object obj) {
        return putObject(realm, keyPrefix, key, obj, DEFAULT_TOKEN);
    }

    static public JsonObject putObject(final String realm, final String keyPrefix, final String key, final Object obj,
                                 final String token) {
        String data = JsonUtils.toJson(obj);
        String prekey = (StringUtils.isBlank(keyPrefix)) ? "" : (keyPrefix + ":");

        return writeCachedJson(realm, prekey + key, data, token);
    }

    static public void clearCache(final String realm, final String token)
    {
        if (!GennySettings.forceCacheApi) {
            cacheInterface.clear(realm);
        } else {
            if (cachedEnabled) {
                localCache.clear();
            } else {
                try {
                    QwandaUtils.apiGet(GennySettings.ddtUrl + "/clear/" + realm , token);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    static public JsonObject readCachedJson(final String realm, final String key) {
        return readCachedJson(realm, key, DEFAULT_TOKEN);
    }

    static public JsonObject readCachedJson(String realm, final String key, final String token) {
        JsonObject result = null;

        if (!GennySettings.forceCacheApi) {
            String ret = null;
            try {
                // log.info("VERTX READING DIRECTLY FROM CACHE! USING
                // "+(GennySettings.isCacheServer?" LOCAL DDT":"CLIENT "));
                if (key == null) {
                    log.error("The key needed for the cache to retrieve an entry is null");

                    return null;
                }
                ret = (String) cacheInterface.readCache(realm, key, token);
            } catch (Exception e) {
                log.error("Cache is  null maybe the realm is not provided or was the wrong realm. The realm is a follows::: " + realm + " ::: if nothing appears within the colons the realm is a empty string");
                e.printStackTrace();
            }
            if (ret != null) {
                result = new JsonObject().put("status", "ok").put("value", ret);
            } else {
                result = new JsonObject().put("status", "error").put("value", ret);
            }
        } else {
            String resultStr = null;
            try {
                //	log.info("VERTX READING FROM CACHE API!");
                if (cachedEnabled) {
                    if ("DUMMY".equals(token)) {
                        // leave realm as it
                    } else {
                        GennyToken temp = new GennyToken(token);
                        realm = temp.getRealm();
                    }

                    resultStr = (String) localCache.get(realm + ":" + key);
                    if ((resultStr != null) && (!"\"null\"".equals(resultStr))) {
                        String resultStr6 = null;
                        if (false) {
                            // ugly way to fix json
                            resultStr6  = VertxUtils.fixJson(resultStr);
                        } else {
                            resultStr6 = resultStr;
                        }
                        // JsonObject rs2 = new JsonObject(resultStr6);
                        // String resultStr2 = resultStr.replaceAll("\\","");
                        // .replace("\\\"","\"");
                        JsonObject resultJson = new JsonObject().put("status", "ok").put("value", resultStr6);
                        resultStr = resultJson.toString();
                    } else {
                        resultStr = null;
                    }

                } else {
                    log.debug(" DDT URL:" + GennySettings.ddtUrl + ", realm:" + realm + "key:" + key + "token:" + token );
                    //resultStr = QwandaUtils.apiGet(GennySettings.ddtUrl + "/service/cache/read/" + realm + "/" + key, token);
                    int count=5;
                    boolean resultFound = false;
                    while (count > 0) {
                        resultStr = QwandaUtils.apiGet(GennySettings.ddtUrl + "/service/cache/read/" + realm + "/" + key, token);
                        if (resultStr == null
                        || ("<html><head><title>Error</title></head><body>Not Found</body></html>".equals(resultStr))
                        || ("<html><body><h1>Resource not found</h1></body></html>".equals(resultStr))) {
                            log.error("Count:" + count  + ", can't find key:" + key + " from cache, response:" + resultStr);
                            count--;
                        } else {
                            count = 0;
                            resultFound = true;
                        }
                    }
                    // result not found, set result to null for next if check
                    if(!resultFound)  resultStr = null;
//					if (resultStr==null) {
//						resultStr = QwandaUtils.apiGet(GennySettings.ddtUrl + "/read/" + realm + "/" + key, token);
//					}
//					resultStr =  readFromDDT(realm, key, token).toString();
                }
                if (resultStr != null) {
                    try {
                        result = new JsonObject(resultStr);
                    } catch (Exception e) {
                        log.error("JsonDecode Error "+resultStr);
                    }
                } else {
                    result = new JsonObject().put("status", "error");
                }

            } catch (IOException e) {
                log.error("Could not read " + key + " from cache");
            }

        }

        return result;
    }

//    static public JsonObject writeCachedJson(final String realm, final String key, final String value) {
//        log.debug("The realm provided to writeCachedJson is :::" + realm);
//        return writeCachedJson(realm, key, value, DEFAULT_TOKEN);
//    }

    static public JsonObject writeCachedJson(final String realm, final String key, final String value,
                                             final String token) {
        log.debug("The realm provided to writeCachedJson is :::" + realm);
        return writeCachedJson(realm, key, value, token, 0L);
    }

    static public JsonObject writeCachedJson(String realm, final String key, String value, final String token,
                                             long ttl_seconds) {
        log.debug("The realm provided to writeCachedJson is :::" + realm);
        if (!GennySettings.forceCacheApi) {
            cacheInterface.writeCache(realm, key, value, token, ttl_seconds);
        } else {
            try {
                if (cachedEnabled) {
                    // force
                    if ("DUMMY".equals(token)) {

                    } else {
                        GennyToken temp = new GennyToken(token);
                        realm = temp.getRealm();
                        log.info("A temporal realm was provided :::" + realm + "::: realm is within the colons");
                    }
                    if (value == null) {
                        localCache.remove(realm + ":" + key);
                    } else {
                        localCache.put(realm + ":" + key, value);
                    }
                } else {

                    log.debug("WRITING TO CACHE USING API! " + key);
                    JsonObject json = new JsonObject();

//					json.put("key", key);
//					json.put("json", value);
//					json.put("ttl", ttl_seconds + "");
//					QwandaUtils.apiPostEntity(GennySettings.ddtUrl + "/service/cache/write/"+key, json.toString(), token);
                    QwandaUtils.apiPostEntity2(GennySettings.ddtUrl + "/service/cache/write/"+key, value, token,null);
                }
            } catch (IOException e) {
                e.printStackTrace();
                JsonObject error = new JsonObject().put("status", "error");
                error.put("stackTrace", e.getStackTrace());
                error.put("errorType", e.toString());
                return error;
            }
        }

        JsonObject ok = new JsonObject().put("status", "ok");
        return ok;

    }

    static public void clearDDT(String realm) {

        cacheInterface.clear(realm);
    }


    static public <T extends BaseEntity> T  readFromDDT(String realm, final String code, final boolean withAttributes,
                                                        final String token, Class clazz) {
        T be = null;
        if (StringUtils.isBlank(token)) {
        	log.error("TOKEN is null for "+code);
        	return null;
        }
        if (StringUtils.isBlank(code)) {
        	log.error("code is null for readFromDDT");
        	return null;
        }


        JsonObject json = readCachedJson(realm, code, token);

        if ("ok".equals(json.getString("status"))) {
            be = JsonUtils.fromJson(json.getString("value"), clazz);
            if (be != null)  {
                if ( be.getCode() == null) {
                    log.error("readFromDDT baseEntity for realm " +
                            realm + " has null code! json is [" + json.getString("value") + "]");
                }
                else {
                    be.setFromCache(true);
                }
            }
        } else {
            // fetch normally
            // log.info("Cache MISS for " + code + " with attributes in realm " +
            // realm);
            if (cachedEnabled) {
                log.debug("Local Cache being used.. this is NOT production");
                // force
                GennyToken temp = new GennyToken(token);
                realm = temp.getRealm();
                String ddtvalue = localCache.get(realm + ":" + code);
                if (ddtvalue == null) {
                    return null;
                } else {
                    be = JsonUtils.fromJson(ddtvalue, BaseEntity.class);
                }
            } else {

                try {
                  //  if (withAttributes) {
                        be = QwandaUtils.getBaseEntityByCodeWithAttributes(code, token);
                 //   } else {
                  //      be = QwandaUtils.getBaseEntityByCode(code, token);
                  //  }
                } catch (Exception e) {
                    // Okay, this is bad. Usually the code is not in the database but in keycloak
                    // So lets leave it to the rules to sort out... (new user)
                    log.error( "BE " + code + " for realm " + realm + " is NOT IN CACHE OR DB " + e.getLocalizedMessage());
                    return null;

                }

                if (be != null)
                    writeCachedJson(realm, code, JsonUtils.toJson(be),token);
            }
            if (be!=null)
                be.setFromCache(false);
            else
                log.warn(String.format("BaseEntity: %s fetched is null", code));
        }
        return be;
    }


    static public <T extends BaseEntity> T  readFromDDT(String realm, final String code, final boolean withAttributes,
                                                        final String token) {
        return readFromDDT(realm, code, withAttributes,token,BaseEntity.class);
    }

    static boolean cacheDisabled = GennySettings.noCache;

    static public <T extends BaseEntity> T readFromDDT(final String realm, final String code, final String token) {
        // if ("PER_SHARONCROW66_AT_GMAILCOM".equals(code)) {
        // log.info("DEBUG");
        // }

        return readFromDDT(realm, code, true, token);

    }

    static public void subscribeAdmin(final String realm, final String adminUserCode) {
        final String SUBADMIN = "SUBADMIN";
        // Subscribe to a code
        Set<String> adminSet = getSetString(realm, SUBADMIN, "ADMINS");
        adminSet.add(adminUserCode);
        putSetString(realm, SUBADMIN, "ADMINS", adminSet);
    }

    static public void unsubscribeAdmin(final String realm, final String adminUserCode) {
        final String SUBADMIN = "SUBADMIN";
        // Subscribe to a code
        Set<String> adminSet = getSetString(realm, SUBADMIN, "ADMINS");
        adminSet.remove(adminUserCode);
        putSetString(realm, SUBADMIN, "ADMINS", adminSet);
    }

    static public void subscribe(final String realm, final String subscriptionCode, final String userCode) {
        final String SUB = "SUB";
        // Subscribe to a code
        Set<String> subscriberSet = getSetString(realm, SUB, subscriptionCode);
        subscriberSet.add(userCode);
        putSetString(realm, SUB, subscriptionCode, subscriberSet);
    }

    static public void subscribe(final String realm, final List<BaseEntity> watchList, final String userCode) {
        final String SUB = "SUB";
        // Subscribe to a code
        for (BaseEntity be : watchList) {
            Set<String> subscriberSet = getSetString(realm, SUB, be.getCode());
            subscriberSet.add(userCode);
            putSetString(realm, SUB, be.getCode(), subscriberSet);
        }
    }

    static public void subscribe(final String realm, final BaseEntity be, final String userCode) {
        final String SUB = "SUB";
        // Subscribe to a code
        Set<String> subscriberSet = getSetString(realm, SUB, be.getCode());
        subscriberSet.add(userCode);
        putSetString(realm, SUB, be.getCode(), subscriberSet);

    }

    /*
     * Subscribe list of users to the be
     */
    static public void subscribe(final String realm, final BaseEntity be, final String[] SubscribersCodeArray) {
        final String SUB = "SUB";
        // Subscribe to a code
        // Set<String> subscriberSet = getSetString(realm, SUB, be.getCode());
        // subscriberSet.add(userCode);
        Set<String> subscriberSet = new HashSet<String>(Arrays.asList(SubscribersCodeArray));
        putSetString(realm, SUB, be.getCode(), subscriberSet);

    }

    static public void unsubscribe(final String realm, final String subscriptionCode, final Set<String> userSet) {
        final String SUB = "SUB";
        // Subscribe to a code
        Set<String> subscriberSet = getSetString(realm, SUB, subscriptionCode);
        subscriberSet.removeAll(userSet);

        putSetString(realm, SUB, subscriptionCode, subscriberSet);
    }

    static public String[] getSubscribers(final String realm, final String subscriptionCode) {
        final String SUB = "SUB";
        // Subscribe to a code
        String[] resultArray = getObject(realm, SUB, subscriptionCode, String[].class);

        String[] resultAdmins = getObject(realm, "SUBADMIN", "ADMINS", String[].class);
        String[] result = (String[]) ArrayUtils.addAll(resultArray, resultAdmins);
        return result;

    }

    static public void subscribeEvent(final String realm, final String subscriptionCode, final QEventMessage msg) {
        final String SUBEVT = "SUBEVT";
        // Subscribe to a code
        Set<String> subscriberSet = getSetString(realm, SUBEVT, subscriptionCode);
        subscriberSet.add(JsonUtils.toJson(msg));
        putSetString(realm, SUBEVT, subscriptionCode, subscriberSet);
    }

    static public QEventMessage[] getSubscribedEvents(final String realm, final String subscriptionCode) {
        final String SUBEVT = "SUBEVT";
        // Subscribe to a code
        String[] resultArray = getObject(realm, SUBEVT, subscriptionCode, String[].class);
        QEventMessage[] msgs = new QEventMessage[resultArray.length];
        int i = 0;
        for (String result : resultArray) {
            msgs[i] = JsonUtils.fromJson(result, QEventMessage.class);
            i++;
        }
        return msgs;
    }

    static public Set<String> getSetString(final String realm, final String keyPrefix, final String key) {
        String[] resultArray = getObject(realm, keyPrefix, key, String[].class);
        log.info("realm provided to getSetString is :::" + realm );
        if (resultArray == null) {
            return new HashSet<String>();
        }
        return Sets.newHashSet(resultArray);
    }

    static public JsonObject putSetString(final String realm, final String keyPrefix, final String key, final Set set) {
        String[] strArray = (String[]) FluentIterable.from(set).toArray(String.class);
        return putObject(realm, keyPrefix, key, strArray);
    }

    static public JsonObject putStringArray(final String realm, final String keyPrefix, final String key,
                                      final String[] string) {
        return putObject(realm, keyPrefix, key, string);
    }

    static public String[] getStringArray(final String realm, final String keyPrefix, final String key) {
        String[] resultArray = getObject(realm, keyPrefix, key, String[].class);
        if (resultArray == null) {
            return null;
        }

        return resultArray;
    }

    static public JsonObject putMap(final String realm, final String keyPrefix, final String key,
                              final Map<String, String> map) {
        return putObject(realm, keyPrefix, key, map);
    }

    static public Map<String, String> getMap(final String realm, final String keyPrefix, final String key) {
        Type type = new TypeToken<Map<String, String>>() {
        }.getType();
        Map<String, String> myMap = getObject(realm, keyPrefix, key, type);
        return myMap;
    }

    public static void putMessageProducer(String sessionState, MessageProducer<JsonObject> toSessionChannel) {
        localMessageProducerCache.put(sessionState, toSessionChannel);

    }

    public static MessageProducer<JsonObject> getMessageProducer(String sessionState) {

        return localMessageProducerCache.get(sessionState);

    }

    static public void publish(BaseEntity user, String channel, Object payload) {

        publish(user, channel, payload, DEFAULT_FILTER_ARRAY);
    }

    static public JsonObject publish(BaseEntity user, String channel, Object payload, final String[] filterAttributes) {

        eb.publish(user, channel, payload, filterAttributes);

        JsonObject ok = new JsonObject().put("status", "ok");
        return ok;

    }

    static public void publish(BaseEntity user, String channel, BaseEntity be, String aliasCode, String token) {

        QDataBaseEntityMessage msg = new QDataBaseEntityMessage(be, aliasCode);
        msg.setToken(token);
        eb.publish(user, channel, msg, null);
    }

    static public JsonObject writeMsg(String channel, Object payload) {
        JsonObject result = null;
        Set<String> rxList = new HashSet<String>();
        String token = null;

        if ((payload instanceof String)||(payload == null)) {
            if ("null".equals((String)payload)) {
                return new JsonObject().put("status", "error");
            }
        }

        if ("webdata".equals(channel) || "webcmds".equals(channel)|| "events".equals(channel)|| "data".equals(channel)) {
			// This is a standard session only
		} else if ("search_events".equals(channel)) {
			payload = JsonUtils.toJson(payload);
		} else 	if ("messages".equals(channel)) {
			String pl = null;
			if (payload instanceof String) { // done in sending
				log.info("payload is String  j ="+payload);
				String js = (String)payload;
				js = js.replaceAll(",\"eventbus\":\"WRITE\"", "");
//				JsonObject j = JsonUtils.fromJson((String)payload, JsonObject.class);
//				log.info("payload is String JsonObject j ="+j);
//				if (j.containsKey("eventbus")) {
//					j.remove("eventbus");
//				}
				log.info("payload after eventbus removal  is String JsonObject j ="+js);
				pl = js;
				System.out.println("Sending to Messages from Wildfly-Qwanda-Service junit rx :"+pl);
				
			} else {
				log.info("payload is QMessageGennyMSG  j ="+((QMessageGennyMSG)payload));

				pl = JsonUtils.toJson((QMessageGennyMSG)payload); // done in Qwanda-service
				System.out.println("Sending to Messages from junit and normal messages send");
			}
				 payload = pl;
	             channel = "messages";
        } else {
            // This looks like we are sending data to a subscription channel

            if (payload instanceof String) {
            	log.info("payload = ["+payload+"]");
                JsonObject msg = (JsonObject) new JsonObject((String)payload);
                log.info(msg.getValue("event_type"));
                JsonArray jsonArray = msg.getJsonArray("recipientCodeArray");

                jsonArray.forEach(object -> {
                    if (object instanceof JsonObject) {
                        JsonObject jsonObject = (JsonObject) object;
                        rxList.add(jsonObject.toString());
                    } else 	 if (object instanceof String) {
                        rxList.add(object.toString());
                    }

                });

                rxList.add(channel);
                JsonArray finalArray = new JsonArray();

                for (String ch : rxList) {
                    finalArray.add(ch);
                }
                token = msg.getString("token");
                msg.put("recipientCodeArray", finalArray);
                log.info("Writing to channels "+finalArray);
                payload = msg.toString();
                channel = "webdata";

            }
            else if  (payload instanceof QDataBaseEntityMessage) {
                QDataBaseEntityMessage msg = (QDataBaseEntityMessage) payload;
                rxList.add(channel);
                String[] rx = msg.getRecipientCodeArray();
                if (rx != null) {
                    Set<String> rx2 = Arrays.stream(rx).collect(Collectors.toSet());
                    rxList.addAll(rx2);
                }
                rx = rxList.toArray(new String[0]);
                msg.setRecipientCodeArray(rx);
                log.info("Writing to channels "+rx);
                token = msg.getToken();
                channel = "webdata";
            } else if (payload instanceof QBulkMessage) {
                QBulkMessage msg = (QBulkMessage) payload;
                rxList.add(channel);
                String[] rx = msg.getRecipientCodeArray();
                if (rx != null) {
                    Set<String> rx2 = Arrays.stream(rx).collect(Collectors.toSet());
                    rxList.addAll(rx2);
                }
                rx = rxList.toArray(new String[0]);
                msg.setRecipientCodeArray(rx);
                log.info("Writing to channels "+rx);
                token = msg.getToken();
                channel = "webdata";
            } else if (payload instanceof JsonObject) {
                JsonObject msg = (JsonObject) payload;
                log.info(msg.getValue("code"));
                JsonArray jsonArray = msg.getJsonArray("recipientCodeArray");

                jsonArray.forEach(object -> {
                    if (object instanceof JsonObject) {
                        JsonObject jsonObject = (JsonObject) object;
                        rxList.add(jsonObject.toString());
                    } else 	 if (object instanceof String) {
                        rxList.add(object.toString());
                    }

                });

                rxList.add(channel);
                JsonArray finalArray = new JsonArray();

                for (String ch : rxList) {
                    finalArray.add(ch);
                }

                msg.put("recipientCodeArray", finalArray);
                log.info("Writing to channels "+finalArray);
                token = msg.getString("token");
                payload = msg;
                channel = "webdata";

            }

        }

        try {
            eb.writeMsg(channel, payload);
            if (!rxList.isEmpty()) {
                // send ends
                writeMsgEnd(new GennyToken(token),rxList);
            }
        } catch (NamingException e) {
            e.printStackTrace();
        }

        result = new JsonObject().put("status", "ok");
        return result;

    }


	/*
	static public JsonObject writeMsgScheduled(String channel, final String jsonMessage, BaseEntity source, String cron, final GennyToken userToken) throws IOException {

		QScheduleMessage scheduleMessage = new QScheduleMessage(jsonMessage, source.getCode(), channel, cron, userToken.getRealm());

		QwandaUtils.apiPostEntity(GennySettings.scheduleServiceUrl , jsonMessage, userToken.getToken());


	}
	 */



    static public JsonObject writeMsg(String channel, BaseEntity baseentity, String aliasCode) {
        QDataBaseEntityMessage msg = new QDataBaseEntityMessage(baseentity, aliasCode);
        return writeMsg(channel, msg);
    }


    static public void writeMsgEnd(GennyToken userToken) {
        QCmdMessage msgend = new QCmdMessage("END_PROCESS", "END_PROCESS");
        msgend.setToken(userToken.getToken());
        msgend.setSend(true);
        VertxUtils.writeMsg("webcmds", msgend);
    }

    static public void writeMsgEnd(GennyToken userToken,Set<String> rxSet) {
        QCmdMessage msgend = new QCmdMessage("END_PROCESS", "END_PROCESS");
        msgend.setToken(userToken.getToken());
        msgend.setSend(true);
        String[] rxArray = rxSet.toArray(new String[0]);
        msgend.setRecipientCodeArray(rxArray);
        VertxUtils.writeMsg("project", msgend);
    }

    static public QEventMessage sendEvent(final String code)
    {
        QEventMessage msg = new QEventMessage("UPDATE",code);
        writeMsg("events",msg);
        return msg;
    }


    static public QEventMessage sendEvent(final String code, final String source, final String target, final GennyToken serviceToken)
    {
        QEventMessage msg = new QEventMessage("UPDATE",code);
        MessageData data = new MessageData(code);
        data.setParentCode(source);
        data.setTargetCode(target);
        msg.setData(data);
        msg.setToken(serviceToken.getToken());
        writeMsg("events",msg);
        return msg;
    }

	public static List<String> getSearchColumnFilterArray(SearchEntity searchBE)
	{
		List<String> attributeFilter = new ArrayList<String>();
		List<String> assocAttributeFilter = new ArrayList<String>();

		for (EntityAttribute ea : searchBE.getBaseEntityAttributes()) {
			String attributeCode = ea.getAttributeCode();
			if (attributeCode.startsWith("COL_") || attributeCode.startsWith("CAL_")) {
				if (attributeCode.equals("COL_PRI_ADDRESS_FULL")) {
					attributeFilter.add("PRI_ADDRESS_LATITUDE");
					attributeFilter.add("PRI_ADDRESS_LONGITUDE");
				}
				if (attributeCode.startsWith("COL__")) {
					String[] splitCode = attributeCode.substring("COL__".length()).split("__");
					assocAttributeFilter.add(splitCode[0]);
				} else {
				attributeFilter.add(attributeCode.substring("COL_".length()));
				}
			}
		}
		attributeFilter.addAll(assocAttributeFilter);
		return attributeFilter;
	}

    static public Object privacyFilter(BaseEntity user, Object payload, final String[] filterAttributes) {
        if (payload instanceof QDataBaseEntityMessage) {
            return JsonUtils.toJson(privacyFilter(user, (QDataBaseEntityMessage) payload,
                    new HashMap<String, BaseEntity>(), filterAttributes));
        } else if (payload instanceof QBulkMessage) {
            return JsonUtils.toJson(privacyFilter(user, (QBulkMessage) payload, filterAttributes));
        } else
            return payload;
    }

    static public QDataBaseEntityMessage privacyFilter(BaseEntity user, QDataBaseEntityMessage msg,
                                                       Map<String, BaseEntity> uniquePeople, final String[] filterAttributes) {
        ArrayList<BaseEntity> bes = new ArrayList<BaseEntity>();
        for (BaseEntity be : msg.getItems()) {
            if (uniquePeople != null && be.getCode() != null && !uniquePeople.containsKey(be.getCode())) {

                be = privacyFilter(user, be, filterAttributes);
                uniquePeople.put(be.getCode(), be);
                bes.add(be);
            } else {
                /*
                 * Avoid sending the attributes again for the same BaseEntity, so sending
                 * without attributes
                 */
                BaseEntity slimBaseEntity = new BaseEntity(be.getCode(), be.getName());
                /*
                 * Setting the links again but Adam don't want it to be send as it increasing
                 * the size of BE. Frontend should create links based on the parentCode of
                 * baseEntity not the links. This requires work in the frontend. But currently
                 * the GRP_NEW_ITEMS are being sent without any links so it doesn't show any
                 * internships.
                 */
                slimBaseEntity.setLinks(be.getLinks());
                bes.add(slimBaseEntity);
            }
        }
        msg.setItems(bes.toArray(new BaseEntity[bes.size()]));
        return msg;
    }

    static public QBulkMessage privacyFilter(BaseEntity user, QBulkMessage msg, final String[] filterAttributes) {
        Map<String, BaseEntity> uniqueBes = new HashMap<String, BaseEntity>();
        for (QDataBaseEntityMessage beMsg : msg.getMessages()) {
            beMsg = privacyFilter(user, beMsg, uniqueBes, filterAttributes);
        }
        return msg;
    }

    static public BaseEntity privacyFilter(BaseEntity user, BaseEntity be) {
        final String[] filterStrArray = { "PRI_FIRSTNAME", "PRI_LASTNAME", "PRI_MOBILE", "PRI_EMAIL", "PRI_PHONE",
                "PRI_IMAGE_URL", "PRI_CODE", "PRI_NAME", "PRI_USERNAME" };

        return privacyFilter(user, be, filterStrArray);
    }

    static public <T extends BaseEntity> T  privacyFilter(BaseEntity user, T be, final String[] filterAttributes) {
        Set<EntityAttribute> allowedAttributes = new HashSet<EntityAttribute>();
        for (EntityAttribute entityAttribute : be.getBaseEntityAttributes()) {
            // log.info("ATTRIBUTE:"+entityAttribute.getAttributeCode()+(entityAttribute.getPrivacyFlag()?"PRIVACYFLAG=TRUE":"PRIVACYFLAG=FALSE"));
            if (( (be.getCode().startsWith("PER_")) || be.getCode().startsWith("PRJ_"))   && (!be.getCode().equals(user.getCode()))) {
                String attributeCode = entityAttribute.getAttributeCode();

                if (Arrays.stream(filterAttributes).anyMatch(x -> x.equals(attributeCode))) {

                    allowedAttributes.add(entityAttribute);
                } else {
                    if (attributeCode.startsWith("PRI_IS_")) {
                        allowedAttributes.add(entityAttribute);// allow all roles

                    } else if (attributeCode.startsWith("LNK_")) {
                        allowedAttributes.add(entityAttribute);// allow attributes that starts with "LNK_"
                    }
                }
            } else {
                if (!entityAttribute.getPrivacyFlag()) { // don't allow privacy flag attributes to get through
                    allowedAttributes.add(entityAttribute);
                }
            }
            if (entityAttribute.getAttributeCode().equals("PRI_INTERVIEW_URL")) {
                log.info("My Interview");
            }
        }
        // Handle Created and Updated attributes
        if (Arrays.asList(filterAttributes).contains("PRI_CREATED")) {
            // log.info("filterAttributes contains PRI_CREATED");
            Attribute createdAttr = new Attribute("PRI_CREATED", "Created", new DataType(LocalDateTime.class));
            EntityAttribute created = new EntityAttribute(be, createdAttr, 1.0);
            created.setValueDateTime(be.getCreated());
            allowedAttributes.add(created);// allow attributes that starts with "LNK_"
        }
        if (Arrays.asList(filterAttributes).contains("PRI_CREATED_DATE")) {
            // log.info("filterAttributes contains PRI_CREATED_DATE");
            Attribute createdAttr = new Attribute("PRI_CREATED_DATE", "Created", new DataType(LocalDate.class));
            EntityAttribute created = new EntityAttribute(be, createdAttr, 1.0);
            created.setValueDate(be.getCreated().toLocalDate());
            allowedAttributes.add(created);// allow attributes that starts with "LNK_"
        }
        if (Arrays.asList(filterAttributes).contains("PRI_UPDATED")) {
            // log.info("filterAttributes contains PRI_UPDATED");
            Attribute updatedAttr = new Attribute("PRI_UPDATED", "Updated", new DataType(LocalDateTime.class));
            EntityAttribute updated = new EntityAttribute(be, updatedAttr, 1.0);
            updated.setValueDateTime(be.getUpdated());
            allowedAttributes.add(updated);// allow attributes that starts with "LNK_"
        }
        if (Arrays.asList(filterAttributes).contains("PRI_UPDATED_DATE")) {
            // log.info("filterAttributes contains PRI_UPDATED_DATE");
            Attribute updatedAttr = new Attribute("PRI_UPDATED_DATE", "Updated", new DataType(LocalDate.class));
            EntityAttribute updated = new EntityAttribute(be, updatedAttr, 1.0);
            updated.setValueDate(be.getUpdated().toLocalDate());
            allowedAttributes.add(updated);// allow attributes that starts with "LNK_"
        }
        be.setBaseEntityAttributes(allowedAttributes);

        return be;
    }

    static public BaseEntity privacyFilter(BaseEntity be, final String[] filterAttributes) {
        Set<EntityAttribute> allowedAttributes = new HashSet<EntityAttribute>();
        for (EntityAttribute entityAttribute : be.getBaseEntityAttributes()) {
            String attributeCode = entityAttribute.getAttributeCode();
            if (Arrays.stream(filterAttributes).anyMatch(x -> x.equals(attributeCode))) {
                allowedAttributes.add(entityAttribute);
            }
        }
        // Handle Created and Updated attributes
        if (Arrays.asList(filterAttributes).contains("PRI_CREATED")) {
            // log.info("filterAttributes contains PRI_CREATED");
            Attribute createdAttr = new Attribute("PRI_CREATED", "Created", new DataType(LocalDateTime.class));
            EntityAttribute created = new EntityAttribute(be, createdAttr, 1.0);
            created.setValueDateTime(be.getCreated());
            allowedAttributes.add(created);// allow attributes that starts with "LNK_"
        }
        if (Arrays.asList(filterAttributes).contains("PRI_CREATED_DATE")) {
            // log.info("filterAttributes contains PRI_CREATED_DATE");
            Attribute createdAttr = new Attribute("PRI_CREATED_DATE", "Created", new DataType(LocalDate.class));
            EntityAttribute created = new EntityAttribute(be, createdAttr, 1.0);
            created.setValueDate(be.getCreated().toLocalDate());
            allowedAttributes.add(created);// allow attributes that starts with "LNK_"
        }
        if (Arrays.asList(filterAttributes).contains("PRI_UPDATED")) {
            // log.info("filterAttributes contains PRI_UPDATED");
            Attribute updatedAttr = new Attribute("PRI_UPDATED", "Updated", new DataType(LocalDateTime.class));
            EntityAttribute updated = new EntityAttribute(be, updatedAttr, 1.0);
            updated.setValueDateTime(be.getUpdated());
            allowedAttributes.add(updated);// allow attributes that starts with "LNK_"
        }
        if (Arrays.asList(filterAttributes).contains("PRI_UPDATED_DATE")) {
            // log.info("filterAttributes contains PRI_UPDATED_DATE");
            Attribute updatedAttr = new Attribute("PRI_UPDATED_DATE", "Updated", new DataType(LocalDate.class));
            EntityAttribute updated = new EntityAttribute(be, updatedAttr, 1.0);
            updated.setValueDate(be.getUpdated().toLocalDate());
            allowedAttributes.add(updated);// allow attributes that starts with "LNK_"
        }
        be.setBaseEntityAttributes(allowedAttributes);

        return be;
    }

    public static Boolean checkIfAttributeValueContainsString(BaseEntity baseentity, String attributeCode,
                                                              String checkIfPresentStr) {

        Boolean isContainsValue = false;

        if (baseentity != null && attributeCode != null && checkIfPresentStr != null) {
            String attributeValue = baseentity.getValue(attributeCode, null);

            if (attributeValue != null && attributeValue.toLowerCase().contains(checkIfPresentStr.toLowerCase())) {
                return true;
            }
        }

        return isContainsValue;
    }

    public static String apiPostEntity(final String postUrl, final String entityString, final String authToken,
                                       final Consumer<String> callback) throws IOException {
        {
            String responseString = "ok";

            return responseString;
        }
    }

    public static String apiPostEntity(final String postUrl, final String entityString, final String authToken)
            throws IOException {
        {
            return apiPostEntity(postUrl, entityString, authToken, null);
        }
    }

    public static Set<String> fetchRealmsFromApi() {
        List<String> activeRealms = new ArrayList<String>();
        JsonObject ar = VertxUtils.readCachedJson(GennySettings.GENNY_REALM, "REALMS");
        String ars = ar.getString("value");

        if (ars == null) {
            try {
                ars = QwandaUtils.apiGet(GennySettings.fyodorServiceUrl + "/utils/realms", "NOTREQUIRED");
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Type listType = new TypeToken<List<String>>() {
        }.getType();
        ars = ars.replaceAll("\\\"", "\"");
        activeRealms = JsonUtils.fromJson(ars, listType);
        Set<String> realms = new HashSet<>(activeRealms);
        return realms;
    }

    static public String fixJson(String resultStr)
    {
        String resultStr2 = resultStr.replaceAll(Pattern.quote("\\\""),
                Matcher.quoteReplacement("\""));
        String resultStr3 = resultStr2.replaceAll(Pattern.quote("\\n"),
                Matcher.quoteReplacement("\n"));
        String resultStr4 = resultStr3.replaceAll(Pattern.quote("\\\n"),
                Matcher.quoteReplacement("\n"));
//		String resultStr5 = resultStr4.replaceAll(Pattern.quote("\"{"),
//				Matcher.quoteReplacement("{"));
//		String resultStr6 = resultStr5.replaceAll(Pattern.quote("\"["),
//				Matcher.quoteReplacement("["));
//		String resultStr7 = resultStr6.replaceAll(Pattern.quote("]\""),
//				Matcher.quoteReplacement("]"));
//		String resultStr8 = resultStr5.replaceAll(Pattern.quote("}\""), Matcher.quoteReplacement("}"));
        String ret = resultStr4.replaceAll(Pattern.quote("\\\""
                        + ""),
                Matcher.quoteReplacement("\""));
        return ret;

    }


    static public void sendFeedbackError(GennyToken userToken, Answer answer, String message)
    {
        VertxUtils.sendFeedback(userToken,answer,"ERROR",message);
    }

    static public void sendFeedbackWarning(GennyToken userToken, Answer answer, String message)
    {
        VertxUtils.sendFeedback(userToken,answer,"WARN",message);
    }
    static public void sendFeedbackSuspicious(GennyToken userToken, Answer answer, String message)
    {
        VertxUtils.sendFeedback(userToken,answer,"SUSPICIOUS",message);
    }
    static public void sendFeedbackHint(GennyToken userToken, Answer answer, String message)
    {
        VertxUtils.sendFeedback(userToken,answer,"HINT",message);
    }




    static public void sendFeedback(GennyToken userToken, Answer answer, String prefix, String message)
    {
        // find the baseentity
        BaseEntity be = VertxUtils.getObject(userToken.getRealm(), "", answer.getTargetCode(), BaseEntity.class,
                userToken.getToken());

        BaseEntity sendBe = new BaseEntity(be.getCode(),be.getName());
        sendBe.setRealm(userToken.getRealm());
        try {
            Attribute att = RulesUtils.getAttribute(answer.getAttributeCode(), userToken.getToken());
            sendBe.addAttribute(att);
            sendBe.setValue(att, answer.getValue());
            Optional<EntityAttribute> ea =sendBe.findEntityAttribute(answer.getAttributeCode());
            if (ea.isPresent()) {
                ea.get().setFeedback(prefix+":"+message);
                QDataBaseEntityMessage msg = new QDataBaseEntityMessage(sendBe);
                msg.setReplace(true);
                msg.setToken(userToken.getToken());
                VertxUtils.writeMsg("webcmds", msg);
            }
        } catch (BadDataException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    static public void sendToFrontEnd(GennyToken userToken, Answer... answers) {
        if ((answers.length > 0)) {
            // find the baseentity
            BaseEntity be = VertxUtils.getObject(userToken.getRealm(), "", answers[0].getTargetCode(), BaseEntity.class,
                    userToken.getToken());
            if (be != null) {

                BaseEntity newBe = new BaseEntity(be.getCode(), be.getName());
                newBe.setRealm(userToken.getRealm());

                for (Answer answer : answers) {

                    try {
                        Attribute att = RulesUtils.getAttribute(answer.getAttributeCode(), userToken.getToken());
                        newBe.addAttribute(att);
                        newBe.setValue(att, answer.getValue());
                    } catch (BadDataException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                QDataBaseEntityMessage msg = new QDataBaseEntityMessage(newBe);
                msg.setReplace(true);
                msg.setToken(userToken.getToken());
                VertxUtils.writeMsg("webcmds", msg);
            }
        }

    }

	static public void sendCmdMsg(BaseEntityUtils beUtils, String msgType, String code) 
	{
		sendCmdMsg(beUtils, msgType, code, null, null);
	}

	static public void sendCmdMsg(BaseEntityUtils beUtils, String msgType, String code, List<String> targetCodes) 
	{
		sendCmdMsg(beUtils, msgType, code, null, targetCodes);
	}

	static public void sendCmdMsg(BaseEntityUtils beUtils, String msgType, String code, String message) 
	{
		sendCmdMsg(beUtils, msgType, code, message, null);
	}

	static public void sendCmdMsg(BaseEntityUtils beUtils, String msgType, String code, String message, List<String> targetCodes) 
	{
		QCmdMessage msg = new QCmdMessage(msgType, code);
		msg.setToken(beUtils.getGennyToken().getToken());
		msg.setSend(true);  		
		if (message != null) {
			msg.setMessage(message);
		}
		if (targetCodes != null) {
			msg.setTargetCodes(targetCodes);
		}
		VertxUtils.writeMsg("webcmds",msg);
	}

	static public void sendAskMsg(BaseEntityUtils beUtils, Ask ask) 
	{
		QDataAskMessage msg = new QDataAskMessage(ask);
		msg.setToken(beUtils.getGennyToken().getToken());
		msg.setReplace(true);
		VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg));
	}

	static public void sendBaseEntityMsg(BaseEntityUtils beUtils, BaseEntity be) 
	{
		QDataBaseEntityMessage msg = new QDataBaseEntityMessage(be);
		msg.setToken(beUtils.getGennyToken().getToken());
		msg.setReplace(true);
		VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg));
	}

	static public void sendBaseEntityMsg(BaseEntityUtils beUtils, BaseEntity[] beArray) 
	{
		QDataBaseEntityMessage msg = new QDataBaseEntityMessage(beArray);
		msg.setToken(beUtils.getGennyToken().getToken());
		msg.setReplace(true);
		VertxUtils.writeMsg("webcmds", JsonUtils.toJson(msg));
	}

}
