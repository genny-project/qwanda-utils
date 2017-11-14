package life.genny;


import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import org.apache.logging.log4j.Logger;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.junit.Test;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mortbay.log.Log;
import javax.persistence.Query;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import life.genny.qwanda.Answer;
import life.genny.qwanda.AnswerLink;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwandautils.KeycloakService;

public class JPAHibernateCRUDTest extends JPAHibernateTest {



  private static final Logger log = org.apache.logging.log4j.LogManager
      .getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());


//  @Test
  public void saveAnswerTest() {
    final Gson gson = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {
          @Override
          public LocalDateTime deserialize(final JsonElement json, final Type type,
              final JsonDeserializationContext jsonDeserializationContext)
              throws JsonParseException {
            return ZonedDateTime.parse(json.getAsJsonPrimitive().getAsString()).toLocalDateTime();
          }

          public JsonElement serialize(final LocalDateTime date, final Type typeOfSrc,
              final JsonSerializationContext context) {
            return new JsonPrimitive(date.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)); // "yyyy-mm-dd"
          }
        }).create();


    String json = "{ " + "\"created\": \"2014-11-01T12:34:56+10:00\"," + "\"value\": \"Bob\","
        + "\"expired\": false," + "\"refused\": false," + "\"weight\": 1," + "\"version\": 1,"
        + "\"targetCode\": \"PER_USER1\"," + "\"sourceCode\": \"PER_USER1\","
        + "\"attributeCode\": \"PRI_FIRSTNAME\"" + "}";

    final Answer answer = gson.fromJson(json, Answer.class);
    log.info("Answer loaded :" + answer);
    final Long answerId = service.insert(answer);

    log.info("answerId=" + answerId);

    json = "{ " + "\"created\": \"2014-11-01T12:34:57+10:00\"," + "\"value\": \"Console\","
        + "\"expired\": false," + "\"refused\": false," + "\"weight\": 1," + "\"version\": 1,"
        + "\"targetCode\": \"PER_USER1\"," + "\"sourceCode\": \"PER_USER1\","
        + "\"attributeCode\": \"PRI_LASTNAME\"" + "}";

    // final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();


    final Answer answer2 = gson.fromJson(json, Answer.class);
    log.info("Answer2 loaded :" + answer2);
    final Long answerId2 = service.insert(answer2);

    log.info("answerId2=" + answerId2);


    final BaseEntity person = service.findBaseEntityByCode("PER_USER1");
    try {
      final AnswerLink al = person.addAnswer(answer, 1.0);
      final AnswerLink al2 = person.addAnswer(answer2, 1.0);
      service.insert(al);
      service.insert(al2);
      service.update(person);
    } catch (final BadDataException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    final List<AnswerLink> answers = service.findAnswerLinks();
    log.info(answers);

    final List<AnswerLink> al2 =
        service.findAnswerLinksByCodes("PER_USER1", "PER_USER1", "PRI_FIRSTNAME");
    log.info(al2);
  }

//  @Test
  public void fetchAttribute() {
    final Attribute at = service.findAttributeByCode("PRI_FIRSTNAME");
    log.info(at);
  }

//  @Test
  public void fetchBE() {
    final BaseEntity be = service.findBaseEntityByCode("PER_USER1");
    log.info(be);
  }

//  @Test
  public void countBE() {
    final Long count =
        (Long) em.createQuery("SELECT count(be) FROM BaseEntity be where  be.code=:sourceCode")
            .setParameter("sourceCode", "PER_USER1").getSingleResult();
    assertThat(count, is(1L));
  }

//  @Test
  public void sqlCountTest() {
    final String sql =
        "SELECT count(distinct be) FROM BaseEntity be,EntityEntity ee where ee.pk.target.code=be.code and ee.pk.linkAttribute.code=:linkAttributeCode and ee.pk.source.code=:sourceCode";

    final Long count = (Long) em.createQuery(sql).setParameter("sourceCode", "GRP_USERS")
        .setParameter("linkAttributeCode", "LNK_CORE").getSingleResult();
    log.info("Number of users = " + count);

    assertThat(count, is(3L));
  }

//  @Test
  public void sqlBETest() {
    final String sql =
        "SELECT be FROM BaseEntity be,EntityEntity ee where ee.pk.target.code=be.code and ee.pk.linkAttribute.code=:linkAttributeCode and ee.pk.source.code=:sourceCode";


    final List<BaseEntity> eeResults = em.createQuery(sql).setParameter("sourceCode", "GRP_USERS")
        .setParameter("linkAttributeCode", "LNK_CORE").setFirstResult(0).setMaxResults(1000)
        .getResultList();

    log.info("Number of users = " + eeResults.size());

    assertThat(eeResults.size(), is(3));
  }



