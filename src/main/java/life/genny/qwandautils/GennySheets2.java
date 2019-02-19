package life.genny.qwandautils;

import static java.lang.System.out;

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

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.io.IOUtils;
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
import com.google.gson.Gson;

import life.genny.qwanda.Ask;
import life.genny.qwanda.Question;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeLink;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.datatype.DataType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.validation.Validation;
import life.genny.qwanda.validation.ValidationList;

public class GennySheets2 {
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	
	 // public static final String SHEETID = System.getenv("GOOGLE_SHEETID");
  // public static final String SHEETID = "1VSXJUn8_BHG1aW0DQrFDnvLjx_jxcNiD33QzqO5D-jc";

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

  public GennySheets2(final String clientSecret, final String sheetId, final File dataStoreDir) {
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

  public GennySheets2(final String clientSecret, final String sheetId, final File dataStoreDir,
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
    log.info("Credentials saved to " + dataStoreDir.getAbsolutePath());
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
    final ValueRange response =
        service.spreadsheets().values().get(sheetId, absoluteRange).execute();
    final List<List<Object>> values = response.getValues();
    return transformNotKnown(values);
  }

  public List<BaseEntity> getBaseEntitys() {
    try {
      return getBeans(BaseEntity.class);
    } catch (final IOException e) {
      return new ArrayList<BaseEntity>();
    }
  }

  public Map<String, Validation> validationData() {
    List<Validation> validations = new ArrayList<Validation>();
    try {
      validations = getBeans(Validation.class);
    } catch (final IOException e2) {
      e2.printStackTrace();
    }
    return validations.stream().map(valObject -> {
      final Map<String, Validation> map = new HashMap<String, Validation>();
      map.put(valObject.getCode(), valObject);
      return map;
    }).reduce((ac, acc) -> {
      ac.putAll(acc);
      return ac;
    }).get();
  }

  public Map<String, DataType> dataTypesData(final Map<String, Validation> validationData) {
    List<Map> obj = new ArrayList<Map>();
    try {
      obj = row2DoubleTuples(DataType.class.getSimpleName());
    } catch (final IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    return obj.stream().map(object -> {
      final Map<String, DataType> dataTypeMap = new HashMap<String, DataType>();
      if (object.get("code") != null) {
        final String code = (String) object.get("code");
        final String name = (String) object.get("name");
        final String validations = (String) object.get("validations");
        final ValidationList validationList = new ValidationList();
        validationList.setValidationList(new ArrayList<Validation>());
        if (validations != null) {
          final String[] validationListStr = validations.split(",");
          for (final String validationCode : validationListStr) {
            validationList.getValidationList().add(validationData.get(validationCode));
          }
        }
        if (!dataTypeMap.containsKey(code)) {
          final DataType dataType = new DataType(name, validationList);
          dataTypeMap.put(code, dataType);
        }
      }
      return dataTypeMap;
    }).reduce((ac, acc) -> {
      ac.putAll(acc);
      return ac;
    }).get();
  }

  public Map<String, Attribute> attributesData(final Map<String, DataType> dataTypeMap) {
    List<Map> attrs = null;
    try {
      attrs = row2DoubleTuples(Attribute.class.getSimpleName());
    } catch (final IOException e2) {
      // TODO Auto-generated catch block
      e2.printStackTrace();
    }
    return attrs.stream().map(data -> {
      final Map<String, Attribute> attributeMap = new HashMap<String, Attribute>();
      Attribute attribute = null;
      final String code = (String) data.get("code");
      final String name = (String) data.get("name");
      final String datatype = (String) data.get("datatype");
      if (data.get("code") != null) {
        attribute = new Attribute(code, name, dataTypeMap.get(datatype));
      }
      attributeMap.put(code, attribute);
      return attributeMap;
    }).reduce((ac, acc) -> {
      ac.putAll(acc);
      return ac;
    }).get();
  }

  public Map<String, BaseEntity> baseEntityData() {
    List<BaseEntity> bes = null;
    try {
      bes = getBeans(BaseEntity.class);
      bes.stream().forEach(object -> {
      });
    } catch (final IOException e2) {
      // TODO Auto-generated catch block
      e2.printStackTrace();
    }
    return bes.stream().map(valObject -> {
      final Map<String, BaseEntity> map = new HashMap<String, BaseEntity>();
      map.put(valObject.getCode(), valObject);
      return map;
    }).reduce((ac, acc) -> {
      ac.putAll(acc);
      return ac;
    }).get();
  }

  public <T> T getObjectByValue(final T object) {

    final T newObject = object;
    return newObject;
  }

  public Map<String, BaseEntity> attr2BaseEntitys(final Map<String, Attribute> findAttributeByCode,
      final Map<String, BaseEntity> findBaseEntityByCode) {
    List<Map> obj2 = null;
    try {
      obj2 = row2DoubleTuples(EntityAttribute.class.getSimpleName());
    } catch (final IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    return obj2.stream().map(object -> {
      final Map<String, BaseEntity> map = new HashMap<String, BaseEntity>();
      final String beCode = (String) object.get("baseEntityCode");
      final String attributeCode = (String) object.get("attributeCode");
      final String weightStr = (String) object.get("weight");
      final String valueString = (String) object.get("valueString");
      log.info("BECode:" + beCode + ":attCode" + attributeCode + ":weight:" + weightStr
          + ": valueString:" + valueString);
      final Attribute attribute = new Attribute("ds", "dsd", null);
      final BaseEntity be = new BaseEntity("ds", "dsd");
      log.info("==============11==============" + findBaseEntityByCode.get(beCode)
          + "============================");
      try {
        BeanUtils.copyProperties(attribute, findAttributeByCode.get(attributeCode));
        BeanUtils.copyProperties(be, findBaseEntityByCode.get(beCode));
      } catch (IllegalAccessException | InvocationTargetException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      log.info("============================" + be + "============================");
      // service.update(be);
      // attribute = findAttributeByCode.get(attributeCode);
      // be = findBaseEntityByCode.get(beCode);
      final Double weight = Double.valueOf(weightStr);
      try {
        be.addAttribute(attribute, weight, valueString);
      } catch (final BadDataException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      map.put(beCode, be);
      return map;
    }).reduce((ac, acc) -> {
      ac.putAll(acc);
      return ac;
    }).get();
  }

  public Map<String, AttributeLink> attrLink() {
    List<Map> obj2 = null;
    try {
      obj2 = row2DoubleTuples(AttributeLink.class.getSimpleName());
    } catch (final IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    return obj2.stream().map(object -> {
      final Map<String, AttributeLink> map = new HashMap<String, AttributeLink>();
      final String code = (String) object.get("code");
      final String name = (String) object.get("name");
      final AttributeLink linkAttribute = new AttributeLink(code, name);
      map.put(code, linkAttribute);
      return map;
    }).reduce((ac, acc) -> {
      ac.putAll(acc);
      return ac;
    }).get();
  }

  public Map<String, BaseEntity> be2BeTarget(
      final Map<String, AttributeLink> findAttributeLinkByCode,
      final Map<String, BaseEntity> findBaseEntityByCode) {
    List<Map> obj3 = null;
    try {
      obj3 = row2DoubleTuples(EntityEntity.class.getSimpleName());
    } catch (final IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    return obj3.stream().map(object -> {
      final Map<String, BaseEntity> map = new HashMap<String, BaseEntity>();
      final String parentCode = (String) object.get("parentCode");
      final String targetCode = (String) object.get("targetCode");
      final String linkCode = (String) object.get("linkCode");
      final String weightStr = (String) object.get("weight");
      final BaseEntity sbe = new BaseEntity("null", "null");
      final BaseEntity tbe = new BaseEntity("null", "null");
      try {
        final BaseEntity temp1 = findBaseEntityByCode.get(parentCode);
        final BaseEntity temp2 = findBaseEntityByCode.get(targetCode);
        BeanUtils.copyProperties(sbe, temp1);
        BeanUtils.copyProperties(tbe, temp2);
        final AttributeLink linkAttribute2 =
            getObjectByValue(findAttributeLinkByCode.get(linkCode));
        final Double weight = Double.valueOf(weightStr);
        sbe.addTarget(tbe, linkAttribute2, weight);
       } catch (final IllegalAccessException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (final InvocationTargetException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (final BadDataException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      map.put(tbe.getCode(), tbe);
      return map;
    }).reduce((ac, acc) -> {
      ac.putAll(acc);
      return ac;
    }).get();
  }

  public Map<String, Question> questionsData(final Map<String, Attribute> findAttributeByCode) {
    List<Map> obj4 = null;
    try {
      obj4 = row2DoubleTuples(Question.class.getSimpleName());
    } catch (final IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    return obj4.stream().map(object -> {
      final Map<String, Question> map = new HashMap<String, Question>();
      final String code = (String) object.get("code");
      final String name = (String) object.get("name");
      final String attrCode = (String) object.get("attribute_code");
      Attribute attr;
      attr = findAttributeByCode.get(attrCode);
      final Question q = new Question(code, name, attr);
      map.put(code, q);
      return map;
    }).reduce((ac, acc) -> {
      ac.putAll(acc);
      return ac;
    }).get();
  }

  public Map<String, Ask> asksData(final Map<String, Question> findQuestionByCode,
      final Map<String, BaseEntity> findBaseEntityByCode) {
    List<Map> obj5 = null;
    try {
      obj5 = row2DoubleTuples(Ask.class.getSimpleName());
    } catch (final IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    return obj5.stream().map(object -> {
      final Map<String, Ask> map = new HashMap<String, Ask>();
      final String qCode = (String) object.get("question_code");
      object.get("name");
      final String source = (String) object.get("source");
      Question q;
      q = findQuestionByCode.get(qCode);
      final BaseEntity s = findBaseEntityByCode.get(source);
      return map;
    }).reduce((ac, acc) -> {
      ac.putAll(acc);
      return ac;
    }).get();
  }
}
