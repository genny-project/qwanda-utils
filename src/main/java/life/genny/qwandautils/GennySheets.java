package life.genny.qwandautils;


import static java.lang.System.out;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.gson.Gson;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import javax.persistence.NoResultException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import life.genny.qwanda.Ask;
import life.genny.qwanda.Question;
import life.genny.qwanda.QuestionQuestion;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeLink;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.datatype.DataType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.validation.Validation;
import life.genny.qwanda.validation.ValidationList;

public class GennySheets {
  /**
   * Stores logger object.
   */
  protected static final Logger log = org.apache.logging.log4j.LogManager
      .getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

  /** Range of Columns to read or write */
  private final String RANGE = "!A1:ZZ";

  private String appName = "Google Sheets API Java Quickstart";

  /** Global variable normally instantiated from a Json file or env variable */
  private String clientSecret;

  /**
   * Global variable for the Sheet ID located on the URL of google docs spreedsheet between the d/
   * and trailing slash ( / ) .
   */
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

  private Sheets service;

  public GennySheets(final String clientSecret, final String sheetId, final File dataStoreDir) {
    this.clientSecret = clientSecret;
    this.sheetId = sheetId;
    this.dataStoreDir = dataStoreDir;
    try {
      DATA_STORE_FACTORY = new FileDataStoreFactory(this.dataStoreDir);
      HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
      service = getSheetsService();
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final Throwable t) {
      t.printStackTrace();
      System.exit(1);
    }

  }

  public GennySheets(final String clientSecret, final String sheetId, final File dataStoreDir,
      final String appName) {
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
  public void setClientSecret(final String clientSecret) {
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
  public void setSheetId(final String sheetId) {
    this.sheetId = sheetId;
  }


  public Sheets getSheetsService() throws Exception {
    final Credential credential = authorize();
    return new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(appName)
        .build();
  }

  public Credential authorize() throws Exception {
    // Load client secrets.
    // out.println(System.getProperty("user.home"));
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
      // out.println(mapper);
      final T lo = (T) g.fromJson(mapper.toString(), object);
      k.add(lo);
    }
    return k;
  }

  public <T> List<T> transformNotKnown(final List<List<Object>> values) {
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
      k.add((T) mapper);
    }
    return k;
  }

  public <T> List<T> getBeans(final Class clazz) throws IOException {
    final String range = clazz.getSimpleName() + RANGE;
    final ValueRange response = service.spreadsheets().values().get(sheetId, range).execute();
    final List<List<Object>> values = response.getValues();
    return transform(values, clazz);
  }

  public List<List<Object>> getStrings(final String sheetName, final String range)
      throws IOException {
    final String absoluteRange = sheetName + RANGE;
    final ValueRange response =
        service.spreadsheets().values().get(sheetId, absoluteRange).execute();
    final List<List<Object>> values = response.getValues();
    return values;
  }

  public <T> List<T> row2DoubleTuples(final String sheetName) throws IOException {
    final String absoluteRange = sheetName + RANGE;
    final ValueRange response;
//    try {
      response = service.spreadsheets().values().get(sheetId, absoluteRange).execute();
//    } catch (GoogleJsonResponseException e) {
//      System.out.println("dfsdfsdfsdfsd");
//      return null;
//    }
    final List<List<Object>> values = response.getValues();
    return transformNotKnown(values);
  }

  public Map<String, Map> newGetBase() {
    List<Map> obj = new ArrayList<Map>();
    try {
      obj = row2DoubleTuples(BaseEntity.class.getSimpleName());
    } catch (final IOException e1) {
      // TODO Auto-generated catch block
      return null;
    }
    return obj.stream().map(object -> {
      final Map<String, Map> map = new HashMap<String, Map>();
      final String code = (String) object.get("code");
      final String name = (String) object.get("name");
      final String icon = (String) object.get("icon");
      Map<String, String> fields = new HashMap<String, String>();
      fields.put("code", code);
      fields.put("name", name);
      fields.put("icon", icon);
      map.put(code, fields);
      return map;
    }).reduce((ac, acc) -> {
      ac.putAll(acc);
      return ac;
    }).get();
  }

  public Map<String, Map> newGetVal() {
    List<Map> obj = new ArrayList<Map>();
    try {
      obj = row2DoubleTuples(Validation.class.getSimpleName());
    } catch (final IOException e1) {
      // TODO Auto-generated catch block
      return null;
    }
    return obj.stream().map(object -> {
      final Map<String, Map> map = new HashMap<String, Map>();
      final String code = (String) object.get("code");
      final String name = (String) object.get("name");
      final String regex = (String) object.get("regex");
      Map<String, String> fields = new HashMap<String, String>();
      fields.put("code", code);
      fields.put("name", name);
      fields.put("regex", regex);
      map.put(code, fields);
      return map;
    }).reduce((ac, acc) -> {
      ac.putAll(acc);
      return ac;
    }).get();
  }

