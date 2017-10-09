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

public class GennySheets {

  public static final String RANGE = "!A1:ZZ";
  // public static final String CLIENT_SECRET = System.getenv("GOOGLE_CLIENT_SECRET");
  public static final String CLIENT_SECRET =
      "{\"installed\":{\"client_id\":\"260075856207-9d7a02ekmujr2bh7i53dro28n132iqhe.apps.googleusercontent.com\",\"project_id\":\"genny-sheets-181905\",\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"token_uri\":\"https://accounts.google.com/o/oauth2/token\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\",\"client_secret\":\"vgXEFRgQvh3_t_e5Hj-eb6IX\",\"redirect_uris\":[\"urn:ietf:wg:oauth:2.0:oob\",\"http://localhost\"]}}";
  public static final String APPLICATION_NAME = "Google Sheets API Java Quickstart";
  // public static final String SHEETID = System.getenv("GOOGLE_SHEETID");
  public static final String SHEETID = "1VSXJUn8_BHG1aW0DQrFDnvLjx_jxcNiD33QzqO5D-jc";
  /** Directory to store user credentials for this application. */
  public static final java.io.File DATA_STORE_DIR = new java.io.File(
      System.getProperty("user.home"), ".credentials/sheets.googleapis.com-java-quickstart");

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
  private static final List<String> SCOPES = Arrays.asList(SheetsScopes.SPREADSHEETS);

  public static Sheets getSheetsService() throws IOException {
    final Credential credential = authorize();
    return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
        .setApplicationName(APPLICATION_NAME).build();
  }

  public static Credential authorize() throws IOException {
    // Load client secrets.
    out.println(System.getProperty("user.home"));
    // InputStream in = GennySheets.class.getResourceAsStream("/client_secret_2.json");
    final InputStream in = IOUtils.toInputStream(CLIENT_SECRET, "UTF-8");
    final GoogleClientSecrets clientSecrets =
        GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

    // Build flow and trigger user authorization request.
    final GoogleAuthorizationCodeFlow flow =
        new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
            .setDataStoreFactory(DATA_STORE_FACTORY).setAccessType("offline").build();
    final Credential credential =
        new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
    System.out.println("Credentials saved to " + DATA_STORE_DIR.getAbsolutePath());
    return credential;
  }

  static {
    try {
      HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
      DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
    } catch (final Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }
  }
  static Gson g = new Gson();

  public static <T> List<T> transform(final List<List<Object>> values, final Class object) {
    final List<String> keys = new ArrayList<String>();
    final List<T> k = new ArrayList<T>();
    for (final Object key : values.get(0)) {
      keys.add((String) key);
    }
    // values.stream().peek(act-> System.out.println(act+"ok1")).
    values.remove(0);
    for (final List row : values) {
      final Map<String, Object> mapper = new HashMap<String, Object>();
      for (int counter = 0; counter < row.size(); counter++) {
        mapper.put(keys.get(counter), row.get(counter));
      }
      final T lo = (T) g.fromJson(mapper.toString(), object);
      k.add(lo);
    }
    return k;
  }

  public static <T> List<T> getBeans(final Class clazz) throws IOException {
    final Sheets service = getSheetsService();
    final String range = clazz.getSimpleName() + RANGE;
    final ValueRange response = service.spreadsheets().values().get(SHEETID, range).execute();
    final List<List<Object>> values = response.getValues();
    return transform(values, clazz);
  }

  public static List<List<Object>> getStrings(final String sheetName, final String range)
      throws IOException {
    final Sheets service = getSheetsService();
    final String absoluteRange = sheetName + RANGE;
    final ValueRange response =
        service.spreadsheets().values().get(SHEETID, absoluteRange).execute();
    final List<List<Object>> values = response.getValues();
    return values;
  }


  public static List<BaseEntity> getBaseEntitys() {

    try {
      return getBeans(BaseEntity.class);
    } catch (final IOException e) {
      return new ArrayList<BaseEntity>();
    }
  }

  public static void main() throws IOException {

    final List<BaseEntity> bes = getBeans(BaseEntity.class);

    bes.forEach(out::println);
    // InputStream in = GennySheets.class.getResourceAsStream("/opt/realm/keycloak.json");
    // String theString = new BufferedReader(new InputStreamReader(in))
    // .lines().collect(Collectors.joining("\n"));
    // System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$"+System.getProperty("user.dir"));

    // out.print(DATA_STORE_DIR);
    // File dir = new File("/opt/realm/");
    //
    // // attempt to create the directory here
    // boolean successful = dir.mkdir();
    // if (successful)
    // {
    // // creating the directory succeeded
    // System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$directory was created successfully");
    // }
    // else
    // {
    // // creating the directory failed
    // System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$failed trying to create the
    // directory");
    // }
    // File[] fi= File;
    // out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&& "+fi[0].getAbsolutePath()+
    // "&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
  }
}
