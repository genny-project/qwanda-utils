package life.genny.models;

import life.genny.qwanda.entity.BaseEntity;

public class QFetchMailSettings {
	
	private BaseEntity sourceBaseEntity;
	private String mailHost;
	private String emailUsername;
	private String emailPassword;
	private String imapNew;
	private String imapProcessed;
	private String imapError;
	
	private String[] column2attributeMapping;
	
	private QFetchMailSettings() {}

	/**
	 * @param sourceBaseEntityCode
	 * @param mailHost
	 * @param emailUsername
	 * @param emailPassword
	 * @param imapNew
	 * @param imapProcessed
	 * @param imapError
	 * @param column2attributeMapping
	 */
	public QFetchMailSettings(BaseEntity sourceBaseEntity, String mailHost, String emailUsername, String emailPassword,
			String imapNew, String imapProcessed, String imapError) {
		this(sourceBaseEntity, mailHost, emailUsername, emailPassword, imapNew, imapProcessed, imapError,new String[0]);
	}
	
	/**
	 * @param sourceBaseEntityCode
	 * @param mailHost
	 * @param emailUsername
	 * @param emailPassword
	 * @param imapNew
	 * @param imapProcessed
	 * @param imapError
	 * @param column2attributeMapping
	 */
	public QFetchMailSettings(BaseEntity sourceBaseEntity, String mailHost, String emailUsername, String emailPassword,
			String imapNew, String imapProcessed, String imapError, String[] column2attributeMapping) {
		this.sourceBaseEntity = sourceBaseEntity;
		this.mailHost = mailHost;
		this.emailUsername = emailUsername;
		this.emailPassword = emailPassword;
		this.imapNew = imapNew;
		this.imapProcessed = imapProcessed;
		this.imapError = imapError;
		this.column2attributeMapping = column2attributeMapping;
	}

	/**
	 * @return the sourceBaseEntity
	 */
	public BaseEntity getSourceBaseEntity() {
		return sourceBaseEntity;
	}

	/**
	 * @return the mailHost
	 */
	public String getMailHost() {
		return mailHost;
	}

	/**
	 * @return the emailUsername
	 */
	public String getEmailUsername() {
		return emailUsername;
	}

	/**
	 * @return the emailPassword
	 */
	public String getEmailPassword() {
		return emailPassword;
	}

	/**
	 * @return the imapNew
	 */
	public String getImapNew() {
		return imapNew;
	}

	/**
	 * @return the imapProcessed
	 */
	public String getImapProcessed() {
		return imapProcessed;
	}

	/**
	 * @return the imapError
	 */
	public String getImapError() {
		return imapError;
	}

	/**
	 * @return the column2attributeMapping
	 */
	public String[] getColumn2attributeMapping() {
		return column2attributeMapping;
	}



	

}
