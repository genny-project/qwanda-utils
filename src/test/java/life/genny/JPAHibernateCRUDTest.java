package life.genny;


import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.junit.Test;
import org.mortbay.log.Log;
import javax.persistence.Query;
import javax.ws.rs.core.MultivaluedMap;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.message.QDataBaseEntityMessage;

public class JPAHibernateCRUDTest extends JPAHibernateTest {

  private static final Logger log = org.apache.logging.log4j.LogManager
      .getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

  @Test
  public void fetchAttribute() {
    final Attribute at = service.findAttributeByCode("PRI_FIRSTNAME");
    log.info(at);
  }

  @Test
  public void fetchBE() {
    final BaseEntity be = service.findBaseEntityByCode("PER_USER1");
    log.info(be);
  }

  @Test
  public void countBE() {
    final Long count =
        (Long) em.createQuery("SELECT count(be) FROM BaseEntity be where  be.code=:sourceCode")
            .setParameter("sourceCode", "PER_USER1").getSingleResult();
    assertThat(count, is(1L));
  }

  @Test
  public void sqlCountTest() {
    final String sql =
        "SELECT count(be) FROM BaseEntity be,EntityEntity ee where ee.pk.target.code=be.code and ee.pk.linkAttribute.code=:linkAttributeCode and ee.pk.source.code=:sourceCode";

    final Long count = (Long) em.createQuery(sql).setParameter("sourceCode", "GRP_USERS")
        .setParameter("linkAttributeCode", "LNK_CORE").getSingleResult();
    log.info("Number of users = " + count);

    assertThat(count, is(3L));
  }

  @Test
  public void sqlBETest() {
    final String sql =
        "SELECT be FROM BaseEntity be,EntityEntity ee where ee.pk.target.code=be.code and ee.pk.linkAttribute.code=:linkAttributeCode and ee.pk.source.code=:sourceCode";


    final List<BaseEntity> eeResults = em.createQuery(sql).setParameter("sourceCode", "GRP_USERS")
        .setParameter("linkAttributeCode", "LNK_CORE").setFirstResult(0).setMaxResults(1000)
        .getResultList();

    log.info("Number of users = " + eeResults.size());

    assertThat(eeResults.size(), is(3));
  }



  @Test
  public void sqlBEandAttributesTest() {

    final String sql =
        "SELECT distinct be FROM BaseEntity be,EntityEntity ee JOIN be.baseEntityAttributes bea where ee.pk.target.code=be.code and ee.pk.linkAttribute.code=:linkAttributeCode and ee.pk.source.code=:sourceCode";


    final List<BaseEntity> eeResults = em.createQuery(sql).setParameter("sourceCode", "GRP_USERS")
        .setParameter("linkAttributeCode", "LNK_CORE").setFirstResult(0).setMaxResults(1000)
        .getResultList();

    log.info("Number of users = " + eeResults.size());

    assertThat(eeResults.size(), is(3));
    assertThat(eeResults.get(0).getBaseEntityAttributes().size(), greaterThan(5));
  }

  @Test
  public void test_Query() {
    final String sourceCode = "GRP_USERS";
    final String linkCode = "LNK_CORE";

    Integer pageStart = 0;
    Integer pageSize = 10; // default
    Integer level = 1;

    final MultivaluedMap params = new MultivaluedMapImpl();
    params.add("pageStart", "0");
    params.add("pageSize", "2");

    params.add("PRI_USERNAME", "user1");

    final String pageStartStr = (String) params.getFirst("pageStart");
    final String pageSizeStr = (String) params.getFirst("pageSize");
    final String levelStr = (String) params.getFirst("level");
    if (pageStartStr != null && pageSizeStr != null) {
      pageStart = Integer.decode(pageStartStr);
      pageSize = Integer.decode(pageSizeStr);
    }
    if (levelStr != null) {
      level = Integer.decode(levelStr);
    }
    final List<BaseEntity> targets = service.findChildrenByAttributeLink(sourceCode, linkCode,
        false, pageStart, pageSize, level);

    BaseEntity[] beArr = new BaseEntity[targets.size()];
    assertThat(beArr.length, is(2));
    // assertThat(beArr[0].getBaseEntityAttributes().size(), is(7));
    beArr = targets.toArray(beArr);
    new QDataBaseEntityMessage(beArr, sourceCode, linkCode);
  }

