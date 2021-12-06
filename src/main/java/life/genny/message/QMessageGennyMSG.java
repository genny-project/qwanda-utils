package life.genny.message;

import java.lang.invoke.MethodHandles;
import org.apache.logging.log4j.Logger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.google.gson.annotations.Expose;
import java.util.concurrent.CopyOnWriteArrayList;

import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QMessage;
import life.genny.qwanda.message.QBaseMSGMessageType;
import life.genny.qwanda.message.QBaseMSGAttachment;
import life.genny.utils.VertxUtils;
import life.genny.qwandautils.ANSIColour;
import life.genny.utils.BaseEntityUtils;

public class QMessageGennyMSG extends QMessage {

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final String MESSAGE_TYPE = "MSG_MESSAGE";
	
	@Expose
	private String templateCode;
	
	@Expose
	private QBaseMSGMessageType[] messageTypeArr;
	
	@Expose
	private String[] recipientArr;
	
	@Expose
	private Map<String, String> messageContextMap;
	
	@Expose
	private List<QBaseMSGAttachment> attachmentList;
	
	@Expose
	private String[] to;
	/**
	 * @return the templateCode
	 */
	public String getTemplateCode() {
		return templateCode;
	}

	/**
	 * @param templateCode the templateCode to set
	 */
	public void setTemplateCode(String templateCode) {
		this.templateCode = templateCode;
	}

	/**
	 * @return the messageTypeArr
	 */
	public QBaseMSGMessageType[] getMessageTypeArr() {
		return messageTypeArr;
	}

	/**
	 * @param messageTypeArr the messageTypeArr to set
	 */
	public void setMessageTypeArr(QBaseMSGMessageType[] messageTypeArr) {
		this.messageTypeArr = messageTypeArr;
	}

	/**
	 * @return the recipientArr
	 */
	public String[] getRecipientArr() {
		return recipientArr;
	}

	/**
	 * @param recipientArr the recipientArr to set
	 */
	public void setRecipientArr(String[] recipientArr) {
		this.recipientArr = recipientArr;
	}

	/**
	 * @return the messageContextMap
	 */
	public Map<String, String> getMessageContextMap() {
		return messageContextMap;
	}

	/**
	 * @param messageContextMap the messageContextMap to set
	 */
	public void setMessageContextMap(Map<String, String> messageContextMap) {
		this.messageContextMap = messageContextMap;
	}
	
	/**
	 * @return the attachmentList
	 */
	public List<QBaseMSGAttachment> getAttachmentList() {
		return attachmentList;
	}

	/**
	 * @param attachmentList the attachmentList to set
	 */
	public void setAttachmentList(List<QBaseMSGAttachment> attachmentList) {
		this.attachmentList = attachmentList;
	}

	/**
	 * @return the to
	 */
	public String[] getTo() {
		return to;
	}

	/**
	 * @param to the to to set
	 */
	public void setTo(String[] to) {
		this.to = to;
	}

	public QMessageGennyMSG() {
		super("COM_MSG");
		this.messageTypeArr = new QBaseMSGMessageType[0];
		this.messageContextMap = new HashMap<String, String>();
		this.recipientArr = new String[0];
	}

	public QMessageGennyMSG(String templateCode) {
		super("COM_MSG");
		this.templateCode = templateCode;
		this.messageTypeArr = new QBaseMSGMessageType[0];
		this.messageContextMap = new HashMap<String, String>();
		this.recipientArr = new String[0];
	}

	public QMessageGennyMSG(QBaseMSGMessageType messageType) {
		super("COM_MSG");
		this.messageTypeArr = new QBaseMSGMessageType[]{ messageType };
		this.messageContextMap = new HashMap<String, String>();
		this.recipientArr = new String[0];
	}

	public QMessageGennyMSG(String msg_type, QBaseMSGMessageType[] messageType, String templateCode, Map<String, String> contextMap, String[] recipientArr) {
		super(msg_type);
		this.templateCode = templateCode;
		this.messageTypeArr = messageType;
		this.messageContextMap = contextMap;
		this.recipientArr = recipientArr;
	}
	
	
	public QMessageGennyMSG(String msg_type, QBaseMSGMessageType[] messageType, String templateCode, Map<String, String> contextMap, String[] recipientArr, List<QBaseMSGAttachment> attachmentList) {
		super(msg_type);
		this.templateCode = templateCode;
		this.messageTypeArr = messageType;
		this.messageContextMap = contextMap;
		this.recipientArr = recipientArr;
		this.attachmentList = attachmentList;
	}
	
	

	public QMessageGennyMSG(String msg_type, String templateCode, QBaseMSGMessageType[] messageTypeArr,
			Map<String, String> messageContextMap, List<QBaseMSGAttachment> attachmentList, String[] to) {
		super(msg_type);
		this.templateCode = templateCode;
		this.messageTypeArr = messageTypeArr;
		this.messageContextMap = messageContextMap;
		this.attachmentList = attachmentList;
		this.to = to;
	}
	
	

	public QMessageGennyMSG(String msg_type, String templateCode, QBaseMSGMessageType[] messageTypeArr,
			Map<String, String> messageContextMap, String[] to) {
		super(msg_type);
		this.templateCode = templateCode;
		this.messageTypeArr = messageTypeArr;
		this.messageContextMap = messageContextMap;
		this.to = to;
	}

