package life.genny.qwandautils;

import life.genny.message.QMessageGennyMSG;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QBaseMSGAttachment;
import life.genny.qwanda.message.QBaseMSGAttachment.AttachmentType;
import life.genny.qwanda.message.QBaseMSGMessageType;
import life.genny.qwanda.message.QMSGMessage;
import life.genny.utils.BaseEntityUtils;
import life.genny.utils.VertxUtils;
import org.apache.logging.log4j.Logger;

import java.lang.invoke.MethodHandles;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class MessageUtils {

    protected static final Logger log = org.apache.logging.log4j.LogManager
            .getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());


    public static QMSGMessage prepareEmailTemplate(String templateCode, String BEGCode, String recipientLinkValue,
                                                   String[] attachments, String token) {

        return prepareTemplate(QBaseMSGMessageType.EMAIL, templateCode, BEGCode, recipientLinkValue, attachments, token);
    }

    public static QMSGMessage prepareSMSTemplate(String templateCode, String BEGCode, String recipientLinkValue,
                                                 String[] attachments, String token) {

        return prepareTemplate(QBaseMSGMessageType.SMS, templateCode, BEGCode, recipientLinkValue, attachments, token);
    }

    public static QMSGMessage prepareToastTemplate(String templateCode, String BEGCode, String recipientLinkValue,
                                                   String[] attachments, String token) {

        return prepareTemplate(QBaseMSGMessageType.TOAST, templateCode, BEGCode, recipientLinkValue, attachments, token);
    }

    public static QMSGMessage prepareVoiceTemplate(String templateCode, String BEGCode, String recipientLinkValue,
                                                   String[] attachments, String token) {

        return prepareTemplate(QBaseMSGMessageType.VOICE, templateCode, BEGCode, recipientLinkValue, attachments, token);
    }

    public static QMSGMessage prepareTemplate(QBaseMSGMessageType messageType, String templateCode, String BEGCode, String recipientLinkValue,
                                              String[] attachments, String token) {

        String code = "code:" + BEGCode;
        String recipient = "recipient:" + recipientLinkValue;
        String[] msgMessageData = {code, recipient};

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


        QBaseMSGMessageType[] messageTypeArr = {messageType};
        QMessageGennyMSG msgMessage = new QMessageGennyMSG("MSG_MESSAGE", messageTypeArr, templateCode, contextMap,
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


        QBaseMSGMessageType[] messageTypeArr = {messageType};
        QMessageGennyMSG msgMessage = new QMessageGennyMSG("MSG_MESSAGE", messageTypeArr, templateCode, contextMap,
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

        QBaseMSGMessageType[] messageTypeArr = {messageType};
        QMessageGennyMSG msgMessage = new QMessageGennyMSG(messageType.name(), templateCode, messageTypeArr, contextMap, to);
        msgMessage.setToken(token);

        log.info("------------------------------------------------------------------------");
        log.info("MESSAGE ::   " + msgMessage.toString());
        log.info("------------------------------------------------------------------------");


        return msgMessage;
    }


    public static QMessageGennyMSG prepareMessageTemplateWithAttachmentForDirectRecipients(String templateCode, QBaseMSGMessageType messageType,
                                                                                           Map<String, String> contextMap, String[] to, List<QBaseMSGAttachment> attachmentList,
                                                                                           String token) {

        QBaseMSGMessageType[] messageTypeArr = {messageType};
        QMessageGennyMSG msgMessage = new QMessageGennyMSG(messageType.name(), templateCode, messageTypeArr, contextMap, attachmentList, to);
        msgMessage.setToken(token);

        log.info("------------------------------------------------------------------------");
        log.info("MESSAGE ::   " + msgMessage);
        log.info("------------------------------------------------------------------------");


        return msgMessage;
    }

    public static String encodeUrl(String base, String parentCode, String code, String targetCode) {
        return encodeUrl(base, parentCode, code, targetCode, null);
    }

    public static String encodeUrl(String base, String parentCode, String code, String targetCode, String token) {
        /**
         * A Function for Base64 encoding urls
         **/

        // Encode Parent and Code
        String encodedParentCode = new String(Base64.getEncoder().encode(parentCode.getBytes()));
        String encodedCode = new String(Base64.getEncoder().encode(code.getBytes()));
        String url = base + "/" + encodedParentCode + "/" + encodedCode;

        // Add encoded targetCode if not null
        if (targetCode != null) {
            String encodedTargetCode = new String(Base64.getEncoder().encode(targetCode.getBytes()));
            url = url + "/" + encodedTargetCode;
        }

        // Add access token if not null
        if (token != null) {
            url = url + "?token=" + token;
        }
        return url;
    }

    public static void sendMessage(BaseEntityUtils beUtils, QBaseMSGMessageType type, BaseEntity recipient, String body, String style) {
        sendMessage(beUtils, type, recipient.getCode(), body, style);
    }

    public static void sendMessage(BaseEntityUtils beUtils, QBaseMSGMessageType type, String recipient, String body, String style)
    /**
     * Used to send on-the-fly messages such as SMS or Toast
     * */
    {
        QMessageGennyMSG msg = new QMessageGennyMSG(type);
        if (body != null) {
            msg.addContext("BODY", body);
        }
        if (style != null) {
            msg.addContext("STYLE", style);
        }
        msg.addRecipient(recipient);
        sendMessage(beUtils, msg);
    }

    /**
     * Used to send a message to the messages service.
     */
    public static void sendMessage(BaseEntityUtils beUtils, QMessageGennyMSG msg) {
        // Check if template code is present
        if (msg.getTemplateCode() == null) {
            log.warn(ANSIColour.YELLOW + "Message does not contain a Template Code!!" + ANSIColour.RESET);
        } else {
            // Make sure template exists
            BaseEntity templateBE = beUtils.getBaseEntityByCode(msg.getTemplateCode());

            if (templateBE == null) {
                log.error(ANSIColour.RED + "Message template " + msg.getTemplateCode() + " does not exist!!" + ANSIColour.RESET);
                return;
            }

            // // Find any required contexts for template
            // String contextListString = templateBE.getValue("PRI_CONTEXT_LIST", "[]");
            // String[] contextArray = contextListString.replaceAll("[", "").replaceAll("]", "").replaceAll("\"", "").split(",");

            // if (!contextListString.equals("[]") && contextArray != null && contextArray.length > 0) {
            // 	// Check that all required contexts are present
            // 	boolean containsAllContexts = Arrays.stream(contextArray).allMatch(item -> msg.getMessageContextMap().containsKey(item));

            // 	if (!containsAllContexts) {
            // 		log.error(ANSIColour.RED+"Msg does not contain all required contexts : " + contextArray.toString() + ANSIColour.RESET);
            // 		return;
            // 	}
            // }
        }
        // Set token and send
        msg.setToken(beUtils.getGennyToken().getToken());
        VertxUtils.writeMsg("messages", msg);
    }

    /**
     * Uses String Builder Pattern
     *
     * @param base
     * @param parentCode
     * @param code
     * @param targetCode
     * @return
     */
    public static String encodedUrlBuilder(String base, String parentCode, String code, String targetCode) {
        return encodedUrlBuilder(base, parentCode, code, targetCode, null);
    }

    /**
     * Uses StringBuilder Pattern
     *
     * @param base
     * @param parentCode
     * @param code
     * @param targetCode
     * @param token
     * @return
     */
    public static String encodedUrlBuilder(String base, String parentCode, String code, String targetCode, String token) {
        /**
         * A Function for Base64 encoding urls
         **/
        StringBuilder url = new StringBuilder();
        // Encode Parent and Code
        url
                .append(base)
                .append("/")
                .append(Base64.getEncoder().encodeToString(parentCode.getBytes()))
                .append("/")
                .append(Base64.getEncoder().encodeToString(code.getBytes()));

        // Add encoded targetCode if not null
        if (targetCode != null) {
            url
                    .append("/")
                    .append(Base64.getEncoder().encodeToString(targetCode.getBytes()));
        }

        // Add access token if not null
        if (token != null) {
            url
                    .append("?token=")
                    .append(token);
        }
        return url.toString();
    }

}
