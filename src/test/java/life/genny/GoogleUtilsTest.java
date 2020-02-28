package life.genny;

import static java.lang.System.out;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeLink;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.datatype.DataType;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.validation.Validation;
import life.genny.qwandautils.GennySheets;
import life.genny.qwandautils.GennySheets2;

public class GoogleUtilsTest {
	
	/**
	 * Stores logger object.
	 */
	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

  GennySheets2 sheets2;

  // @Test
  public void getData2() {
    // sheets = new GennySheets(
    // "{\"installed\":{\"client_id\":\"260075856207-9d7a02ekmujr2bh7i53dro28n132iqhe.apps.googleusercontent.com\",\"project_id\":\"genny-sheets-181905\",\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"token_uri\":\"https://accounts.google.com/o/oauth2/token\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\",\"client_secret\":\"vgXEFRgQvh3_t_e5Hj-eb6IX\",\"redirect_uris\":[\"urn:ietf:wg:oauth:2.0:oob\",\"http://localhost\"]}}",
    // "1VSXJUn8_BHG1aW0DQrFDnvLjx_jxcNiD33QzqO5D-jc", new File(System.getProperty("user.home"),
    // ".credentials/sheets.googleapis.com-java-quickstart"));

    sheets2 = new GennySheets2(
        "{\"installed\":{\"client_id\":\"260075856207-9d7a02ekmujr2bh7i53dro28n132iqhe.apps.googleusercontent.com\",\"project_id\":\"genny-sheets-181905\",\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"token_uri\":\"https://accounts.google.com/o/oauth2/token\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\",\"client_secret\":\"vgXEFRgQvh3_t_e5Hj-eb6IX\",\"redirect_uris\":[\"urn:ietf:wg:oauth:2.0:oob\",\"http://localhost\"]}}",
        "1-h7cmgJyUf2Xg7fd9icCJAdLjj6oXtYFPXGqd8En7sM", new File(System.getProperty("user.home"),
            ".credentials/sheets.googleapis.com-java-quickstart"));



    // getBaseEntity();
    // getAttributes();
    // getEntityAttribute();
    // getEntityEntity();
    // getValidation();
    // getA();
    log.info("sheets validations");
    final Map<String, Validation> daa = sheets2.validationData();
    // sheets.validationData().entrySet().stream().forEach(out::println);
    final Map<String, DataType> dao = sheets2.dataTypesData(daa);
    sheets2.attributesData(dao);

    final Map<String, BaseEntity> bes = sheets2.baseEntityData();
    // sheets.attr2BaseEntitys(atrr, bes).entrySet().stream().forEach(out::println);
    final Map<String, AttributeLink> attrLink = sheets2.attrLink();
    sheets2.be2BeTarget(attrLink, bes).entrySet().stream().forEach(out::println);
    // Map<String, Question> question = sheets.questionsData(atrr);
    // sheets.asksData(question, bes).entrySet().stream().forEach(out::println);

  }

