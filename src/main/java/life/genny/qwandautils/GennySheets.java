package life.genny.qwandautils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
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
import life.genny.qwanda.Ask;
import life.genny.qwanda.Question;
import life.genny.qwanda.QuestionQuestion;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeLink;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.datatype.DataType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.validation.Validation;

public class GennySheets {
  /**
   * Stores logger object.
   */
  protected static final Logger log = org.apache.logging.log4j.LogManager
      .getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

  /** Range of Columns to read or write */
  private final String RANGE = "!A1:Z";

  private String appName = "Google Sheets API Java Quickstart";

  /** Global variable normally instantiated from a Json file or env variable */
  private String clientSecret;

  /**
   * Global variable for the Sheet ID located on the URL of google docs spreedsheet between the d/
   * and trailing slash ( / ) .
   */
  private String sheetId;


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
    // InputStream in =
    // GennySheets.class.getResourceAsStream("/client_secret_2.json");
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


  public <T> List<T> fetchTable(final String sheetName) throws IOException {
    final String absoluteRange = sheetName + RANGE;
    final ValueRange response;
    response = service.spreadsheets().values().get(sheetId, absoluteRange).execute();
    final List<List<Object>> values = response.getValues();
    return transformNotKnown(values);
  }

  public Map<String, Map> getTable(String tableName) {
    List<Map<String, String>> rowFields = new ArrayList<Map<String, String>>();
    try {
      rowFields = fetchTable(tableName);
    } catch (final IOException e1) {
      // TODO Auto-generated catch block
      return null;
    }

    rowFields.stream().map(field -> {
      final Map<String, Map> map = new HashMap<String, Map>();

      Set<String> collect = field.keySet().stream().map(columnName -> columnName.toUpperCase())
          .collect(Collectors.toSet());

      collect.stream().forEach(System.out::println);
      // EnumSet.allOf(ColumnName.class).contains(keySet);
      return null;
    });

    return null;
  }
  
  private final String CODE = "code";
  private final String BASEENTITYCODE = "baseEntityCode";
  private final String ATTRIBUTECODE = "attributeCode";
  private final String PARENTCODE = "parentCode";
  private final String TARGETCODE = "targetCode";
  private final String LINKCODE = "linkCode";
  private final String QUESTION_CODE = "question_code";
  private final String SOURCECODE = "sourceCode";


  Function<Map<String,String>,String> mapBaseEntityKey = (map) -> map.get(CODE);
  Function<Map<String,String>,String> mapAttributeKey = (map) -> map.get(CODE);
  Function<Map<String,String>,String> mapAttributeLinkKey = (map) -> map.get(CODE);
  Function<Map<String,String>,String> mapDataTypeKey = (map) -> map.get(CODE);
  Function<Map<String,String>,String> mapValidationKey = (map) -> map.get(CODE);
  Function<Map<String,String>,String> mapEntityAttributeKey = (map) -> map.get(BASEENTITYCODE) + map.get(ATTRIBUTECODE);
  Function<Map<String,String>,String> mapEntityEntityKey = (map) -> map.get(TARGETCODE) + map.get(PARENTCODE) + map.get(LINKCODE) ;
  Function<Map<String,String>,String> mapQuestionKey = (map) -> map.get(TARGETCODE) + map.get(PARENTCODE) + map.get(LINKCODE) ;
  Function<Map<String,String>,String> mapQuestionQuestionKey = (map) -> map.get(TARGETCODE) + map.get(PARENTCODE);
  Function<Map<String,String>,String> mapAskKey = (map) -> map.get(QUESTION_CODE) + map.get(SOURCECODE) + map.get(TARGETCODE);
  Function<Map<String,String>,String> mapNotificationsKey = (map) -> map.get(QUESTION_CODE) ;
  
  public Set<String> getColumnNames( List<Map<String, String>> rowList){
    return rowList.stream().flatMap(columns -> 
          columns.keySet()
          .stream()
          .map(columnName -> columnName)
          .collect(Collectors.toSet())
          .stream()
    ).collect(Collectors.toSet());
  }
  
  public List<Map>  convertTableToListMap(String tableName) {
    List<Map<String, String>> rowList = new ArrayList<Map<String, String>>();

    try {
      System.out.println(tableName);
      rowList = fetchTable(tableName);
    } catch (final IOException e1) {
      // TODO Auto-generated catch block
      return null;
    }
    
    Set<String> columnNames = getColumnNames(rowList);
    
    Function<Map<String,String>,List<Map>>  putFieldsOnMap  = (row) -> {
      final List<Map> list = new ArrayList<Map>();
      Map<String, Object> fields = new HashMap<String, Object>();
      columnNames.stream().forEach(key -> fields.put(key, row.get(key).trim()));
      System.out.println(fields);
      list.add(fields);
      return list;
    };
    
    BinaryOperator<List<Map>> aggregateAll = (first, second) -> {
      first.addAll(second);
      return first;
    };
    
    List<Map> map = rowList.stream()
        .map(putFieldsOnMap)
        .reduce(aggregateAll)
        .get();

    return map;
  }

