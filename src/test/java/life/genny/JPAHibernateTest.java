package life.genny;


import org.apache.logging.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.OptimisticLockException;
import javax.persistence.Persistence;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import life.genny.qwandautils.BaseEntityService;
import life.genny.qwandautils.GennySheets;

public class JPAHibernateTest {

  private static final Logger log = org.apache.logging.log4j.LogManager
      .getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

  protected static EntityManagerFactory emf;
  protected static EntityManager em;

  private final static String secret = System.getenv("GOOGLE_CLIENT_SECRET");
  private final static String gennySheetId = System.getenv("GOOGLE_SHEETID");
  private final static String channel40 = "1jNe-MOXx8DFxA2kDeCHjdZ7U-4Fqk1cqyhTgsZvUZ4o";
  static File credentialPath = new File(System.getProperty("user.home"), ".credentials/genny");
  static File channelPath = new File(System.getProperty("user.home"), ".credentials/channel");

  protected static BaseEntityService service = null;


  @BeforeClass
  public static void init() throws FileNotFoundException, SQLException {
    emf = Persistence.createEntityManagerFactory("mnf-pu-test");
    em = emf.createEntityManager();
    import_from_google_docs();
  }

  // @Before
  public void initializeDatabase() {
    // final Session session = em.unwrap(Session.class);
    // session.doWork(new Work() {
    // @Override
    // public void execute(final Connection connection) throws SQLException {
    // try {
    // final File script = new File(getClass().getResource("/data.sql").getFile());
    // RunScript.execute(connection, new FileReader(script));
    // } catch (final FileNotFoundException e) {
    // throw new RuntimeException("could not initialize with script");
    // }
    // }
    // });
  }

