package life.genny;

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
import org.junit.Test;

import com.opencsv.CSVReader;

import life.genny.models.QFetchMailSettings;
import life.genny.qwanda.Answer;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeText;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwandautils.MailUtils;
import life.genny.qwandautils.QwandaUtils;

public class MailUtilsTest {

	/**
	 * Stores logger object.
	 */
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	private static String[] COLUMN_ATTRIBUTE_MAPPING = { "Release Number:RAW_RELEASE_NUMBER",
			"Release Open Date:RAW_RELEASE_OPEN_DATE", "Customer:RAW_CUSTOMER", "Customer Ref:RAW_CUSTOMER_REF",
			"Line Operator:RAW_LINE_OPERATOR", "ISO:RAW_ISO", "Req:RAW_REQUIRED", "Rcvd:RAW_RCVD",
			"Pickup Location:RAW_PICKUP_LOCATION", "Delivery Location:RAW_DELIVERY_LOCATION",
			"Vessel Cut Off:RAW_VESSEL_CUT_OFF" };

	@Test
	public void fetchMailTest() {
		BaseEntity source = new BaseEntity("SRC_CSV_TEST", "Test CSV Source");

		String username = System.getenv("EMAIL_USERNAME");
		String password = System.getenv("EMAIL_PASSWORD");
		String host = System.getenv("EMAIL_HOST") != null ? System.getenv("EMAIL_HOST") : "imap.googlemail.com"; // default
																													// gmail

		if (!(StringUtils.isBlank(username) || StringUtils.isBlank(password))) {

			QFetchMailSettings settings = new QFetchMailSettings(source, host, username, password, "Genny",
					"Genny/Archive", "Genny/Error", COLUMN_ATTRIBUTE_MAPPING);

			MailUtils.fetchMail(settings,"BEG_");
		}
	}



}
