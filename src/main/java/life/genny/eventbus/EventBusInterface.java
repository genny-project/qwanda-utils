package life.genny.eventbus;

import com.carrotsearch.sizeof.RamUsageEstimator;
import com.google.gson.JsonParser;
import io.vertx.core.json.JsonObject;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.NamingException;
import life.genny.models.GennyToken;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QBulkMessage;
import life.genny.qwanda.message.QBulkPullMessage;
import life.genny.qwanda.message.QCmdMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwanda.message.QMessage;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaMessage;
import life.genny.qwandautils.QwandaUtils;
import life.genny.utils.BaseEntityUtils;
import life.genny.utils.VertxUtils;
import org.apache.logging.log4j.Logger;

public interface EventBusInterface {
  static final Logger log =
      org.apache.logging.log4j.LogManager.getLogger(
          MethodHandles.lookup().lookupClass().getCanonicalName());

  public static Object privacyFilter(
      BaseEntity user, Object payload, final String[] filterAttributes) {
    if (payload instanceof QDataBaseEntityMessage) {
      return JsonUtils.toJson(
          privacyFilter(
              user,
              (QDataBaseEntityMessage) payload,
              new HashMap<String, BaseEntity>(),
              filterAttributes));
    } else if (payload instanceof QBulkMessage) {
      return JsonUtils.toJson(privacyFilter(user, (QBulkMessage) payload, filterAttributes));
    } else if (payload instanceof QwandaMessage) {
      return JsonUtils.toJson(privacyFilter(user, (QwandaMessage) payload, filterAttributes));
    } else return payload;
  }