  public Map<String, Map> newGetDType() {
    List<Map> obj = new ArrayList<Map>();
    try {
      obj = row2DoubleTuples(DataType.class.getSimpleName());
    } catch (final IOException e1) {
      // TODO Auto-generated catch block
      return null;
    }
    return obj.stream().map(object -> {
      final Map<String, Map> map = new HashMap<String, Map>();
      final String code = (String) object.get("code");
      final String name = (String) object.get("name");
      final String validations = (String) object.get("validations");
      Map<String, String> fields = new HashMap<String, String>();
      fields.put("code", code);
      fields.put("name", name);
      fields.put("validations", validations);
      map.put(code, fields);
      return map;
    }).reduce((ac, acc) -> {
      ac.putAll(acc);
      return ac;
    }).get();
  }

  public Map<String, Map> newGetAttr() {
    List<Map> obj = new ArrayList<Map>();
    try {
      obj = row2DoubleTuples(Attribute.class.getSimpleName());
    } catch (final IOException e1) {
      // TODO Auto-generated catch block
      return null;
    }
    return obj.stream().map(object -> {
      final Map<String, Map> map = new HashMap<String, Map>();
      final String code = (String) object.get("code");
      final String name = (String) object.get("name");
      final String dataType = (String) object.get("datatype");
      Map<String, String> fields = new HashMap<String, String>();
      fields.put("code", code);
      fields.put("name", name);
      fields.put("dataType", dataType);
      map.put(code, fields);
      return map;
    }).reduce((ac, acc) -> {
      ac.putAll(acc);
      return ac;
    }).get();
  }

  public Map<String, Map> newGetAttrLink() {
    List<Map> obj = new ArrayList<Map>();
    try {
      obj = row2DoubleTuples(AttributeLink.class.getSimpleName());
    } catch (final IOException e1) {
      // TODO Auto-generated catch block
      return null;
    }
    return obj.stream().map(object -> {
      final Map<String, Map> map = new HashMap<String, Map>();
      final String code = (String) object.get("code");
      final String name = (String) object.get("name");
      Map<String, String> fields = new HashMap<String, String>();
      fields.put("code", code);
      fields.put("name", name);
      map.put(code, fields);
      return map;
    }).reduce((ac, acc) -> {
      ac.putAll(acc);
      return ac;
    }).get();
  }

  public Map<String, Map> newGetEntAttr() {
    List<Map> obj = new ArrayList<Map>();
    try {
      obj = row2DoubleTuples(EntityAttribute.class.getSimpleName());
    } catch (final IOException e1) {
      // TODO Auto-generated catch block
      return null;
    }
    return obj.stream().map(object -> {
      final Map<String, Map> map = new HashMap<String, Map>();
      final String baseEntityCode = (String) object.get("baseEntityCode");
      final String attributeCode = (String) object.get("attributeCode");
      final String weight = (String) object.get("weight");
      final String valueString = (String) object.get("valueString");
      Map<String, String> fields = new HashMap<String, String>();
      fields.put("baseEntityCode", baseEntityCode);
      fields.put("attributeCode", attributeCode);
      fields.put("weight", weight);
      fields.put("valueString", valueString);
      map.put(baseEntityCode + attributeCode, fields);
      return map;
    }).reduce((ac, acc) -> {
      ac.putAll(acc);
      return ac;
    }).get();
  }

  public Map<String, Map> newGetEntEnt() {
    List<Map> obj = new ArrayList<Map>();
    try {
      obj = row2DoubleTuples(EntityEntity.class.getSimpleName());
    } catch (final IOException e1) {
      // TODO Auto-generated catch block
      return null;
    }
    return obj.stream().map(object -> {
      final Map<String, Map> map = new HashMap<String, Map>();
      final String parentCode = (String) object.get("parentCode");
      final String targetCode = (String) object.get("targetCode");
      final String linkCode = (String) object.get("linkCode");
      final String weight = (String) object.get("weight");
      final String valueString = (String) object.get("valueString");
      Map<String, String> fields = new HashMap<String, String>();
      fields.put("parentCode", parentCode);
      fields.put("targetCode", targetCode);
      fields.put("linkCode", linkCode);
      fields.put("weight", weight);
      fields.put("valueString", valueString);
      map.put(targetCode + parentCode + linkCode, fields);
      return map;
    }).reduce((ac, acc) -> {
      ac.putAll(acc);
      return ac;
    }).get();
  }

