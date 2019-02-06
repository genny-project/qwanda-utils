package life.genny.qwandautils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.invoke.MethodHandles;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import com.opencsv.CSVReader;
import com.sun.mail.util.MailSSLSocketFactory;

import life.genny.models.QFetchMailSettings;
import life.genny.qwanda.Answer;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeText;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.exception.BadDataException;


public class MailUtils
{
	/**
	 * Stores logger object.
	 */
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

  private static String getFrom(Message javaMailMessage) 
  throws MessagingException
  {
    String from = "";
    Address a[] = javaMailMessage.getFrom();
    if ( a==null ) return null;
    for ( int i=0; i<a.length; i++ )
    {
      Address address = a[i];
      from = from + address.toString();
    }

    return from;
  }

  private static String removeQuotes(String stringToModify)
  {
    int indexOfFind = stringToModify.indexOf(stringToModify);
    if ( indexOfFind < 0 ) return stringToModify;

    StringBuffer oldStringBuffer = new StringBuffer(stringToModify);
    StringBuffer newStringBuffer = new StringBuffer();
    for ( int i=0, length=oldStringBuffer.length(); i<length; i++ )
    {
      char c = oldStringBuffer.charAt(i);
      if ( c == '"' || c == '\'' )
      {
        // do nothing
      }
      else
      {
        newStringBuffer.append(c);
      }

    }
    return new String(newStringBuffer);
  }