  public static QDataBaseEntityMessage privacyFilter(
      BaseEntity user,
      QDataBaseEntityMessage msg,
      Map<String, BaseEntity> uniquePeople,
      final String[] filterAttributes) {
    ArrayList<BaseEntity> bes = new ArrayList<BaseEntity>();
    if (uniquePeople != null) {
      for (BaseEntity be : msg.getItems()) {
        if (be != null) {

          if (!uniquePeople.containsKey(be.getCode())) {
            if (be.getCode().equals(msg.getParentCode())) {
              // get the latest parent code from api to ensure links are ok?
              try {
                be = QwandaUtils.getBaseEntityByCode(be.getCode(), msg.getToken());
              } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
              }
            }

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
      }
      msg.setItems(bes.toArray(new BaseEntity[bes.size()]));
    }
    return msg;
  }

  public static QwandaMessage privacyFilter(
      BaseEntity user, QwandaMessage msg, final String[] filterAttributes) {
    privacyFilter(user, msg.askData, filterAttributes);
    return msg;
  }

  public static QBulkMessage privacyFilter(
      BaseEntity user, QBulkMessage msg, final String[] filterAttributes) {
    Map<String, BaseEntity> uniqueBes = new HashMap<String, BaseEntity>();
    for (QDataBaseEntityMessage beMsg : msg.getMessages()) {
      beMsg = privacyFilter(user, beMsg, uniqueBes, filterAttributes);
    }
    return msg;
  }

  public static BaseEntity privacyFilter(BaseEntity user, BaseEntity be) {
    final String[] filterStrArray = {
      "PRI_FIRSTNAME",
      "PRI_LASTNAME",
      "PRI_MOBILE",
      "PRI_DRIVER",
      "PRI_OWNER",
      "PRI_IMAGE_URL",
      "PRI_CODE",
      "PRI_NAME",
      "PRI_USERNAME",
      "PRI_DRIVER_RATING"
    };

    return privacyFilter(user, be, filterStrArray);
  }

  public static BaseEntity privacyFilter(
      BaseEntity user, BaseEntity be, final String[] filterAttributes) {
    Set<EntityAttribute> allowedAttributes = new HashSet<EntityAttribute>();
    for (EntityAttribute entityAttribute : be.getBaseEntityAttributes()) {
      // log.info("ATTRIBUTE:"+entityAttribute.getAttributeCode()+(entityAttribute.getPrivacyFlag()?"PRIVACYFLAG=TRUE":"PRIVACYFLAG=FALSE"));
      if ((be.getCode().startsWith("PER_")) && (!be.getCode().equals(user.getCode()))) {
        String attributeCode = entityAttribute.getAttributeCode();

        if (Arrays.stream(filterAttributes).anyMatch(x -> x.equals(attributeCode))) {

          allowedAttributes.add(entityAttribute);
        } else {
          if (attributeCode.startsWith("PRI_IS_")) {
            allowedAttributes.add(entityAttribute); // allow all roles
          }
          if (attributeCode.startsWith("LNK_")) {
            allowedAttributes.add(entityAttribute); // allow attributes that starts with "LNK_"
          }
        }
      } else {
        if (!entityAttribute
            .getPrivacyFlag()) { // don't allow privacy flag attributes to get through
          allowedAttributes.add(entityAttribute);
        }
      }
    }
    be.setBaseEntityAttributes(allowedAttributes);

    return be;
  }

  public static Boolean checkIfAttributeValueContainsString(
      BaseEntity baseentity, String attributeCode, String checkIfPresentStr) {

    Boolean isContainsValue = false;

    if (baseentity != null && attributeCode != null && checkIfPresentStr != null) {
      String attributeValue = baseentity.getValue(attributeCode, null);

      if (attributeValue != null
          && attributeValue.toLowerCase().contains(checkIfPresentStr.toLowerCase())) {
        return true;
      }
    }

    return isContainsValue;
  }

  public default void write(final String channel, final Object payload) throws NamingException {
    log.debug("MOCK EVT BUS WRITE: " + channel + ":" + payload);
  }

  public default void send(final String channel, final Object payload) throws NamingException {
    log.debug("MOCK EVT BUS SEND: " + channel + ":" + payload);
  }

  public default void writeMsg(final String channel, final Object msg) throws NamingException {

    if ((GennySettings.forceEventBusApi) && (!VertxUtils.cachedEnabled)) {
      try {
        String json = "";
        if (msg instanceof String) {
          String msgStr = (String) msg;
          if (msgStr.contains("PRI_HAS_ICON")) {
            // log.info("Bad thm_ico");
            QDataBaseEntityMessage qdbem = JsonUtils.fromJson(msgStr, QDataBaseEntityMessage.class);
            for (BaseEntity be : qdbem.getItems()) {
              for (EntityAttribute ea : be.getBaseEntityAttributes()) {
                if (ea.getValueBoolean() != null) {
                  ea.setValueString(null); // hacky fix
                }
              }
            }
            msgStr = JsonUtils.toJson(qdbem);
            json = VertxUtils.fixJson(msgStr);
            String resultStr2 =
                json.replaceAll(Pattern.quote("\\\""), Matcher.quoteReplacement("\""));
            String resultStr5 =
                resultStr2.replaceAll(Pattern.quote("\"{"), Matcher.quoteReplacement("{"));
            //						String resultStr6 = resultStr5.replaceAll(Pattern.quote("\"["),
            //								Matcher.quoteReplacement("["));
            //						String resultStr7 = resultStr6.replaceAll(Pattern.quote("]\""),
            //								Matcher.quoteReplacement("]"));
            json = resultStr5.replaceAll(Pattern.quote("}\""), Matcher.quoteReplacement("}"));
          } else {
            json = msgStr; // VertxUtils.fixJson(msgStr);
          }

        } else {

          json = JsonUtils.toJson(msg);
        }

        JsonObject event = (JsonObject) new JsonObject((String) json);
        event.put("eventbus", "WRITE");

        //				JsonParser parser = new JsonParser();
        //				com.google.gson.JsonObject event = parser.parse(json).getAsJsonObject();
        //				event.addProperty("eventbus", "WRITE");
        json = event.toString();
        QwandaUtils.apiPostEntity2(
            GennySettings.qwandaServiceUrl + "/service/messages/" + channel,
            json,
            event.getString("token"),
            null);
      } catch (Exception e) {
        String json2 = msg.toString();
        log.error("Error in posting message to bridge eventbus:" + channel + ":" + msg);
      }

    } else {

      if (GennySettings.multiBridgeMode
          && (("cmds".equals(channel)) || ("webcmds".equals(channel)))) {
        if (!(msg instanceof String)) {
          QMessage msg2 = (QMessage) msg;
          String token = msg2.getToken();
          GennyToken gToken = new GennyToken(token); // This is costly

          String sourceAddress =
              (String)
                  VertxUtils.getCacheInterface()
                      .readCache(gToken.getRealm(), gToken.getJTI(), token);
          log.info("Sending to bridge at " + sourceAddress);
          write(sourceAddress, msg);
        } else {
          write(channel, msg);
        }

      } else {
        if (!(msg instanceof String)) {

          if (msg instanceof QCmdMessage) {
            QCmdMessage msg2 = (QCmdMessage) msg;
            write(channel, JsonUtils.toJson(msg2));
          } else {
            QMessage msg2 = (QMessage) msg;
            String token = msg2.getToken();
            GennyToken gToken = new GennyToken(token); // This is costly

            // if the message is large then try BulkPull
            // if (msg instanceof QBulkMessage) {
            long msgSize = RamUsageEstimator.sizeOf(msg);
            if (GennySettings.bulkPull && (msgSize > GennySettings.pontoonMinimumThresholdBytes)) {
              log.info("WRITING PONTOON BULK PULL MESSAGE size=" + msgSize);
              BaseEntityUtils beUtils = new BaseEntityUtils(gToken);
              QBulkPullMessage qBulkPullMsg = beUtils.createQBulkPullMessage(msg2);
              write(channel, JsonUtils.toJson(qBulkPullMsg));
            } else {
              write(channel, JsonUtils.toJson(msg));
            }
          }
        } else {
          //
          //					String payload2 = js.toString();
          //					if (payload2 != null) {
          String str = (String) msg;
          if (GennySettings.bulkPull
              && (str.length() > GennySettings.pontoonMinimumThresholdBytes)) {
            long msgSize = RamUsageEstimator.sizeOf(msg);
            log.info("WRITING PONTOON BULK PULL MESSAGE size=" + msgSize);
            String json = msg.toString();
            JsonParser parser = new JsonParser();
            com.google.gson.JsonObject event = parser.parse(json).getAsJsonObject();
            GennyToken gToken = new GennyToken(event.get("token").getAsString());
            BaseEntityUtils beUtils = new BaseEntityUtils(gToken);
            QBulkPullMessage qBulkPullMsg = beUtils.createQBulkPullMessage(str);
            write(channel, JsonUtils.toJson(qBulkPullMsg));
          } else {
            write(channel, msg);
          }

          //					}
        }
      }
    }
  }

  public default void sendMsg(final String channel, final Object msg) throws NamingException {
    String json = msg.toString();
    JsonObject event = new JsonObject(json);

    if (GennySettings.forceEventBusApi) {
      try {
        event.put("eventbus", "SEND");
        QwandaUtils.apiPostEntity(
            GennySettings.bridgeServiceUrl + "?channel=" + channel, json, event.getString("token"));
      } catch (Exception e) {
        log.error("Error in posting message to bridge eventbus:" + event);
      }

    } else {
      if (GennySettings.multiBridgeMode
          && (("cmds".equals(channel)) || ("webcmds".equals(channel)))) {
        QMessage msg2 = (QMessage) msg;
        String token = msg2.getToken();
        GennyToken gToken = new GennyToken(token); // This is costly
        String sourceAddress =
            (String)
                VertxUtils.getCacheInterface().readCache(gToken.getRealm(), gToken.getJTI(), token);
        log.info("Sending to bridge at " + sourceAddress);
        send(sourceAddress, msg);
      } else {
        send(channel, msg);
      }
    }
  }

  public default void publish(
      BaseEntity user, String channel, Object payload, final String[] filterAttributes) {
    try {
      // Actually Send ....
      switch (channel) {
        case "event":
        case "events":
          sendMsg("events", payload);
          break;
        case "data":
          writeMsg("data", payload);
          break;

        case "webdata":
          payload = EventBusInterface.privacyFilter(user, payload, filterAttributes);
          writeMsg("webdata", payload);
          break;
        case "cmds":
        case "webcmds":
          payload = EventBusInterface.privacyFilter(user, payload, filterAttributes);
          writeMsg("webcmds", payload);
          break;
        case "services":
          writeMsg("services", payload);
          break;
        case "messages":
          writeMsg("messages", payload);
          break;
        case "statefulmessages":
          writeMsg("statefulmessages", payload);
          break;
        case "signals":
          writeMsg("signals", payload);
          break;

        default:
          payload = EventBusInterface.privacyFilter(user, payload, filterAttributes);
          sendMsg(channel, payload);
          // log.error("Channel does not exist: " + channel);
      }
    } catch (NamingException e) {
      e.printStackTrace();
    }
  }
}