  // @Before
  public static void import_from_google_docs() {

    service = new BaseEntityService(em);
    em.getTransaction().begin();

    System.out
        .println("**************************** IMPORTING  *************************************");

    final GennySheets gennySheets = new GennySheets(secret, gennySheetId, credentialPath);


    System.out.println(
        "**************************** IMPORTING VALIDATIONS *************************************");

    // Validations
    final Map<String, Validation> validationMap = new HashMap<String, Validation>();

    List<Validation> validations = null;
    try {
      validations = gennySheets.getBeans(Validation.class);
    } catch (final IOException e2) {
      // TODO Auto-generated catch block
      e2.printStackTrace();
    }
    validations.stream().forEach(object -> {
      service.insert(object);
      validationMap.put(object.getCode(), object);
    });

    System.out.println(
        "**************************** IMPORTING DATATYPES *************************************");

    // DataTypes
    final Map<String, DataType> dataTypeMap = new HashMap<String, DataType>();
    List<Map> obj = null;
    try {
      obj = gennySheets.row2DoubleTuples(DataType.class.getSimpleName());
    } catch (final IOException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    obj.stream().forEach(object -> {
      final String code = (String) object.get("code");
      final String name = (String) object.get("name");
      final String validationss = (String) object.get("validations");
      final ValidationList validationList = new ValidationList();
      validationList.setValidationList(new ArrayList<Validation>());
      if (validationss != null) {
        final String[] validationListStr = validationss.split(",");
        for (final String validationCode : validationListStr) {
          validationList.getValidationList().add(validationMap.get(validationCode));
        }
      }
      if (!dataTypeMap.containsKey(code)) {
        final DataType dataType = new DataType(name, validationList);
        dataTypeMap.put(code, dataType);
      }
    });

    System.out.println(
        "**************************** IMPORTING ATTRIBUTES *************************************");

    List<Map> attrs = null;
    try {
      attrs = gennySheets.row2DoubleTuples(Attribute.class.getSimpleName());
    } catch (final IOException e2) {
      // TODO Auto-generated catch block
      e2.printStackTrace();
    }
    attrs.stream().forEach(data -> {
      Attribute attribute = null;
      final String datatype = (String) data.get("datatype");
      try {
        System.out.println("ATTRIBUTE:::Code:" + data.get("code") + ":name" + data.get("name")
            + ":datatype:" + data.get("datatype"));
        attribute = service.findAttributeByCode((String) data.get("code"));

      } catch (final NoResultException e) {
        attribute = new Attribute((String) data.get("code"), (String) data.get("name"),
            dataTypeMap.get(datatype));
        // System.out.println("ATTRIBUTE:::Code:" + data.get("code") + ":name" + data.get("name")
        // + ":datatype:" + data.get("datatype") + "##################" + attribute);
        service.insert(attribute);
      } catch (final OptimisticLockException ee) {
        attribute = new Attribute((String) data.get("code"), (String) data.get("name"),
            dataTypeMap.get(datatype));
        // System.out.println("ATTRIBUTE:::Code:" + data.get("code") + ":name" + data.get("name")
        // + ":datatype:" + data.get("datatype") + "##################" + attribute);
        // service.insert(attribute);
      }
    });
    System.out.println(
        "**************************** IMPORTING BASEENTITYS *************************************");

    List<BaseEntity> bes = null;
    try {
      bes = gennySheets.getBeans(BaseEntity.class);
      bes.stream().forEach(object -> {
        service.insert(object);
      });
    } catch (final IOException e2) {
      // TODO Auto-generated catch block
      e2.printStackTrace();
    }


    if (true) {
      System.out.println(
          "**************************** IMPORTING BASEENTITY - ATTRIBUTE LINKS *************************************");

      // Now link Attributes to Baseentitys
      List<Map> obj2 = null;
      try {
        obj2 = gennySheets.row2DoubleTuples(EntityAttribute.class.getSimpleName());
      } catch (final IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      System.out.println("Number of Links = " + obj2.size());

      obj2.stream().forEach(object -> {
        final String beCode = (String) object.get("baseEntityCode");
        final String attributeCode = (String) object.get("attributeCode");
        final String weightStr = (String) object.get("weight");
        final String valueString = (String) object.get("valueString");
        System.out.println("BECode:" + beCode + ":attCode" + attributeCode + ":weight:" + weightStr
            + ": valueString:" + valueString);
        Attribute attribute = null;
        BaseEntity be = null;
        try {
          attribute = service.findAttributeByCode(attributeCode);
          be = service.findBaseEntityByCode(beCode);
          System.out.println("In EA : found " + be.getCode() + "," + attribute.getCode());
          final Double weight = Double.valueOf(weightStr);
          try {
            be.addAttribute(attribute, weight, valueString);

          } catch (final BadDataException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }

          service.update(be);
        } catch (final NoResultException e) {

        }
      });

      // Now link be to be
      final AttributeLink linkAttribute2 = new AttributeLink("LNK_CORE", "Parent");
      service.insert(linkAttribute2);

      List<Map> obj3 = null;
      try {
        obj3 = gennySheets.row2DoubleTuples(EntityEntity.class.getSimpleName());
      } catch (final IOException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
      }
      obj3.stream().forEach(object -> {
        final String parentCode = (String) object.get("parentCode");
        final String targetCode = (String) object.get("targetCode");
        object.get("linkCode");
        final String weightStr = (String) object.get("weight");
        object.get("valueString");
        BaseEntity sbe = null;
        BaseEntity tbe = null;
        try {
          sbe = service.findBaseEntityByCode(parentCode);
          tbe = service.findBaseEntityByCode(targetCode);
          final Double weight = Double.valueOf(weightStr);
          sbe.addTarget(tbe, linkAttribute2, weight);
          service.update(tbe);
        } catch (final NoResultException e) {

        } catch (final BadDataException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      });
    }

    em.getTransaction().commit();
  }

//  public Map<String, Object> genny(final String projectType, final File path) {
//
//    final GennySheets sheets = new GennySheets(secret, projectType, path);
//
//    final Map<String, Validation> gValidations = sheets.validationData();
//    gValidations.entrySet().stream().map(tuple -> tuple.getValue()).forEach(validation -> {
//      service.insert(validation);
//    });
//    final Map<String, DataType> gDataTypes = sheets.dataTypesData(gValidations);
//    final Map<String, Attribute> gAttrs = sheets.attributesData(gDataTypes);
//    gAttrs.entrySet().stream().map(tuple -> tuple.getValue()).forEach(attr -> {
//      service.insert(attr);
//    });
//    final Map<String, BaseEntity> gBes = sheets.baseEntityData();
//    gBes.entrySet().stream().map(tuple -> tuple.getValue()).forEach(be -> {
//      service.insert(be);
//    });
//    final Map<String, BaseEntity> gAttr2Bes = sheets.attr2BaseEntitys(gAttrs, gBes);
//    gAttr2Bes.entrySet().stream().map(tuple -> tuple.getValue()).forEach(be -> {
//      System.out.println(be + "***********************");
//      service.update(be);
//    });
//    final Map<String, AttributeLink> gAttrLink = sheets.attrLink();
//    gAttrLink.entrySet().stream().map(tuple -> tuple.getValue()).forEach(link -> {
//      service.insert(link);
//    });
//    final Map<String, BaseEntity> gBes2Bes = sheets.be2BeTarget(gAttrLink, gAttr2Bes);
//    gBes2Bes.entrySet().stream().map(tuple -> tuple.getValue()).forEach(tbe -> {
//      service.update(tbe);
//    });
//    final Map<String, Question> gQuestions = sheets.questionsData(gAttrs);
//    gQuestions.entrySet().stream().map(tuple -> tuple.getValue()).forEach(q -> {
//      service.insert(q);
//    });
//    final Map<String, Ask> gAsks = sheets.asksData(gQuestions, gBes);
//    gAsks.entrySet().stream().map(tuple -> tuple.getValue()).forEach(ask -> {
//      service.insert(ask);
//    });
//    final Map<String, Object> genny = new HashMap<String, Object>();
//    genny.put("validations", gValidations);
//    genny.put("dataType", gDataTypes);
//    genny.put("attributes", gAttrs);
//    genny.put("baseEntitys", gBes);
//    genny.put("attibutesEntity", gAttr2Bes);
//    genny.put("attributeLink", gAttrLink);
//    genny.put("basebase", gBes2Bes);
//    genny.put("questions", gQuestions);
//    genny.put("ask", gAsks);
//
//    return genny;
//  }

  @After
  public void aftertest() {
    // log.info("AFTER: Clearing data");
    // org.h2.store.fs.FileUtils.deleteRecursive("mem:test", true);
    // // em.clear();
    // DeleteDbFiles.execute("mem", "test", true);
    // em.createNativeQuery("TRUNCATE SCHEMA PUBLIC RESTART IDENTITY AND COMMIT NO CHECK")
    // .executeUpdate();
  }

  @AfterClass
  public static void tearDown() {
    log.info("Tearing down");
    org.h2.store.fs.FileUtils.deleteRecursive("mem:test", true);
    em.clear();
    em.close();
    emf.close();
  }


}
