package life.genny.qwandautils;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import io.vertx.core.json.JsonObject;
import life.genny.qwanda.message.QBaseMSGAttachment;
import life.genny.qwanda.message.QBaseMSGAttachment.AttachmentType;
import life.genny.qwanda.message.QBaseMSGMessageType;
import life.genny.qwanda.message.QMSGMessage;
import life.genny.qwanda.message.QMessageGennyMSG;

public class MessageUtils {

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());



	public static QMSGMessage prepareEmailTemplate(String templateCode, String BEGCode, String recipientLinkValue,
			String[] attachments, String token) {

		return prepareTemplate(QBaseMSGMessageType.EMAIL,templateCode, BEGCode, recipientLinkValue, attachments, token);		
	}
	
	public static QMSGMessage prepareSMSTemplate(String templateCode, String BEGCode, String recipientLinkValue,
			String[] attachments, String token) {

		return prepareTemplate(QBaseMSGMessageType.SMS,templateCode, BEGCode, recipientLinkValue, attachments, token);		
	}
	
	public static QMSGMessage prepareToastTemplate(String templateCode, String BEGCode, String recipientLinkValue,
			String[] attachments, String token) {

		return prepareTemplate(QBaseMSGMessageType.TOAST,templateCode, BEGCode, recipientLinkValue, attachments, token);		
	}
	
	public static QMSGMessage prepareVoiceTemplate(String templateCode, String BEGCode, String recipientLinkValue,
			String[] attachments, String token) {

		return prepareTemplate(QBaseMSGMessageType.VOICE,templateCode, BEGCode, recipientLinkValue, attachments, token);		
	}

	public static QMSGMessage prepareTemplate(QBaseMSGMessageType messageType, String templateCode, String BEGCode, String recipientLinkValue,
			String[] attachments, String token) {

		String code = "code:" + BEGCode;
		String recipient = "recipient:" + recipientLinkValue;
		String[] msgMessageData = { code, recipient };

		// Creating messageData MSG_MESSAGE
		QMSGMessage msgMessage = new QMSGMessage("MSG_MESSAGE", templateCode, msgMessageData, messageType,
				attachments);
		msgMessage.setToken(token);

		log.info("------------------------------------------------------------------------");
		log.info("MESSAGE TO OWNER   ::   " + msgMessage);
		log.info("------------------------------------------------------------------------");


		return msgMessage;
	}

	
	public static QMessageGennyMSG prepareMessageTemplate(String templateCode, QBaseMSGMessageType messageType,
			Map<String, String> contextMap, String[] recipientArray, String token) {


		QMessageGennyMSG msgMessage = new QMessageGennyMSG("MSG_MESSAGE", messageType, templateCode, contextMap,
				recipientArray);
		msgMessage.setToken(token);

		log.info("------------------------------------------------------------------------");
		log.info("MESSAGE ::   " + msgMessage);
		log.info("------------------------------------------------------------------------");


		return msgMessage;
	}

	public static QMessageGennyMSG prepareMessageTemplateWithAttachments(String templateCode, QBaseMSGMessageType messageType,
			Map<String, String> contextMap, String[] recipientArray, List<QBaseMSGAttachment> attachmentList,
			String token) {


		QMessageGennyMSG msgMessage = new QMessageGennyMSG("MSG_MESSAGE", messageType, templateCode, contextMap,
				recipientArray, attachmentList);
		msgMessage.setToken(token);

		log.info("------------------------------------------------------------------------");
		log.info("MESSAGE ::   " + msgMessage);
		log.info("------------------------------------------------------------------------");


		return msgMessage;
	}

	public static QBaseMSGAttachment prepareAttachment(String attachmentType, String contentType, String attachmentUrl,
			Boolean isMergeRequired, String attachmentPrefixName) {

		QBaseMSGAttachment attachment = null;
		Boolean isMergeNeeded = false;

		/* Handle attachment type parameter */
		AttachmentType type = null;
		if (attachmentType.equalsIgnoreCase("inline")) {
			type = AttachmentType.INLINE;
		} else if (attachmentType.equalsIgnoreCase("non-inline")) {
			type = AttachmentType.NON_INLINE;
		} else {
			type = AttachmentType.NON_INLINE;
		}

		/* Handle null conditions for isMergeRequired parameter */
		if (isMergeRequired == null) {
			isMergeNeeded = false;
		} else {
			isMergeNeeded = isMergeRequired;
		}

		if (attachmentUrl != null) {

			/* Create instance for Attachment */
			attachment = new QBaseMSGAttachment(type, contentType, attachmentUrl, isMergeNeeded, attachmentPrefixName);
		}

		return attachment;
	}

	public static QMessageGennyMSG prepareMessageTemplateForDirectRecipients(String templateCode, QBaseMSGMessageType messageType,
			Map<String, String> contextMap, String[] to, String token) {

		QMessageGennyMSG msgMessage = new QMessageGennyMSG(messageType.name(), templateCode, messageType, contextMap, to);
		msgMessage.setToken(token);

		log.info("------------------------------------------------------------------------");
		log.info("MESSAGE ::   " + msgMessage.toString());
		log.info("------------------------------------------------------------------------");


		return msgMessage;
	}
	

	
	public static QMessageGennyMSG prepareMessageTemplateWithAttachmentForDirectRecipients(String templateCode, QBaseMSGMessageType messageType,
			Map<String, String> contextMap, String[] to, List<QBaseMSGAttachment> attachmentList,
			String token) {

		QMessageGennyMSG msgMessage = new QMessageGennyMSG(messageType.name(), templateCode, messageType, contextMap, attachmentList, to);
		msgMessage.setToken(token);

		log.info("------------------------------------------------------------------------");
		log.info("MESSAGE ::   " + msgMessage);
		log.info("------------------------------------------------------------------------");


		return msgMessage;
	}

}