  public Map<String, Map> convertTableToMap(String tableName, Function<Map<String,String>,String> toMapKey) {
    List<Map<String, String>> rowList = new ArrayList<Map<String, String>>();
    
    try {
      rowList = fetchTable(tableName);
    } catch (final IOException e1) {
      // TODO Auto-generated catch block
      return null;
    }
    
    Set<String> columnNames = getColumnNames(rowList);
    
    Function<Map<String,String>,Map<String,Map>>  putFieldsOnMap  = (row) -> {
      final Map<String, Map> map = new HashMap<String, Map>();
      Map<String, String> fields = new HashMap<String, String>();
      columnNames.stream().forEach(key -> fields.put(key, row.get(key).trim()));
      map.put(toMapKey.apply(row), fields);
      return map;
    };
    
    BinaryOperator<Map<String, Map>> aggregateAll = (first, second) -> {
      first.putAll(second);
      return first;
    };
    
    Map<String, Map> map = rowList.stream()
        .map(putFieldsOnMap)
        .reduce(aggregateAll)
        .get();
    
    return map;
  }

  public Map<String, Map> newGetBase() {
    return convertTableToMap(BaseEntity.class.getSimpleName(),mapBaseEntityKey);
  }

  public Map<String, Map> newGetVal() {
    return convertTableToMap(Validation.class.getSimpleName(),mapValidationKey);
  }

  public Map<String, Map> newGetDType() {
    return convertTableToMap(DataType.class.getSimpleName(),mapDataTypeKey);
  }

  public Map<String, Map> newGetAttr() {
    return convertTableToMap(Attribute.class.getSimpleName(),mapAttributeKey);
  }

  public Map<String, Map> newGetAttrLink() {
    return convertTableToMap(AttributeLink.class.getSimpleName(),mapAttributeLinkKey);
  }

  public Map<String, Map> newGetEntAttr() {
    
    return convertTableToMap(EntityAttribute.class.getSimpleName(),mapEntityAttributeKey);
  }

  public Map<String, Map> newGetEntEnt() {
    return convertTableToMap(EntityEntity.class.getSimpleName(),mapEntityEntityKey);
  }

  public Map<String, Map> newGetQtn() {
    return convertTableToMap(Question.class.getSimpleName(),mapQuestionKey);
  }

  public Map<String, Map> newGetQueQue() {
    return convertTableToMap(QuestionQuestion.class.getSimpleName(),mapQuestionQuestionKey);
  }

  public Map<String, Map> newGetAsk() {
    return convertTableToMap(Ask.class.getSimpleName(),mapAskKey);
  }
  
  public Map<String, Map> getMessageTemplates() {
    return convertTableToMap("Notifications",mapNotificationsKey);
  }

  public List<Map> projectsImport() {
    return convertTableToListMap("Modules");
  }

  public List<Map> hostingImport() {
    return convertTableToListMap("Projects");
  }


/* User these constants to represent column names from spreadsheet*/
//private final String NAME = "name";
//private final String ICON = "icon";
//private final String DEPLOY_CODE = "deploy_code";
//private final String REGEX = "regex";
//private final String GROUP_CODES = "group_codes";
//private final String RECURSIVE = "recursive";
//private final String MULTI_ALLOWED = "multi_allowed";
//private final String VALIDATIONS = "validations";
//private final String INPUTMASK = "inputmask";
//private final String DISABLED = "disabled";
//private final String PRIVACY = "privacy";
//private final String DESCRIPTION = "description";
//private final String HELP = "help";
//private final String DEFAULTVALUE = "defaultValue";
//private final String WEIGHT = "weight";
//private final String VALUESTRING = "valueString";
//private final String HTML = "html";
//private final String MANDATORY = "mandatory";
//private final String REFUSED = "refused";
//private final String EXPIRED = "expired";
//private final String EXPECTEDID = "expectedId";
//private final String MODULE = "module";
//private final String SHEETID = "sheetID";
//private final String CLIENTSECRET = "clientSecret";
//private final String SUBJECT = "subject";
//private final String SMS = "sms";
//private final String EMAIL = "email";
//private final String TOAST = "toast";
//private final String PLACEHOLDER = "placeholder";
//private final String DATATYPE = "datatype";



}