  // @Test
  // public void testGetObjectById_success() {
  // final Book book = em.find(Book.class, 1);
  // assertNotNull(book);
  // }
  //
  // @Test
  // public void testGetAll_success() {
  // final List<Book> books = em.createNamedQuery("Book.getAll", Book.class).getResultList();
  // assertEquals(1, books.size());
  // }
  //
  // @Test
  // public void testPersist_success() {
  // em.getTransaction().begin();
  // em.persist(new Book(10, "Unit Test Hibernate/JPA with in memory H2 Database"));
  // em.getTransaction().commit();
  //
  // final List<Book> books = em.createNamedQuery("Book.getAll", Book.class).getResultList();
  //
  // assertNotNull(books);
  // assertEquals(2, books.size());
  // }
  //
  // @Test
  // public void testDelete_success() {
  // final Book book = em.find(Book.class, 1);
  //
  // em.getTransaction().begin();
  // em.remove(book);
  // em.getTransaction().commit();
  //
  // final List<Book> books = em.createNamedQuery("Book.getAll", Book.class).getResultList();
  //
  // assertEquals(0, books.size());
  // }

  @Test
  public void getBesWithAttributesPaged() {
    Integer pageStart = 0;
    Integer pageSize = 10; // default
    final MultivaluedMap<String, String> params = new MultivaluedMapImpl<String, String>();
    params.add("pageStart", "0");
    params.add("pageSize", "2");

    params.add("PRI_USERNAME", "user1");
    params.add("PRI_USERNAME", "user2");

    final String pageStartStr = params.getFirst("pageStart");
    final String pageSizeStr = params.getFirst("pageSize");
    final String levelStr = params.getFirst("level");
    if (pageStartStr != null && pageSizeStr != null) {
      pageStart = Integer.decode(pageStartStr);
      pageSize = Integer.decode(pageSizeStr);
    }
    if (levelStr != null) {
      Integer.decode(levelStr);
    }


    final List<BaseEntity> eeResults;
    new HashMap<String, BaseEntity>();


    Log.info("**************** BE SEARCH WITH ATTRIBUTE VALUE WITH ATTRIBUTES!! pageStart = "
        + pageStart + " pageSize=" + pageSize + " ****************");

    // ugly and insecure
    final Integer pairCount = params.size();
    if (pairCount.equals(0)) {
      eeResults =
          em.createQuery("SELECT distinct be FROM BaseEntity be JOIN be.baseEntityAttributes bee")
              .setFirstResult(pageStart).setMaxResults(pageSize).getResultList();
    } else {
      String queryStr =
          "SELECT distinct be FROM BaseEntity be JOIN be.baseEntityAttributes bee where  ";
      int attributeCodeIndex = 0;
      int valueIndex = 0;
      final List<String> attributeCodeList = new ArrayList<String>();
      final List<String> valueList = new ArrayList<String>();

      for (final Map.Entry<String, List<String>> entry : params.entrySet()) {
        if (entry.getKey().equals("pageStart") || entry.getKey().equals("pageSize")) { // ugly
          continue;
        }
        final List<String> qvalueList = entry.getValue();
        if (!qvalueList.isEmpty()) {
          // create the value or
          String valueQuery = "(";
          for (final String value : qvalueList) {
            valueQuery += "bee.valueString=:valueString" + valueIndex + " or ";
            valueList.add(valueIndex, value);
            valueIndex++;
          }
          // remove last or
          valueQuery = valueQuery.substring(0, valueQuery.length() - 4);
          valueQuery += ")";
          attributeCodeList.add(attributeCodeIndex, entry.getKey());
          if (attributeCodeIndex > 0) {
            queryStr += " and ";
          }
          queryStr +=
              " bee.attributeCode=:attributeCode" + attributeCodeIndex + " and " + valueQuery;
          System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
        }
        attributeCodeIndex++;

      }
      System.out.println("Query=" + queryStr);
      final Query query = em.createQuery(queryStr);
      int index = 0;
      for (final String attributeParm : attributeCodeList) {
        query.setParameter("attributeCode" + index, attributeParm);
        System.out.println("attributeCode" + index + "=:" + attributeParm);
        index++;
      }
      index = 0;
      for (final String valueParm : valueList) {
        query.setParameter("valueString" + index, valueParm);
        System.out.println("valueString" + index + "=:" + valueParm);
        index++;
      }
      query.setFirstResult(pageStart).setMaxResults(pageSize).getResultList();
      eeResults = query.getResultList();
    }
    if (eeResults.isEmpty()) {
      System.out.println("EEE IS EMPTY");
    } else {
      System.out.println("EEE Count" + eeResults.size());
      System.out.println("EEE" + eeResults);
    }
    for (final BaseEntity be : eeResults) {
      System.out.println(be.getCode() + " + attributes");
      be.getBaseEntityAttributes().stream().forEach(p -> System.out.println(p.getAttributeCode()));
    }

    // TODO: improve

    // final List<BaseEntity> results = beMap.values().stream().collect(Collectors.toList());

  }

