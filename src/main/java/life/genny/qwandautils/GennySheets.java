package life.genny.qwandautils;

import static java.lang.System.out;
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
import org.apache.commons.io.IOUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import life.genny.qwanda.entity.BaseEntity;
import java.io.File;

public class GennySheets {
//public static final String SHEETID = System.getenv("GOOGLE_SHEETID");
//public static final String SHEETID = "1VSXJUn8_BHG1aW0DQrFDnvLjx_jxcNiD33QzqO5D-jc";
  
  /** Range of Columns to read or write */
  private final String RANGE = "!A1:ZZ";
 
  private String appName = "Google Sheets API Java Quickstart";
  
  /** Global variable normally instantiated from a Json file or env variable */
  private String clientSecret;
  
  /** Global variable for the Sheet ID located on the URL of google docs spreedsheet between the d/ and trailing slash ( / ) . */
  private String sheetId;
  
  /** Global instance of the {@link Gson}. */
  private Gson g = new Gson();

  private FileDataStoreFactory DATA_STORE_FACTORY;

  /** Global instance of the JSON factory. */
  private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  /** Global instance of the HTTP transport. */
  private HttpTransport HTTP_TRANSPORT;
  
  /** Directory to store user credentials for this application. */
  private File dataStoreDir;
  
  /**
   * Global instance of the scopes required by this quickstart.
   *
   * If modifying these scopes, delete your previously saved credentials at
   * ~/.credentials/sheets.googleapis.com-java-quickstart
   */
  private final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS);
  
  public GennySheets(String clientSecret, String sheetId, File dataStoreDir ) {
    this.clientSecret = clientSecret;
    this.sheetId = sheetId;
    this.dataStoreDir = dataStoreDir;
    try {
      DATA_STORE_FACTORY = new FileDataStoreFactory(this.dataStoreDir);
      HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }catch (final Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
  
  public GennySheets(String clientSecret, String sheetId, File dataStoreDir, String appName) {
    this(clientSecret, sheetId, dataStoreDir);
    this.appName = appName;
  }
    
  /**
   * @return the clientSecret
   */
  public String getClientSecret() {
    return clientSecret;
  }

  /**
   * @param clientSecret the clientSecret to set
   */
  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }

  /**
   * @return the sheetId
   */
  public String getSheetId() {
    return sheetId;
  }

  /**
   * @param sheetId the sheetId to set
   */
  public void setSheetId(String sheetId) {
    this.sheetId = sheetId;
  }

  public Sheets getSheetsService() throws IOException {
    final Credential credential = authorize();
    return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
        .setApplicationName(appName).build();
  }

  public Credential authorize() throws IOException {
    // Load client secrets.
    out.println(System.getProperty("user.home"));
    // InputStream in = GennySheets.class.getResourceAsStream("/client_secret_2.json");
    final InputStream in = IOUtils.toInputStream(clientSecret, "UTF-8");
    final GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

    // Build flow and trigger user authorization request.
    final GoogleAuthorizationCodeFlow flow =
        new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
    final Credential credential =
        new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    System.out.println("Credentials saved to " + dataStoreDir.getAbsolutePath());
    return credential;
  }
  
  public <T> List<T> transform(final List<List<Object>> values, final Class object) {
    final List<String> keys = new ArrayList<String>();
    final List<T> k = new ArrayList<T>();
    for (final Object key : values.get(0)) {
      keys.add((String) key);
    }
    values.remove(0);
    for (final List row : values) {
      final Map<String, Object> mapper = new HashMap<String, Object>();
      for (int counter = 0; counter < row.size(); counter++) {
        mapper.put(keys.get(counter), row.get(counter));
      }
//      out.println(mapper);
      final T lo = (T) g.fromJson(mapper.toString(), object);
      k.add(lo);
    }
    return k;
  }

  public <T> List<T> getBeans(final Class clazz) throws IOException {
    final Sheets service = getSheetsService();
    final String range = clazz.getSimpleName() + RANGE;
    final ValueRange response = service.spreadsheets().values().get(sheetId, range).execute();
    final List<List<Object>> values = response.getValues();
    return transform(values, clazz);
  }

  public List<List<Object>> getStrings(final String sheetName, final String range)
      throws IOException {
    final Sheets service = getSheetsService();
    final String absoluteRange = sheetName + RANGE;
    final ValueRange response =
        service.spreadsheets().values().get(sheetId, absoluteRange).execute();
    final List<List<Object>> values = response.getValues();
    return values;
  }

  public List<BaseEntity> getBaseEntitys() {
    try {
      return getBeans(BaseEntity.class);
    } catch (final IOException e) {
      return new ArrayList<BaseEntity>();
    }
  }
}
