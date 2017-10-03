package life.genny;

import static java.lang.System.out;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.jar.Attributes;

import org.junit.Test;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.gson.Gson;

import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.entity.BaseEntity;

public class GoogleUtilsTest {

	public static Sheets getSheetsService() throws IOException {
		Credential credential = authorize();
		return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME)
				.build();
	}

	public static Credential authorize() throws IOException {
		// Load client secrets.

		out.println(System.getProperty("user.home"));
		InputStream in = GoogleUtilsTest.class.getResourceAsStream("/client_secret.json");
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

		// Build flow and trigger user authorization request.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY,
				clientSecrets, SCOPES).setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
		Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");

		System.out.println("Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
		return credential;
	}

	private static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";

	/** Directory to store user credentials for this application. */
	private static final java.io.File DATA_STORE_DIR = new java.io.File(System.getProperty("user.home"),
			".credentials/sheets.googleapis.com-java-quickstart");

	/** Global instance of the {@link FileDataStoreFactory}. */
	private static FileDataStoreFactory DATA_STORE_FACTORY;

	/** Global instance of the JSON factory. */
	private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

	/** Global instance of the HTTP transport. */
	private static HttpTransport HTTP_TRANSPORT;

	/**
	 * Global instance of the scopes required by this quickstart.
	 *
	 * If modifying these scopes, delete your previously saved credentials at
	 * ~/.credentials/sheets.googleapis.com-java-quickstart
	 */
	private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS_READONLY);

	static {
		try {
			HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
			DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
		} catch (Throwable t) {
			t.printStackTrace();
			System.exit(1);
		}
	}
	static Gson g = new Gson();
	public static <T> List<T> transform(List<List<Object>> values, Class object) {
		List<String> keys = new ArrayList<String>();
		List<T> k = new ArrayList<T>();
		for (Object key : values.get(0)) {
			keys.add((String) key);
		}
//		values.stream().peek(act-> System.out.println(act+"ok1")).
		values.remove(0);
		for (List row : values) {
			Map<String, Object> mapper = new HashMap<String, Object>();
			for (int counter = 0; counter < row.size(); counter++) {
				mapper.put(keys.get(counter), (String)row.get(counter));
			}
			T lo = (T) g.fromJson(mapper.toString(), object);
			k.add(lo);
		}
		return k;
	}

	public static <T> List<T> getBeans(Class object) throws IOException {
		Sheets service = getSheetsService();
		String spreadsheetId = "1VSXJUn8_BHG1aW0DQrFDnvLjx_jxcNiD33QzqO5D-jc";
		String range = object.getSimpleName()+"!A1:ZZ";
		ValueRange response = service.spreadsheets().values().get(spreadsheetId, range).execute();
		List<List<Object>> values = response.getValues();
		return transform(values, object);
	}
	@Test
	public void loadItems() throws IOException {
			
//			List<BaseEntity> baseEntitys = getBeans(BaseEntity.class);
			
			List<Attribute> attrs = getBeans(Attribute.class);
			
			attrs.stream().forEach(out::println);
//			baseEntitys.forEach(out::println);
	}

}