  @Test
  public void getChildrenWithAttributesPaged() {
    System.out.println("\n\n******************* KIDS WITH ATTRIBUTE!**************");
    Integer pageStart = 0;
    Integer pageSize = 10; // default
    final MultivaluedMap<String, String> params = new MultivaluedMapImpl<String, String>();
    params.add("pageStart", "0");
    params.add("pageSize", "10");

    params.add("PRI_USERNAME", "user1");
    // params.add("PRI_USERNAME", "user2");

    final String pageStartStr = params.getFirst("pageStart");
    final String pageSizeStr = params.getFirst("pageSize");
    final String levelStr = params.getFirst("level");
    if (pageStartStr != null && pageSizeStr != null) {
      pageStart = Integer.decode(pageStartStr);
      pageSize = Integer.decode(pageSizeStr);
    }
    if (levelStr != null) {
      Integer.decode(levelStr);
    }


    final List<BaseEntity> eeResults;
    new HashMap<String, BaseEntity>();


    log.info("**************** BE SEARCH WITH ATTRIBUTE VALUE WITH ATTRIBUTES!! pageStart = "
        + pageStart + " pageSize=" + pageSize + " ****************");

    // ugly and insecure
    final Integer pairCount = params.size();
    if (pairCount.equals(2)) {
      eeResults = em.createQuery(
          "SELECT distinct be FROM BaseEntity be,EntityEntity ee JOIN be.baseEntityAttributes bee where ee.pk.target.code=be.code and ee.pk.linkAttribute.code=:linkAttributeCode and ee.pk.source.code=:sourceCode")
          .setParameter("sourceCode", "GRP_USERS").setParameter("linkAttributeCode", "LNK_CORE")
          .setFirstResult(pageStart).setMaxResults(pageSize).getResultList();


    } else {
      System.out.println("PAIR COUNT IS NOT ZERO " + pairCount);
      String eaStrings = "";
      String eaStringsQ = "(";
      for (int i = 0; i < (pairCount - 2); i++) {
        eaStrings += ",EntityAttribute ea" + i;
        eaStringsQ += "ea" + i + ".baseEntityCode=be.code or ";
      }
      eaStringsQ = eaStringsQ.substring(0, eaStringsQ.length() - 4);
      eaStringsQ += ")";

      String queryStr = "SELECT distinct be FROM BaseEntity be,EntityEntity ee" + eaStrings
          + "  JOIN be.baseEntityAttributes bee where " + eaStringsQ
          + " and ee.pk.target.code=be.code and ee.pk.linkAttribute.code=:linkAttributeCode and ee.pk.source.code=:sourceCode and ";
      int attributeCodeIndex = 0;
      int valueIndex = 0;
      final List<String> attributeCodeList = new ArrayList<String>();
      final List<String> valueList = new ArrayList<String>();

      for (final Map.Entry<String, List<String>> entry : params.entrySet()) {
        if (entry.getKey().equals("pageStart") || entry.getKey().equals("pageSize")) { // ugly
          continue;
        }
        final List<String> qvalueList = entry.getValue();
        if (!qvalueList.isEmpty()) {
          // create the value or
          String valueQuery = "(";
          for (final String value : qvalueList) {
            valueQuery +=
                "ea" + attributeCodeIndex + ".valueString=:valueString" + valueIndex + " or ";
            valueList.add(valueIndex, value);
            valueIndex++;
          }
          // remove last or
          valueQuery = valueQuery.substring(0, valueQuery.length() - 4);
          valueQuery += ")";
          attributeCodeList.add(attributeCodeIndex, entry.getKey());
          if (attributeCodeIndex > 0) {
            queryStr += " and ";
          }
          queryStr += " ea" + attributeCodeIndex + ".attributeCode=:attributeCode"
              + attributeCodeIndex + " and " + valueQuery;
          System.out.println("Key : " + entry.getKey() + " Value : " + entry.getValue());
        }
        attributeCodeIndex++;

      }
      System.out.println("KIDS + ATTRIBUTE Query=" + queryStr);
      final Query query = em.createQuery(queryStr);
      int index = 0;
      for (final String attributeParm : attributeCodeList) {
        query.setParameter("attributeCode" + index, attributeParm);
        System.out.println("attributeCode" + index + "=:" + attributeParm);
        index++;
      }
      index = 0;
      for (final String valueParm : valueList) {
        query.setParameter("valueString" + index, valueParm);
        System.out.println("valueString" + index + "=:" + valueParm);
        index++;
      }
      query.setParameter("sourceCode", "GRP_USERS").setParameter("linkAttributeCode", "LNK_CORE");

      query.setFirstResult(pageStart).setMaxResults(pageSize);
      eeResults = query.getResultList();
    }
    if (eeResults.isEmpty()) {
      System.out.println("EEE IS EMPTY");
    } else {
      System.out.println("EEE Count" + eeResults.size());
      System.out.println("EEE" + eeResults);
    }
    for (final BaseEntity be : eeResults) {
      System.out.println("\n" + be.getCode() + " + attributes");
      be.getBaseEntityAttributes().stream().forEach(p -> System.out.println(p.getAttributeCode()));
    }
    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    // TODO: improve

    // final List<BaseEntity> results = beMap.values().stream().collect(Collectors.toList());

  }