//  @Test
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

//  @Test
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

//  @Test
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

//  @Test
  public void getChildrenWithAttributesPaged() {
    System.out.println("\n\n******************* KIDS WITH ATTRIBUTE!**************");
    final MultivaluedMap<String, String> qparams = new MultivaluedMapImpl<String, String>();
    qparams.add("pageStart", "0");
    qparams.add("pageSize", "10");
    final String sourceCode = "GRP_USERS";
    final String linkCode = "LNK_CORE";


    qparams.add("PRI_USERNAME", "user1");
    // params.add("PRI_USERNAME", "user2");

    Integer pageStart = 0;
    Integer pageSize = 10; // default
    final Boolean includeAttributes = true;
    final MultivaluedMap<String, String> params = new MultivaluedMapImpl<String, String>();
    params.putAll(qparams);

    final String pageStartStr = params.getFirst("pageStart");
    final String pageSizeStr = params.getFirst("pageSize");
    final String levelStr = params.getFirst("level");
    if (pageStartStr != null) {
      pageStart = Integer.decode(pageStartStr);
      params.remove("pageStart");
    }
    if (pageSizeStr != null) {
      pageSize = Integer.decode(pageSizeStr);
      params.remove("pageSize");
    }
    if (levelStr != null) {
      Integer.decode(levelStr);
      params.remove("level");
    }


    final List<BaseEntity> eeResults;
    new HashMap<String, BaseEntity>();

    if (includeAttributes) {


      // ugly and insecure
      final Integer pairCount = params.size();
      if (pairCount == 0) {
        eeResults = em.createQuery(
            "SELECT distinct be FROM BaseEntity be,EntityEntity ee JOIN be.baseEntityAttributes bee where ee.pk.target.code=be.code and ee.pk.linkAttribute.code=:linkAttributeCode and ee.pk.source.code=:sourceCode")
            .setParameter("sourceCode", sourceCode).setParameter("linkAttributeCode", linkCode)
            .setFirstResult(pageStart).setMaxResults(pageSize).getResultList();


      } else {
        System.out.println("PAIR COUNT IS NOT ZERO " + pairCount);
        String eaStrings = "";
        String eaStringsQ = "";
        if (pairCount > 0) {
          eaStringsQ = "(";
          for (int i = 0; i < (pairCount); i++) {
            eaStrings += ",EntityAttribute ea" + i;
            eaStringsQ += "ea" + i + ".baseEntityCode=be.code or ";
          }
          eaStringsQ = eaStringsQ.substring(0, eaStringsQ.length() - 4);
          eaStringsQ += ") and ";
        }


        String queryStr = "SELECT distinct be FROM BaseEntity be,EntityEntity ee" + eaStrings
            + "  JOIN be.baseEntityAttributes bee where " + eaStringsQ
            + "  ee.pk.target.code=be.code and ee.pk.linkAttribute.code=:linkAttributeCode and ee.pk.source.code=:sourceCode and ";
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
        query.setParameter("sourceCode", sourceCode).setParameter("linkAttributeCode", linkCode);

        query.setFirstResult(pageStart).setMaxResults(pageSize);
        eeResults = query.getResultList();


      }
    } else {
      Log.info("**************** ENTITY ENTITY WITH NO ATTRIBUTES ****************");


      // ugly and insecure
      final Integer pairCount = params.size();
      if (pairCount == 0) {
        eeResults = em.createQuery(
            "SELECT distinct be FROM BaseEntity be,EntityEntity ee  where ee.pk.target.code=be.code and ee.pk.linkAttribute.code=:linkAttributeCode and ee.pk.source.code=:sourceCode")
            .setParameter("sourceCode", sourceCode).setParameter("linkAttributeCode", linkCode)
            .setFirstResult(pageStart).setMaxResults(pageSize).getResultList();


      } else {
        System.out.println("PAIR COUNT  " + pairCount);
        String eaStrings = "";
        String eaStringsQ = "";
        if (pairCount > 0) {
          eaStringsQ = "(";
          for (int i = 0; i < (pairCount); i++) {
            eaStrings += ",EntityAttribute ea" + i;
            eaStringsQ += "ea" + i + ".baseEntityCode=be.code or ";
          }
          eaStringsQ = eaStringsQ.substring(0, eaStringsQ.length() - 4);
          eaStringsQ += ") and ";
        }

        String queryStr = "SELECT distinct be FROM BaseEntity be,EntityEntity ee" + eaStrings
            + "  where " + eaStringsQ
            + " ee.pk.target.code=be.code and ee.pk.linkAttribute.code=:linkAttributeCode and ee.pk.source.code=:sourceCode and ";
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
        query.setParameter("sourceCode", sourceCode).setParameter("linkAttributeCode", linkCode);

        query.setFirstResult(pageStart).setMaxResults(pageSize);
        eeResults = query.getResultList();

      }
      for (final BaseEntity be : eeResults) {
        be.setBaseEntityAttributes(null); // ugly
      }

    }
    System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++");
    // TODO: improve

    // final List<BaseEntity> results = beMap.values().stream().collect(Collectors.toList());

  }