  public void getBaseEntity2() {
    List<BaseEntity> bes = null;
    try {
      bes = sheets2.getBeans(BaseEntity.class);
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    bes.forEach(out::println);
  }

  public void getAttributes2() {
    List<Attribute> attr = null;
    try {
      attr = sheets2.getBeans(Attribute.class);
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    attr.forEach(out::println);
  }

  public void getEntityAttribute2() {
    List<EntityAttribute> entAttr = null;
    try {
      entAttr = sheets2.getBeans(EntityAttribute.class);
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    entAttr.forEach(out::println);
  }

  public void getEntityEntity2() {
    List<EntityEntity> entEnt = null;
    try {
      entEnt = sheets2.getBeans(EntityEntity.class);
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    entEnt.forEach(out::println);
  }

  public void getValidation2() {
    List<Validation> validation = null;
    try {
      validation = sheets2.getBeans(Validation.class);
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    validation.forEach(out::println);
  }

  public void getA2() {
    List<Map> objs = null;
    try {
      objs = sheets2.row2DoubleTuples("DataType");
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    objs.stream().forEach(out::println);

  }
 
  GennySheets sheets;

    // @Test
    public void getData() {
      // sheets = new GennySheets(
      // "{\"installed\":{\"client_id\":\"260075856207-9d7a02ekmujr2bh7i53dro28n132iqhe.apps.googleusercontent.com\",\"project_id\":\"genny-sheets-181905\",\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"token_uri\":\"https://accounts.google.com/o/oauth2/token\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\",\"client_secret\":\"vgXEFRgQvh3_t_e5Hj-eb6IX\",\"redirect_uris\":[\"urn:ietf:wg:oauth:2.0:oob\",\"http://localhost\"]}}",
      // "1VSXJUn8_BHG1aW0DQrFDnvLjx_jxcNiD33QzqO5D-jc", new File(System.getProperty("user.home"),
      // ".credentials/sheets.googleapis.com-java-quickstart"));

      sheets = new GennySheets(
          "{\"installed\":{\"client_id\":\"260075856207-9d7a02ekmujr2bh7i53dro28n132iqhe.apps.googleusercontent.com\",\"project_id\":\"genny-sheets-181905\",\"auth_uri\":\"https://accounts.google.com/o/oauth2/auth\",\"token_uri\":\"https://accounts.google.com/o/oauth2/token\",\"auth_provider_x509_cert_url\":\"https://www.googleapis.com/oauth2/v1/certs\",\"client_secret\":\"vgXEFRgQvh3_t_e5Hj-eb6IX\",\"redirect_uris\":[\"urn:ietf:wg:oauth:2.0:oob\",\"http://localhost\"]}}",
          "1-h7cmgJyUf2Xg7fd9icCJAdLjj6oXtYFPXGqd8En7sM", new File(System.getProperty("user.home"),
              ".credentials/sheets.googleapis.com-java-quickstart"));



      // getBaseEntity();
      // getAttributes();
      // getEntityAttribute();
      // getEntityEntity();
      // getValidation();
      // getA();
      log.info("sheets validations");
//      final Map<String, Validation> daa = sheets.validationData();
//      // sheets.validationData().entrySet().stream().forEach(out::println);
//      final Map<String, DataType> dao = sheets.dataTypesData(daa);
//      sheets.attributesData(dao);
  //
//      final Map<String, BaseEntity> bes = sheets.baseEntityData();
//      // sheets.attr2BaseEntitys(atrr, bes).entrySet().stream().forEach(out::println);
//      final Map<String, AttributeLink> attrLink = sheets.attrLink();
//      sheets.be2BeTarget(attrLink, bes).entrySet().stream().forEach(out::println);
      // Map<String, Question> question = sheets.questionsData(atrr);
      // sheets.asksData(question, bes).entrySet().stream().forEach(out::println);

    }

    public void getBaseEntity() {
      List<BaseEntity> bes = null;
      try {
        bes = sheets.getBeans(BaseEntity.class);
      } catch (final IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      bes.forEach(out::println);
    }

    public void getAttributes() {
      List<Attribute> attr = null;
      try {
        attr = sheets.getBeans(Attribute.class);
      } catch (final IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      attr.forEach(out::println);
    }

    public void getEntityAttribute() {
      List<EntityAttribute> entAttr = null;
      try {
        entAttr = sheets.getBeans(EntityAttribute.class);
      } catch (final IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      entAttr.forEach(out::println);
    }

    public void getEntityEntity() {
      List<EntityEntity> entEnt = null;
      try {
        entEnt = sheets.getBeans(EntityEntity.class);
      } catch (final IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      entEnt.forEach(out::println);
    }

    public void getValidation() {
      List<Validation> validation = null;
      try {
        validation = sheets.getBeans(Validation.class);
      } catch (final IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      validation.forEach(out::println);
    }

    public void getA() {
      List<Map> objs = null;
      try {
        objs = sheets.row2DoubleTuples("DataType");
      } catch (final IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      objs.stream().forEach(out::println);

    }
  
  
  
}
