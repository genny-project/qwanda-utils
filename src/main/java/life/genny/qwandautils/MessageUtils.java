package life.genny.qwandautils;

import java.lang.invoke.MethodHandles;

import org.apache.logging.log4j.Logger;

import io.vertx.core.json.JsonObject;
import life.genny.qwanda.message.QBaseMSGMessageType;
import life.genny.qwanda.message.QMSGMessage;

public class MessageUtils {
	
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	public static JsonObject prepareSMSTemplate(String templateCode, String BEGCode, String recipientLinkValue,
			String[] attachments, String token) {

		String code = "code:" + BEGCode;
		String recipient = "recipient:" + recipientLinkValue;
		String[] msgMessageData = { code, recipient };

		// Creating messageData MSG_MESSAGE
		QMSGMessage msgMessage = new QMSGMessage("MSG_MESSAGE", templateCode, msgMessageData, QBaseMSGMessageType.SMS,
				attachments);
		JsonObject jsonMessage = new JsonObject().mapFrom(msgMessage);

		log.info("------------------------------------------------------------------------");
		log.info("MESSAGE TO OWNER   ::   " + jsonMessage.toString());
		log.info("------------------------------------------------------------------------");

		jsonMessage.put("token", token);

		return jsonMessage;
	}

	public static JsonObject prepareEmailTemplate(String templateCode, String BEGCode, String recipientLinkValue,
			String[] attachments, String token) {

		String code = "code:" + BEGCode;
		String recipient = "recipient:" + recipientLinkValue;
		String[] msgMessageData = { code, recipient };

		// Creating messageData MSG_MESSAGE
		QMSGMessage msgMessage = new QMSGMessage("MSG_MESSAGE", templateCode, msgMessageData, QBaseMSGMessageType.EMAIL,
				attachments);
		JsonObject jsonMessage = new JsonObject().mapFrom(msgMessage);

		log.info("------------------------------------------------------------------------");
		log.info("MESSAGE TO OWNER   ::   " + jsonMessage.toString());
		log.info("------------------------------------------------------------------------");

		jsonMessage.put("token", token);

		return jsonMessage;
	}

}