//  @Test
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


  // @Test
  public void getKeycloakUsersTest() {
    KeycloakService ks;
    final Map<String, Map<String, Object>> usersMap = new HashMap<String, Map<String, Object>>();

    try {
      ks = new KeycloakService("https://bouncer.outcome-hub.com", "channel40", "service",
          "WelcomeToTheTruck", "channel40");
      final List<LinkedHashMap> users = ks.fetchKeycloakUsers();
      for (final Object user : users) {
        final LinkedHashMap map = (LinkedHashMap) user;
        final Map<String, Object> userMap = new HashMap<String, Object>();
        for (final Object key : map.keySet()) {
          // System.out.println(key + ":" + map.get(key));
          userMap.put((String) key, map.get(key));

        }
        usersMap.put((String) userMap.get("username"), userMap);
        System.out.println();
      }

      System.out.println("finished");
    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    for (final String username : usersMap.keySet()) {
      final MultivaluedMap params = new MultivaluedMapImpl();
      params.add("PRI_USERNAME", username);
      final Map<String, Object> userMap = usersMap.get(username);

      final List<BaseEntity> users = service.findBaseEntitysByAttributeValues(params, true, 0, 1);
      if (users.isEmpty()) {
        final String code = "PER_CH40_" + username;
        final String firstName = (String) userMap.get("firstName");
        final String lastName = (String) userMap.get("lastName");
        final String name = firstName + " " + lastName;
        final String email = (String) userMap.get("email");
        final String id = (String) userMap.get("id");
        final Long unixSeconds = (Long) userMap.get("createdTimestamp");
        final Date date = new Date(unixSeconds); // *1000 is to convert seconds to milliseconds
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z"); // the format of
                                                                                    // your date
        sdf.setTimeZone(TimeZone.getTimeZone("GMT+10")); // give a timezone reference for formating
        sdf.format(date);
        final Attribute firstNameAtt = service.findAttributeByCode("PRI_FIRSTNAME");
        final Attribute lastNameAtt = service.findAttributeByCode("PRI_LASTNAME");
        final Attribute nameAtt = service.findAttributeByCode("PRI_NAME");
        final Attribute emailAtt = service.findAttributeByCode("PRI_EMAIL");
        final Attribute uuidAtt = service.findAttributeByCode("PRI_UUID");
        final Attribute usernameAtt = service.findAttributeByCode("PRI_USERNAME");

        try {
          final BaseEntity user = new BaseEntity(code, name);

          user.addAttribute(firstNameAtt, 0.0, firstName);
          user.addAttribute(lastNameAtt, 0.0, lastName);
          user.addAttribute(nameAtt, 0.0, name);
          user.addAttribute(emailAtt, 0.0, email);
          user.addAttribute(uuidAtt, 0.0, id);
          user.addAttribute(usernameAtt, 0.0, username);
          service.insert(user);

          System.out.println("BE:" + user);
        } catch (final BadDataException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }

      } else {
        users.get(0);
      }

    }

  }

  public void updateUser(final String realm, final String keycloakId, final String fn,
      final String ln) throws Exception {
    KeycloakService ks;

    ks = new KeycloakService("https://bouncer.outcome-hub.com", "channel40", "service",
        "WelcomeToTheTruck", "channel40");

    final UserResource userResource = ks.getKeycloak().realm(realm).users().get(keycloakId);
    final UserRepresentation user = userResource.toRepresentation();
    user.setFirstName(fn);
    user.setLastName(ln);
    userResource.update(user);

  }


}