  public Map<String, Map> newGetQtn() {
    List<Map> obj = new ArrayList<Map>();
    try {
      obj = row2DoubleTuples(Question.class.getSimpleName());
    } catch (final IOException e1) {
      // TODO Auto-generated catch block
      return null;
    }
    return obj.stream().map(object -> {
      final Map<String, Map> map = new HashMap<String, Map>();
      final String code = (String) object.get("code");
      final String name = (String) object.get("name");
      final String attribute_code = (String) object.get("attribute_code");
      Map<String, String> fields = new HashMap<String, String>();
      fields.put("code", code);
      fields.put("name", name);
      fields.put("attribute_code", attribute_code);
      map.put(code, fields);
      return map;
    }).reduce((ac, acc) -> {
      ac.putAll(acc);
      return ac;
    }).get();
  }

  public Map<String, Map> newGetQueQue() {
    List<Map> obj = new ArrayList<Map>();
    try {
      obj = row2DoubleTuples(QuestionQuestion.class.getSimpleName());
    } catch (final IOException e1) {
      // TODO Auto-generated catch block
      return null;
    }
    return obj.stream().map(object -> {
      final Map<String, Map> map = new HashMap<String, Map>();
      final String parentCode = (String) object.get("parentCode");
      final String targetCode = (String) object.get("targetCode");
      final String weight = (String) object.get("weight");
      final String mandatory = (String) object.get("mandatory");
      Map<String, String> fields = new HashMap<String, String>();
      fields.put("parentCode", parentCode);
      fields.put("targetCode", targetCode);
      fields.put("weight", weight);
      fields.put("mandatory", mandatory);
      map.put(targetCode + parentCode, fields);
      return map;
    }).reduce((ac, acc) -> {
      ac.putAll(acc);
      return ac;
    }).get();
  }


  public Map<String, Map> newGetAsk() {
    List<Map> obj = new ArrayList<Map>();
    try {
      obj = row2DoubleTuples(Ask.class.getSimpleName());
    } catch (final IOException e1) {
      // TODO Auto-generated catch block
      return null;
    }
    return obj.stream().map(object -> {
      final Map<String, Map> map = new HashMap<String, Map>();
      final String question_code = (String) object.get("question_code");
      final String name = (String) object.get("name");
      final String sourceCode = (String) object.get("sourceCode");
      final String targetCode = (String) object.get("targetCode");
      final String attributeCode = (String) object.get("attributeCode");
      final String refused = (String) object.get("refused");
      final String expired = (String) object.get("expired");
      final String expectedId = (String) object.get("expectedId");
      Map<String, String> fields = new HashMap<String, String>();
      fields.put("question_code", question_code);
      fields.put("name", name);
      fields.put("sourceCode", sourceCode);
      fields.put("targetCode", targetCode);
      fields.put("sourceCode", sourceCode);
      fields.put("attributeCode", attributeCode);
      fields.put("refused", refused);
      fields.put("expired", expired);
      fields.put("expectedId", expectedId);
      map.put(question_code + sourceCode + targetCode, fields);
      return map;
    }).reduce((ac, acc) -> {
      ac.putAll(acc);
      return ac;
    }).get();
  }

  public List<Map> projectsImport() {
    List<Map> obj = new ArrayList<Map>();
    try {
      obj = row2DoubleTuples("Modules");
    } catch (IOException e) {
      e.printStackTrace();
    }
    return obj.stream().map(data -> {
      final List<Map> map = new ArrayList<Map>();
      String code = (String) data.get("code");
      String name = (String) data.get("name");
      String module = (String) data.get("module");
      String sheetID = (String) data.get("sheetID");
      Object clientSecret = data.get("clientSecret");
      Map<String, Object> fields = new HashMap<String, Object>();
      fields.put("sheetID", sheetID);
      fields.put("name", name);
      fields.put("module", module);
      fields.put("clientSecret", clientSecret);
      map.add(fields);
      return map;
    }).reduce((ac, acc) -> {
      ac.addAll(acc);
      return ac;
    }).get();
  }
  
  public List<Map> hostingImport() {
    List<Map> obj = new ArrayList<Map>();
    try {
      obj = row2DoubleTuples("Projects");
    } catch (IOException e) {
      e.printStackTrace();
    }
    return obj.stream().map(data -> {
      final List<Map> map = new ArrayList<Map>();
      String code = (String) data.get("code");
      String name = (String) data.get("name");
      String module = (String) data.get("module");
      String sheetID = (String) data.get("sheetID");
      Object clientSecret = data.get("clientSecret");
      Map<String, Object> fields = new HashMap<String, Object>();
      fields.put("sheetID", sheetID);
      fields.put("name", name);
      fields.put("module", module);
      fields.put("clientSecret", clientSecret);
      map.add(fields);
      return map;
    }).reduce((ac, acc) -> {
      ac.addAll(acc);
      return ac;
    }).get();
  }
}