	public void addMessageType(QBaseMSGMessageType messageType) {
		
		List<QBaseMSGMessageType> list = this.getMessageTypeArr() != null ? new CopyOnWriteArrayList<>(Arrays.asList(this.getMessageTypeArr())) : new CopyOnWriteArrayList<>();
		list.add(messageType);
		this.setMessageTypeArr(list.toArray(new QBaseMSGMessageType[0]));
	}

	public void setRecipient(String recipient) {
		this.recipientArr = new String[0];
		addRecipient(recipient);
	}

	public void setRecipient(BaseEntity recipient) {
		this.recipientArr = new String[0];
		addRecipient(recipient);
	}

	public void addRecipient(BaseEntity recipient) {
		if (recipient == null) {
			log.warn("RECIPIENT BE passed is NULL");
		} else {
			addRecipient("[\""+recipient.getCode()+"\"]");
		}
	}

	public void addRecipient(String recipient) {
		
		if (recipient == null) {
			log.warn("RECIPIENT passed is NULL");
		} else {
			List<String> list = this.getRecipientArr() != null ? new CopyOnWriteArrayList<>(Arrays.asList(this.getRecipientArr())) : new CopyOnWriteArrayList<>();
			list.add(recipient);
			this.setRecipientArr(list.toArray(new String[0]));
		}
	}

	public void addContext(String key, Object value) {
		if (value == null) {
			log.warn(key + " passed is NULL");
		} else {
			if (value.getClass().equals(BaseEntity.class)) {
				this.messageContextMap.put(key, ((BaseEntity) value).getCode());
			} else {
				this.messageContextMap.put(key, value.toString());
			}
		}
	}

	/* 
	 * Thought it unnecessary to rewrite all of these methods, 
	 * so the builder re-uses them instead.
	 */
	public QMessageGennyMSG(Builder builder) {
		super("COM_MSG");
		this.templateCode = builder.msg.templateCode;
		this.messageTypeArr = builder.msg.messageTypeArr;
		this.recipientArr = builder.msg.recipientArr;
		this.messageContextMap = builder.msg.messageContextMap;
		this.attachmentList = builder.msg.attachmentList;
		this.to = builder.msg.to;
	}

	public static class Builder {

		public QMessageGennyMSG msg;
		public BaseEntityUtils beUtils;

		public Builder(final String templateCode) {
			this.msg = new QMessageGennyMSG(templateCode);
		}

		public Builder(final String templateCode, BaseEntityUtils beUtils) {
			this.msg = new QMessageGennyMSG(templateCode);
			this.beUtils = beUtils;
		}

		public Builder addRecipient(BaseEntity recipient) {
			this.msg.addRecipient(recipient);
			return this;
		}

		public Builder addRecipient(String recipient) {
			this.msg.addRecipient(recipient);
			return this;
		}

		public Builder addContext(String key, Object value) {
			this.msg.addContext(key, value);
			return this;
		}

		public Builder addMessageType(QBaseMSGMessageType messageType) {
			this.msg.addMessageType(messageType);
			return this;
		}

		public Builder setToken(String token) {
			this.msg.setToken(token);
			return this;
		}

		public Builder setUtils(BaseEntityUtils beUtils) {
			this.beUtils = beUtils;
			return this;
		}

		public QMessageGennyMSG send() {

			if (this.beUtils == null) {
				log.error(ANSIColour.RED+"No beUtils set for message. Cannot send!!!" + ANSIColour.RESET);
				return this.msg;
			}
			// Check if template code is present
			if (this.msg.getTemplateCode() == null) {
				log.warn(ANSIColour.YELLOW+"Message does not contain a Template Code!!"+ANSIColour.RESET);
			} else {
				// Make sure template exists
				BaseEntity templateBE = beUtils.getBaseEntityByCode(this.msg.getTemplateCode());

				if (templateBE == null) {
					log.error(ANSIColour.RED+"Message template " + this.msg.getTemplateCode() + " does not exist!!"+ANSIColour.RESET);
					return this.msg;
				}

				// // Find any required contexts for template
				// String contextListString = templateBE.getValue("PRI_CONTEXT_LIST", "[]");
				// String[] contextArray = contextListString.replaceAll("[", "").replaceAll("]", "").replaceAll("\"", "").split(",");

				// if (!contextListString.equals("[]") && contextArray != null && contextArray.length > 0) {
				// 	// Check that all required contexts are present
				// 	boolean containsAllContexts = Arrays.stream(contextArray).allMatch(item -> this.msg.getMessageContextMap().containsKey(item));

				// 	if (!containsAllContexts) {
				// 		log.error(ANSIColour.RED+"Msg does not contain all required contexts : " + contextArray.toString() + ANSIColour.RESET);
				// 		return this.msg;
				// 	}
				// }
			}

			// Set Msg Type to DEFAULT if none set already
			if (this.msg.messageTypeArr.length == 0) {
				this.msg.addMessageType(QBaseMSGMessageType.DEFAULT);
			}
			// Set token and send
			this.msg.setToken(beUtils.getGennyToken().getToken());
			VertxUtils.writeMsg("messages", this.msg);
			return this.msg;
		}

	}


	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "QMessageGennyMSG [templateCode=" + templateCode + ", messageTypeArr=" + messageTypeArr
				+ ", recipientArr=" + Arrays.toString(recipientArr) + ", messageContextMap=" + messageContextMap
				+ ", attachmentList=" + attachmentList + ", to=" + Arrays.toString(to) + "]";
	}

}