	public static List<BaseEntity> readCSV(BaseEntity source, InputStream is, String msgId,final String filename, String[] mapping, final String bePrefix) {

		List<BaseEntity> baseEntitys = new ArrayList<BaseEntity>();

		Map<String, String> attributeCodeMap = new HashMap<String, String>();
		String[] attributeArray = new String[mapping.length];
		String[] columnArray = new String[mapping.length];
		int index = 0;
		for (String pair : mapping) {
			String[] split = pair.split(":");
			attributeCodeMap.put(StringUtils.trim(split[0]), StringUtils.trim(split[1]));
			columnArray[index] = StringUtils.trim(split[0]).replaceAll("\\p{C}", "?");
			attributeArray[index++] = StringUtils.trim(split[1]);
		}

		Reader reader = new InputStreamReader(is);
		CSVReader csvReader = new CSVReader(reader);

		// Reading Records One by One in a String array
		String[] nextRecord;
		int rowIndex = 0;
		try {
			while ((nextRecord = csvReader.readNext()) != null) {
				// check if header
				String firstColumnValue = nextRecord[0].replaceAll("\\p{C}", "");
				;
				firstColumnValue = StringUtils.trimToNull(firstColumnValue);
				char v = firstColumnValue.charAt(0);

				String columnName = StringUtils.trim(columnArray[0]);
				if (columnName.equalsIgnoreCase(firstColumnValue)) {
					log.info("Skipping Header:");
					log.info(nextRecord);
					continue; // skip header line
				}

				BaseEntity be = new BaseEntity(bePrefix+"_"+msgId+"-"+String.format("%03d", rowIndex), filename + "-" + rowIndex);
				int columnIndex = 0;
				for (String columnValue : nextRecord) {
					String value = StringUtils.trim(columnValue).replaceAll("\\p{C}", "?");
					if (columnIndex < attributeArray.length) {
						String attributeCode = attributeArray[columnIndex];
						// System.out.print(attributeCode+":"+value+",");
						Attribute mockAttribute = new AttributeText(attributeCode, columnArray[columnIndex]);
						Answer answer = new Answer(source, be, mockAttribute, value);
						try {
							be.addAnswer(answer);
						} catch (BadDataException e) {
							log.error("Bad Input data!:" + answer);
						}
						// create
						columnIndex++;
					} else {
						log.info("Bad index value [" + value + "]");
					}
				}
				baseEntitys.add(be);
				log.info();
				;
				rowIndex++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return baseEntitys;
	}
	
	public static List<BaseEntity> fetchMail(QFetchMailSettings settings, final String bePrefix) {
		
		List<BaseEntity> importBEs = new ArrayList<BaseEntity>();
		
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
    //    props.put("mail.imap.ssl.enable", true);
    //    props.setProperty("mail.imap.port", "993");
   //     props.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
   //     props.setProperty("mail.imap.socketFactory.fallback","true");
    //    props.setProperty("mail.imap.socketFactory.port", "993");
     //   props.setProperty("mail.imap.starttls.enable", "true");
        props.put("mail.smtp.starttls.enable",true);
        props.put("mail.smtp.auth", true);  // If you need to authenticate

        // establish Mapping
        

        Session session = null;
        Store store = null;
        Folder inbox = null;
        Folder processed = null;
        Folder error = null;
	    try
		{
			// Connect to the server
	    	MailSSLSocketFactory socketFactory= new MailSSLSocketFactory();
	        socketFactory.setTrustAllHosts(true);
	 //       props.put("mail.imaps.ssl.socketFactory", socketFactory);

			session = Session.getDefaultInstance(props, null);
			log.info("Host:"+settings.getMailHost()+" Username: "+settings.getEmailUsername()+" Password: ["+settings.getEmailPassword()+"]");
			log.info("PRE STORE");
			try {
				store = session.getStore("imaps");  // getStore("imaps")
			} catch (NoSuchProviderException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			log.info("Store Fetched");
			if (store == null) {
				log.info("Store is null!");
			}
			store.connect(settings.getMailHost(), settings.getEmailUsername(), settings.getEmailPassword());
			log.info("Store Connected");
			if (store.isConnected()) {
			inbox = store.getFolder(settings.getImapNew());
			processed = store.getFolder(settings.getImapProcessed());
			error = store.getFolder(settings.getImapError());
			
			inbox.open(Folder.READ_ONLY);
			int messageCount = inbox.getMessageCount();
			log.info("Number of messages in inbox = " + messageCount);

			Message[] messages = inbox.getMessages();
			log.info("------------------------------");

			for (int j = 0; j < messageCount; j++) {
				Message msg = messages[j];
				log.info("Message subject " + msg.getSubject());
	            log.info("From: " + msg.getFrom()[0]);
	           log.info("To: "+msg.getAllRecipients()[0]);
	            log.info("Date: "+msg.getReceivedDate());
	            log.info("Size: "+msg.getSize());
	            log.info(msg.getFlags());
	            log.info("Body: \n"+ msg.getContent());
	            log.info(msg.getContentType());
	            System.out.print(msg.getReceivedDate()); // Use this as unique id

				try {
					Multipart multipart = (Multipart) msg.getContent();
					log.info("\tMessage attachment count " + multipart.getCount());
					for (int i = 0; i < multipart.getCount(); i++) {
						BodyPart bodyPart = multipart.getBodyPart(i);
						if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())
								|| (StringUtils.isBlank(bodyPart.getFileName()))) {
							log.info("\tMessage has no attachments");
							continue; // dealing with attachments only

						}
						log.info("\tMessage attachment filename is " + bodyPart.getFileName());
						log.info("\tMessage attachment type is " + bodyPart.getContentType());
						InputStream is = bodyPart.getInputStream();
						Date rxDate = msg.getReceivedDate();
						String pattern = "yyyyMMddHHmmss";
						SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);

						String date = simpleDateFormat.format(rxDate);
						
						try {
							List<BaseEntity> importBEsFromEmail = readCSV(settings.getSourceBaseEntity(),is,date,bodyPart.getFileName(),settings.getColumn2attributeMapping(), bePrefix);
							importBEs.addAll(importBEsFromEmail);
							
							for (BaseEntity be : importBEs) {
								log.info(be);
								for (EntityAttribute ea : be.getBaseEntityAttributes()) {
									System.out.print(ea.getAttributeCode()+":"+ea.getValueString()+", ");
								}
								log.info();
							}

							Message[] processedMessages = new Message[1];
							processedMessages[0] = msg;
							inbox.copyMessages(processedMessages, processed);
						} catch (Exception e) {
							Message[] errorMessages = new Message[1];
							errorMessages[0] = msg;
							inbox.copyMessages(errorMessages, error);
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (MessagingException e) {
					e.printStackTrace();
				}
			}
			}
			else {
				log.info("Cannot connect to imap server");
			}
		} catch (Exception e)
	    {
	    	e.getStackTrace();
	    	log.info("Mail Fetch Exception "+e.getLocalizedMessage());
	
	  }
	    finally {
	    	 try {
	    		 if (inbox != null && inbox.isOpen()) { inbox.close(true); }
	             if (store != null) { store.close(); }
	             log.info("Closing IMAP");
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	      
	    }
	    
	    return importBEs;
}

	
	
}