  @Test
  public void sqlBEFilterTest() {
    // final String sql =
    // "SELECT distinct be FROM BaseEntity be,EntityEntity ee,EntityAttribute ea0,EntityAttribute
    // ea1 JOIN be.baseEntityAttributes bee where (ea0.baseEntityCode=be.code or
    // ea1.baseEntityCode=be.code) and ee.pk.target.code=be.code and
    // ee.pk.linkAttribute.code=:linkAttributeCode and ee.pk.source.code=:sourceCode and
    // ea0.attributeCode=:attributeCode0 and (ea0.valueString='user1') and
    // ea1.attributeCode=:attributeCode1 and (ea1.valueString='user2')";
    final String sql =
        // "SELECT distinct be FROM BaseEntity be,EntityEntity ee,EntityAttribute ea0 JOIN
        // be.baseEntityAttributes bee where (ea0.baseEntityCode=be.code ) and
        // ee.pk.target.code=be.code and ee.pk.linkAttribute.code=:linkAttributeCode and
        // ee.pk.source.code=:sourceCode and ea0.attributeCode=:attributeCode0 and
        // (ea0.valueString=:valueString0)";
        "SELECT distinct be FROM BaseEntity be,EntityEntity ee,EntityAttribute ea0  JOIN be.baseEntityAttributes bee where (ea0.baseEntityCode=be.code) and ee.pk.target.code=be.code and ee.pk.linkAttribute.code=:linkAttributeCode and ee.pk.source.code=:sourceCode and  ea0.attributeCode=:attributeCode0 and (ea0.valueString=:valueString0)";
    try {
      final List<BaseEntity> eeResults = em.createQuery(sql).setParameter("sourceCode", "GRP_USERS")
          .setParameter("linkAttributeCode", "LNK_CORE")
          .setParameter("attributeCode0", "PRI_USERNAME").setParameter("valueString0", "user1")
          .setFirstResult(0).setMaxResults(1000).getResultList();

      log.info("Number of users = " + eeResults.size());

      assertThat(eeResults.size(), is(1));
    } catch (final Exception e) {
      // TODO Auto-generated catch block
      log.info("NO RESULT");
    }
  }
}
