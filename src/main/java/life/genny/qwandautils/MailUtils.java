package life.genny.qwandautils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

import com.opencsv.CSVReader;

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

	public static List<BaseEntity> readCSV(BaseEntity source, InputStream is, final String filename, String[] mapping) {

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
					System.out.println("Skipping Header:");
					System.out.println(nextRecord);
					continue; // skip header line
				}

				BaseEntity be = new BaseEntity(QwandaUtils.getUniqueId("RAW"), filename + "-" + rowIndex);
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
						System.out.println("Bad index value [" + value + "]");
					}
				}
				baseEntitys.add(be);
				System.out.println();
				;
				rowIndex++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return baseEntitys;
	}
	
	public static List<BaseEntity> fetchMail(QFetchMailSettings settings) {
		
		List<BaseEntity> importBEs = new ArrayList<BaseEntity>();
		
        Properties props = System.getProperties();
        props.setProperty("mail.store.protocol", "imaps");
        
        // establish Mapping
        

        Session session = null;
        Store store = null;
        Folder inbox = null;
	    try
		{
			// Connect to the server
			session = Session.getDefaultInstance(props, null);
			store = session.getStore("imaps");
			store.connect(settings.getMailHost(), settings.getEmailUsername(), settings.getEmailPassword());

			inbox = store.getFolder(settings.getImapNew());
			inbox.open(Folder.READ_ONLY);
			int messageCount = inbox.getMessageCount();
			System.out.println("Number of messagesa in inbox = " + messageCount);

			Message[] messages = inbox.getMessages();
			System.out.println("------------------------------");

			for (int j = 0; j < messageCount; j++) {
				Message msg = messages[j];
				System.out.println("Message subject " + msg.getSubject());
	            System.out.println("From: " + msg.getFrom()[0]);
	           System.out.println("To: "+msg.getAllRecipients()[0]);
	            System.out.println("Date: "+msg.getReceivedDate());
	            System.out.println("Size: "+msg.getSize());
	            System.out.println(msg.getFlags());
	            System.out.println("Body: \n"+ msg.getContent());
	            System.out.println(msg.getContentType());

				try {
					Multipart multipart = (Multipart) msg.getContent();
					System.out.println("\tMessage attachment count " + multipart.getCount());
					for (int i = 0; i < multipart.getCount(); i++) {
						BodyPart bodyPart = multipart.getBodyPart(i);
						if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())
								|| (StringUtils.isBlank(bodyPart.getFileName()))) {
							System.out.println("\tMessage has no attachments");
							continue; // dealing with attachments only

						}
						System.out.println("\tMessage attachment filename is " + bodyPart.getFileName());
						System.out.println("\tMessage attachment type is " + bodyPart.getContentType());
						InputStream is = bodyPart.getInputStream();
						List<BaseEntity> importBEsFromEmail = readCSV(settings.getSourceBaseEntity(),is,bodyPart.getFileName(),settings.getColumn2attributeMapping());
						importBEs.addAll(importBEsFromEmail);
						
						for (BaseEntity be : importBEs) {
							System.out.println(be);
							for (EntityAttribute ea : be.getBaseEntityAttributes()) {
								System.out.print(ea.getAttributeCode()+":"+ea.getValueString()+", ");
							}
							System.out.println();
						}

					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (MessagingException e) {
					e.printStackTrace();
				}
			}

		} catch (Exception e)
	    {
	    	System.out.println(e.getStackTrace());
	
	
	  }
	    finally {
	    	 try {
	    		 if (inbox != null && inbox.isOpen()) { inbox.close(true); }
	             if (store != null) { store.close(); }
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	      
	    }
	    
	    return importBEs;
}

}