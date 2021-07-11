package life.genny.utils;

import java.io.IOException;
import java.io.Serializable;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.naming.NamingException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.mindrot.jbcrypt.BCrypt;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vertx.core.json.JsonObject;
import life.genny.models.GennyToken;
import life.genny.qwanda.Answer;
import life.genny.qwanda.Layout;
import life.genny.qwanda.Link;
import life.genny.qwanda.attribute.Attribute;
import life.genny.qwanda.attribute.AttributeLink;
import life.genny.qwanda.attribute.AttributeText;
import life.genny.qwanda.attribute.EntityAttribute;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.EntityEntity;
import life.genny.qwanda.entity.SearchEntity;
import life.genny.qwanda.exception.BadDataException;
import life.genny.qwanda.message.QBulkPullMessage;
import life.genny.qwanda.message.QDataAnswerMessage;
import life.genny.qwanda.message.QDataBaseEntityMessage;
import life.genny.qwanda.message.QMessage;
import life.genny.qwandautils.GennySettings;
import life.genny.qwandautils.JsonUtils;
import life.genny.qwandautils.QwandaUtils;
import life.genny.utils.Layout.LayoutUtils;

public class BaseEntityUtils implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

	private Map<String, Object> decodedMapToken;
	private String token;
	private String realm;
	private String qwandaServiceUrl;

	private CacheUtils cacheUtil;
	private GennyToken gennyToken;
	private GennyToken serviceToken;

	public BaseEntityUtils(GennyToken userToken) {
		this(GennySettings.qwandaServiceUrl, userToken);
	}

	public BaseEntityUtils(GennyToken serviceToken, GennyToken userToken) {
		this(GennySettings.qwandaServiceUrl, userToken);
		this.serviceToken = serviceToken;
	}

	public BaseEntityUtils(String qwandaServiceUrl, GennyToken gennyToken) {

		this.decodedMapToken = gennyToken.getAdecodedTokenMap();
		this.qwandaServiceUrl = qwandaServiceUrl;
		this.token = gennyToken.getToken();
		this.realm = gennyToken.getRealm();
		this.gennyToken = gennyToken;
		this.serviceToken = gennyToken; // override afterwards
		this.cacheUtil = new CacheUtils(qwandaServiceUrl, token, decodedMapToken, realm);
		this.cacheUtil.setBaseEntityUtils(this);
	}

	public BaseEntityUtils(String qwandaServiceUrl, String token, Map<String, Object> decodedMapToken, String realm) {
		this(qwandaServiceUrl, new GennyToken(token));
	}

	private String getRealm() {
		return gennyToken.getRealm();
	}

	/* =============== refactoring =============== */

	public BaseEntity create(final String uniqueCode, final String bePrefix, final String name) {

		String uniqueId = QwandaUtils.getUniqueId(bePrefix, uniqueCode);
		if (uniqueId != null) {
			return this.create(uniqueId, name);
		}

		return null;
	}

	public BaseEntity create(String baseEntityCode, String name) {

		BaseEntity newBaseEntity = null;
		if (VertxUtils.cachedEnabled == false) {
			newBaseEntity = QwandaUtils.createBaseEntityByCode(baseEntityCode, name, qwandaServiceUrl, this.token);
		} else {
			GennyToken gt = new GennyToken(this.token);
			newBaseEntity = new BaseEntity(baseEntityCode, name);
			newBaseEntity.setRealm(gt.getRealm());
		}
		this.addAttributes(newBaseEntity);
		VertxUtils.writeCachedJson(newBaseEntity.getRealm(), newBaseEntity.getCode(), JsonUtils.toJson(newBaseEntity),
				this.token);
		return newBaseEntity;
	}

	public List<BaseEntity> getBaseEntityFromSelectionAttribute(BaseEntity be, String attributeCode) {

		List<BaseEntity> bes = new CopyOnWriteArrayList<>();

		String attributeValue = be.getValue(attributeCode, null);
		if (attributeValue != null) {
			if (!attributeValue.isEmpty() && !attributeValue.equals(" ")) {
				/*
				 * first we try to serialise the attriute into a JsonArray in case this is a
				 * multi-selection attribute
				 */
				try {

					// JsonArray attributeValues = new JsonArray(attributeValue);

					JsonParser parser = new JsonParser();
					JsonElement tradeElement = parser.parse(attributeValue);
					JsonArray attributeValues = tradeElement.getAsJsonArray();

					/* we loop through the attribute values */
					for (int i = 0; i < attributeValues.size(); i++) {

						String beCode = attributeValues.get(i).getAsString();

						/* we try to fetch the base entity */
						if (beCode != null) {

							BaseEntity baseEntity = this.getBaseEntityByCode(beCode);
							if (baseEntity != null) {

								/* we add the base entity to the list */
								bes.add(baseEntity);
							}
						}
					}

					/* we return the list */
					return bes;

				} catch (Exception e) {
					/*
					 * serialisation did not work - we can assume this is a single selection
					 * attribute
					 */

					/* we fetch the BaseEntity */
					BaseEntity baseEntity = this.getBaseEntityByCode(attributeValue);

					/* we return a list containing only this base entity */
					if (baseEntity != null) {
						bes.add(baseEntity);
					}

					/* we return */
					return bes;
				}
			}
		}

		return bes;
	}

	/* ================================ */
	/* old code */

	public BaseEntity createRole(final String uniqueCode, final String name, String... capabilityCodes) {
		String code = "ROL_IS_" + uniqueCode.toUpperCase();
		log.info("Creating Role " + code + ":" + name);
		BaseEntity role = this.getBaseEntityByCode(code);
		if (role == null) {
			role = QwandaUtils.createBaseEntityByCode(code, name, qwandaServiceUrl, this.token);
			this.addAttributes(role);

			VertxUtils.writeCachedJson(role.getRealm(), role.getCode(), JsonUtils.toJson(role));
		}

		for (String capabilityCode : capabilityCodes) {
			Attribute capabilityAttribute = RulesUtils.attributeMap.get("CAP_" + capabilityCode);
			try {
				role.addAttribute(capabilityAttribute, 1.0, "TRUE");
			} catch (BadDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Now force the role to only have these capabilitys
		try {
			String result = QwandaUtils.apiPutEntity(qwandaServiceUrl + "/qwanda/baseentitys/force",
					JsonUtils.toJson(role), this.token);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return role;
	}

	public Object get(final String key) {
		return this.decodedMapToken.get(key);
	}

	public void set(final String key, Object value) {
		this.decodedMapToken.put(key, value);
	}

	public Attribute saveAttribute(Attribute attribute, final String token) throws IOException {

		RulesUtils.attributeMap.put(attribute.getCode(), attribute);
		try {
			if (!VertxUtils.cachedEnabled) { // only post if not in junit

				String result = QwandaUtils.apiPostEntity(this.qwandaServiceUrl + "/qwanda/attributes",
						JsonUtils.toJson(attribute), token);
			}
			return attribute;
		} catch (IOException e) {
			log.error("Socket error trying to post attribute");
			throw new IOException("Cannot save attribute");
		}

	}

	public void addAttributes(BaseEntity be) {

		if (be != null) {

			if (!(be.getCode().startsWith("SBE_") || be.getCode().startsWith("RAW_"))) { // don't bother with search be
				// or raw attributes
				for (EntityAttribute ea : be.getBaseEntityAttributes()) {
					if (ea != null) {
						Attribute attribute = RulesUtils.attributeMap.get(ea.getAttributeCode());
						if (attribute != null) {
							ea.setAttribute(attribute);
						} else {
							RulesUtils.loadAllAttributesIntoCache(this.token);
							attribute = RulesUtils.attributeMap.get(ea.getAttributeCode());
							if (attribute != null) {
								ea.setAttribute(attribute);
							} else {
								log.error("Cannot get Attribute - " + ea.getAttributeCode());

								Attribute dummy = new AttributeText(ea.getAttributeCode(), ea.getAttributeCode());
								ea.setAttribute(dummy);

							}
						}
					}
				}
			}
		}
	}

	public BaseEntity saveAnswer(Answer answer) {

		BaseEntity ret = addAnswer(answer);

		try {
			JsonObject json = new JsonObject(JsonUtils.toJson(answer));
			json.put("token", this.token);
			log.info("Saving answer");
			VertxUtils.eb.write("answer", json);
			log.info("Finished saving answer");
		} catch (NamingException e) {
			log.error("Error in saving answer through kafka :::: " + e.getMessage());
		}
		return ret;
	}

	public BaseEntity addAnswer(Answer answer) {

		return this.updateCachedBaseEntity(answer);
	}

	public <T extends BaseEntity> T updateBaseEntity(T be, Answer answer, Class clazz) {

		T be2 = this.updateCachedBaseEntity(answer, clazz);
		try {
			Attribute attr = RulesUtils.getAttribute(answer.getAttributeCode(), this.getGennyToken().getToken());
			be.setValue(attr, answer.getValue());
		} catch (BadDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return be2;
	}

	public BaseEntity updateBaseEntity(String baseEntityCode, Answer answer) {
		BaseEntity be = this.getBaseEntityByCode(baseEntityCode);
		return updateBaseEntity(be, answer, BaseEntity.class);
	}

	public <T extends BaseEntity> T updateBaseEntity(T be, Answer answer) {

		return updateBaseEntity(be, answer, BaseEntity.class);
	}

	public void saveAnswers(List<Answer> answers, final boolean changeEvent) {
		if (!((answers == null) || (answers.isEmpty()))) {

			if (!changeEvent) {
				for (Answer answer : answers) {
					answer.setChangeEvent(false);
				}
			}

			// Sort answers into target Baseentitys
			Map<String, List<Answer>> answersPerTargetCodeMap = answers.stream()
					.collect(Collectors.groupingBy(Answer::getTargetCode));

			for (String targetCode : answersPerTargetCodeMap.keySet()) {
				List<Answer> targetAnswers = answersPerTargetCodeMap.get(targetCode);
				Answer items[] = new Answer[targetAnswers.size()];
				items = targetAnswers.toArray(items);

				QDataAnswerMessage msg = new QDataAnswerMessage(items);
				this.updateCachedBaseEntity(targetAnswers);

				if (!VertxUtils.cachedEnabled) { // if not running junit, no need for api
					// String jsonAnswer = JsonUtils.toJson(msg);
					// jsonAnswer.replace("\\\"", "\"");

					if (!VertxUtils.cachedEnabled) { // only post if not in junit
//							QwandaUtils.apiPostEntity(this.qwandaServiceUrl + "/qwanda/answers/bulk", jsonAnswer,
//									token);
						for (Answer answer : targetAnswers) {
							try {
								JsonObject json = new JsonObject(JsonUtils.toJson(answer));
								json.put("token", this.token);
								log.info("Saving answer");
								VertxUtils.eb.write("answer", json);
								log.info("Finished saving answer");
							} catch (NamingException e) {
								log.error("Error in saving answer through kafka :::: " + e.getMessage());
							}

						}
					}
				} else {
					for (Answer answer : answers) {
						log.info("Saving Answer :" + answer);
					}
				}
			}
		}
	}

	public void saveAnswers(List<Answer> answers) {
		this.saveAnswers(answers, true);
	}

	public BaseEntity getOfferBaseEntity(String groupCode, String linkCode, String linkValue, String quoterCode,
			String prefix, String attributeCode) {
		return this.getOfferBaseEntity(groupCode, linkCode, linkValue, quoterCode, true, prefix, attributeCode);
	}

	public BaseEntity getOfferBaseEntity(String groupCode, String linkCode, String linkValue, String quoterCode,
			Boolean includeHidden, String prefix, String attributeCode) {

		/*
		 * TODO : Replace with searchEntity when it will be capable of filtering based
		 * on linkWeight
		 */
		List linkList = this.getLinkList(groupCode, linkCode, linkValue, this.token);
		String quoterCodeForOffer = null;

		if (linkList != null) {

			try {
				for (Object linkObj : linkList) {

					Link link = JsonUtils.fromJson(linkObj.toString(), Link.class);
					BaseEntity offerBe = null;
					if (!includeHidden) {
						if (link.getWeight() != 0) {
							offerBe = this.getBaseEntityByCode(link.getTargetCode());
						}
					} else {
						offerBe = this.getBaseEntityByCode(link.getTargetCode());
					}

					// BaseEntity offerBe = this.getBaseEntityByCode(link.getTargetCode());

					if (offerBe != null && offerBe.getCode().startsWith(prefix)) {

						quoterCodeForOffer = offerBe.getValue(attributeCode, null);

						if (quoterCode.equals(quoterCodeForOffer)) {
							return offerBe;
						}
					}
				}
			} catch (Exception e) {

			}
		}

		return null;
	}

	public void updateBaseEntityAttribute(final String sourceCode, final String beCode, final String attributeCode,
			final String newValue) {
		List<Answer> answers = new CopyOnWriteArrayList<Answer>();
		answers.add(new Answer(sourceCode, beCode, attributeCode, newValue));
		this.saveAnswers(answers);
	}

	public SearchEntity getSearchEntityByCode(final String code) {

		SearchEntity searchBe = null;
		try {
			JsonObject cachedJsonObject = VertxUtils.readCachedJson(this.realm, code, this.token);
			if (cachedJsonObject != null) {
				String data = cachedJsonObject.getString("value");
				log.info("json recieved :" + data);

				if (data != null) {
					searchBe = JsonUtils.fromJson(data, SearchEntity.class);
					log.info("searchEntity :: " + JsonUtils.toJson(searchBe));
				}
			}

		} catch (Exception e) {
			log.error("Failed to read cache for search " + code);
		}
		return searchBe;
	}

	public <T extends BaseEntity> T getBaseEntityByCode(final String code) {
		return this.getBaseEntityByCode(code, true);
	}

	public Optional<BaseEntity> getOptionalBaseEntityByCode(final String code) {
		return this.getOptionalBaseEntityByCode(code, true);
	}

	public <T extends BaseEntity> T getBaseEntityByCode(final String code, Boolean withAttributes, Class clazz) {

		return BaseEntityByCode(code, withAttributes, clazz, null);
	}

	public <T extends BaseEntity> T getFilteredBaseEntityByCode(final String code, final String questionCode) {
		String[] filteredStrings = null;
		String askMsgs2Str = (String) VertxUtils.cacheInterface.readCache(this.getRealm(), "FRM_" + code + "_ASKS",
				this.getGennyToken().getToken());

		if (askMsgs2Str != null) {
			// extract the 'PRI_ and LNKs
			Set<String> allMatches = new HashSet<String>();
			Matcher m = Pattern.compile("(PRI_\\[A-Z0-9\\_])").matcher(askMsgs2Str);
			while (m.find()) {
				allMatches.add(m.group());
			}

			filteredStrings = allMatches.toArray(new String[0]);
		}

		// ugly TODO HAC

		T be = BaseEntityByCode(code, true, BaseEntity.class, filteredStrings);

		for (EntityAttribute ea : be.getBaseEntityAttributes()) {

			if (ea.getAttributeCode().equals("PRI_VIDEO_URL")) {
				String value = ea.getValueString();
				if (value.startsWith("http")) {
					// normal video
				} else {
					log.info("My Interview");

					BaseEntity project = this
							.getBaseEntityByCode("PRJ_" + this.getGennyToken().getRealm().toUpperCase());
					String apiKey = project.getValueAsString("ENV_API_KEY_MY_INTERVIEW");
					String secretToken = project.getValueAsString("ENV_SECRET_MY_INTERVIEW");
					long unixTimestamp = Instant.now().getEpochSecond();
					String apiSecret = apiKey + secretToken + unixTimestamp;
					String hashed = BCrypt.hashpw(apiSecret, BCrypt.gensalt(10));
					String videoId = ea.getValueString();
					String url = "https://api.myinterview.com/2.21.2/getVideo?apiKey=" + apiKey + "&hashTimestamp="
							+ unixTimestamp + "&hash=" + hashed + "&video=" + videoId;
					String url2 = "https://embed.myinterview.com/player.v3.html?apiKey=" + apiKey + "&hashTimestamp="
							+ unixTimestamp + "&hash=" + hashed + "&video=" + videoId + "&autoplay=1&fs=0";

					log.info("MyInterview Hash is " + url);
					log.info("MyInterview Hash2 is " + url2);
					if ("PER_DOMENIC_AT_GADATECHNOLOGY_DOT_COM_DOT_AU".equals(code)) {
						ea.setValue("https://www.youtube.com/watch?v=dQw4w9WgXcQ"); // needed a demo video
					} else {
						ea.setValue(url2);
					}
				}
			}

		}

		return be;

	}

	public <T extends BaseEntity> T BaseEntityByCode(final String code, Boolean withAttributes, Class clazz,
			final String[] filterAttributes) {

		T be = null;

		if (code == null) {
			log.error("Cannot pass a null code");
			return null;
		}

		try {
			// log.info("Fetching BaseEntityByCode, code="+code);
			be = VertxUtils.readFromDDT(getRealm(), code, withAttributes, this.token, clazz);
			if (be == null) {
				if (!VertxUtils.cachedEnabled) { // because during junit it annoys me
					log.info("be (" + code + ") fetched is NULL ");
				}
			} else {
				this.addAttributes(be);
			}
		} catch (Exception e) {
			log.info("Failed to read cache for baseentity " + code);
		}

		if (filterAttributes != null) {
			BaseEntity user = this.getBaseEntityByCode(this.gennyToken.getUserCode());
			be = VertxUtils.privacyFilter(user, be, filterAttributes);
		}

		return be;
	}

	public <T extends BaseEntity> T getBaseEntityByCode(final String code, Boolean withAttributes) {

		return getBaseEntityByCode(code, withAttributes, BaseEntity.class);
	}

	public Optional<BaseEntity> getOptionalBaseEntityByCode(final String code, Boolean withAttributes) {

		Optional<BaseEntity> be = Optional.empty();

		try {
			// log.info("Fetching BaseEntityByCode, code="+code);
			be = Optional.ofNullable(VertxUtils.readFromDDT(getRealm(), code, withAttributes, this.token));
			if (be.isPresent()) {
				this.addAttributes(be.orElse(null));
			}
		} catch (Exception e) {
			log.info("Failed to read cache for baseentity " + code);
		}

		return be;
	}

	public BaseEntity getBaseEntityByAttributeAndValue(final String attributeCode, final String value) {

		BaseEntity be = null;
		be = RulesUtils.getBaseEntityByAttributeAndValue(this.qwandaServiceUrl, this.decodedMapToken, this.token,
				attributeCode, value);
		if (be != null) {
			this.addAttributes(be);
		}
		return be;
	}

	public List<BaseEntity> getBaseEntitysByAttributeAndValue(final String attributeCode, final String value) {

		List<BaseEntity> bes = null;
		bes = RulesUtils.getBaseEntitysByAttributeAndValue(this.qwandaServiceUrl, this.decodedMapToken, this.token,
				attributeCode, value);
		return bes;
	}

	public void clearBaseEntitysByParentAndLinkCode(final String parentCode, final String linkCode, Integer pageStart,
			Integer pageSize) {

		String key = parentCode + linkCode + "-" + pageStart + "-" + pageSize;
		VertxUtils.putObject(this.realm, "LIST", key, null);
	}

	public List<BaseEntity> getBaseEntitysByParentAndLinkCode(final String parentCode, final String linkCode,
			Integer pageStart, Integer pageSize, Boolean cache) {
		cache = false;
		List<BaseEntity> bes = new CopyOnWriteArrayList<BaseEntity>();
		String key = parentCode + linkCode + "-" + pageStart + "-" + pageSize;
		if (cache) {
			Type listType = new TypeToken<List<BaseEntity>>() {
			}.getType();
			List<String> beCodes = VertxUtils.getObject(this.realm, "LIST", key, (Class) listType);
			if (beCodes == null) {
				bes = RulesUtils.getBaseEntitysByParentAndLinkCodeWithAttributes(qwandaServiceUrl, this.decodedMapToken,
						this.token, parentCode, linkCode, pageStart, pageSize);
				beCodes = new CopyOnWriteArrayList<String>();
				for (BaseEntity be : bes) {
					VertxUtils.putObject(this.realm, "", be.getCode(), JsonUtils.toJson(be));
					beCodes.add(be.getCode());
				}
				VertxUtils.putObject(this.realm, "LIST", key, beCodes);
			} else {
				for (String beCode : beCodes) {
					BaseEntity be = getBaseEntityByCode(beCode);
					bes.add(be);
				}
			}
		} else {

			bes = RulesUtils.getBaseEntitysByParentAndLinkCodeWithAttributes(qwandaServiceUrl, this.decodedMapToken,
					this.token, parentCode, linkCode, pageStart, pageSize);
		}

		return bes;
	}

	/* added because of the bug */
	public List<BaseEntity> getBaseEntitysByParentAndLinkCode2(final String parentCode, final String linkCode,
			Integer pageStart, Integer pageSize, Boolean cache) {

		List<BaseEntity> bes = null;

		// if (isNull("BES_" + parentCode.toUpperCase() + "_" + linkCode)) {

		bes = RulesUtils.getBaseEntitysByParentAndLinkCodeWithAttributes2(qwandaServiceUrl, this.decodedMapToken,
				this.token, parentCode, linkCode, pageStart, pageSize);

		// } else {
		// bes = getAsBaseEntitys("BES_" + parentCode.toUpperCase() + "_" + linkCode);
		// }

		return bes;
	}

	// Adam's speedup
	public List<BaseEntity> getBaseEntitysByParentAndLinkCode3(final String parentCode, final String linkCode,
			Integer pageStart, Integer pageSize, Boolean cache) {
		cache = false;
		List<BaseEntity> bes = new CopyOnWriteArrayList<BaseEntity>();

		BaseEntity parent = getBaseEntityByCode(parentCode);
		for (EntityEntity ee : parent.getLinks()) {
			if (ee.getLink().getAttributeCode().equalsIgnoreCase(linkCode)) {
				BaseEntity child = getBaseEntityByCode(ee.getLink().getTargetCode());

				bes.add(child);
			}
		}
		return bes;
	}

	public List<BaseEntity> getBaseEntitysByParentLinkCodeAndLinkValue(final String parentCode, final String linkCode,
			final String linkValue, Integer pageStart, Integer pageSize, Boolean cache) {

		List<BaseEntity> bes = null;

		bes = RulesUtils.getBaseEntitysByParentAndLinkCodeAndLinkValueWithAttributes(qwandaServiceUrl,
				this.decodedMapToken, this.token, parentCode, linkCode, linkValue, pageStart, pageSize);
		return bes;
	}

	public List<BaseEntity> getBaseEntitysByParentAndLinkCode(final String parentCode, final String linkCode,
			Integer pageStart, Integer pageSize, Boolean cache, final String stakeholderCode) {
		List<BaseEntity> bes = null;

		bes = RulesUtils.getBaseEntitysByParentAndLinkCodeWithAttributesAndStakeholderCode(qwandaServiceUrl,
				this.decodedMapToken, this.token, parentCode, linkCode, stakeholderCode);
		if (cache) {
			set("BES_" + parentCode.toUpperCase() + "_" + linkCode, bes); // WATCH THIS!!!
		}

		return bes;
	}

	public String moveBaseEntity(String baseEntityCode, String sourceCode, String targetCode) {
		return this.moveBaseEntity(baseEntityCode, sourceCode, targetCode, "LNK_CORE", "LINK");
	}

	public String moveBaseEntity(String baseEntityCode, String sourceCode, String targetCode, String linkCode) {
		return this.moveBaseEntity(baseEntityCode, sourceCode, targetCode, linkCode, "LINK");
	}

	public String moveBaseEntity(String baseEntityCode, String sourceCode, String targetCode, String linkCode,
			final String linkValue) {

		Link link = new Link(sourceCode, baseEntityCode, linkCode, linkValue);

		try {

			/* we call the api */
			QwandaUtils.apiPostEntity(qwandaServiceUrl + "/qwanda/baseentitys/move/" + targetCode,
					JsonUtils.toJson(link), this.token);
		} catch (IOException e) {
			log.error(e.getMessage());
		}

		return null;
	}

	/**
	 * Get the BaseEntity that is linked with a specific attribute. Generally this
	 * will be a LNK attribute, although it doesn't have to be.
	 *
	 * @param baseEntityCode The targeted BaseEntity Code
	 * @param attributeCode  The attribute storing the data
	 * @return The baseEntity with code stored in the attribute
	 */
	public BaseEntity getBaseEntityFromLNKAttr(String baseEntityCode, String attributeCode) {

		BaseEntity be = getBaseEntityByCode(baseEntityCode);
		return getBaseEntityFromLNKAttr(be, attributeCode);
	}

	/**
	 * Get the BaseEntity that is linked with a specific attribute.
	 *
	 * @param baseEntity    The targeted BaseEntity
	 * @param attributeCode The attribute storing the data
	 * @return The baseEntity with code stored in the attribute
	 */
	public BaseEntity getBaseEntityFromLNKAttr(BaseEntity baseEntity, String attributeCode) {

		String newBaseEntityCode = getBaseEntityCodeFromLNKAttr(baseEntity, attributeCode);
		if (newBaseEntityCode == null) {
			return null;
		}
		BaseEntity newBe = getBaseEntityByCode(newBaseEntityCode);
		return newBe;
	}

	/**
	 * Get the code of the BaseEntity that is linked with a specific attribute.
	 *
	 * @param baseEntityCode The targeted BaseEntity Code
	 * @param attributeCode  The attribute storing the data
	 * @return The baseEntity code stored in the attribute
	 */
	public String getBaseEntityCodeFromLNKAttr(String baseEntityCode, String attributeCode) {

		BaseEntity be = getBaseEntityByCode(baseEntityCode);
		return getBaseEntityCodeFromLNKAttr(be, attributeCode);
	}

	/**
	 * Get the code of the BaseEntity that is linked with a specific attribute.
	 *
	 * @param baseEntity    The targeted BaseEntity
	 * @param attributeCode The attribute storing the data
	 * @return The baseEntity code stored in the attribute
	 */
	public String getBaseEntityCodeFromLNKAttr(BaseEntity baseEntity, String attributeCode) {

		String attributeValue = baseEntity.getValue(attributeCode, null);
		if (attributeValue == null) {
			return null;
		}
		String newBaseEntityCode = cleanUpAttributeValue(attributeValue);
		return newBaseEntityCode;
	}

	/**
	 * Get an ArrayList of BaseEntity codes that are linked with a specific
	 * attribute.
	 *
	 * @param baseEntityCode The targeted BaseEntity Code
	 * @param attributeCode  The attribute storing the data
	 * @return An ArrayList of codes stored in the attribute
	 */
	public List<String> getBaseEntityCodeArrayFromLNKAttr(String baseEntityCode, String attributeCode) {

		BaseEntity be = getBaseEntityByCode(baseEntityCode);
		return getBaseEntityCodeArrayFromLNKAttr(be, attributeCode);
	}

	/**
	 * Get an ArrayList of BaseEntity codes that are linked with a specific
	 * attribute.
	 *
	 * @param baseEntity    The targeted BaseEntity
	 * @param attributeCode The attribute storing the data
	 * @return An ArrayList of codes stored in the attribute
	 */
	public List<String> getBaseEntityCodeArrayFromLNKAttr(BaseEntity baseEntity, String attributeCode) {

		String attributeValue = getBaseEntityCodeFromLNKAttr(baseEntity, attributeCode);
		if (attributeValue == null) {
			return null;
		}

		String[] baseEntityCodeArray = attributeValue.split(",");
		List<String> beCodeList = Arrays.asList(baseEntityCodeArray);
		return beCodeList;
	}

	/**
	 * Classic Genny style string clean up. Hope this makes our code look a little
	 * nicer :)
	 *
	 * @param value The value to clean
	 * @return A clean string
	 */
	public String cleanUpAttributeValue(String value) {
		String cleanCode = value.replace("\"", "").replace("[", "").replace("]", "").replace(" ", "");
		return cleanCode;
	}

	public Object getBaseEntityValue(final String baseEntityCode, final String attributeCode) {
		BaseEntity be = getBaseEntityByCode(baseEntityCode);
		Optional<EntityAttribute> ea = be.findEntityAttribute(attributeCode);
		if (ea.isPresent()) {
			return ea.get().getObject();
		} else {
			return null;
		}
	}

	public static String getBaseEntityAttrValueAsString(BaseEntity be, String attributeCode) {

		String attributeVal = null;
		for (EntityAttribute ea : be.getBaseEntityAttributes()) {
			try {
				if (ea.getAttributeCode().equals(attributeCode)) {
					attributeVal = ea.getObjectAsString();
				}
			} catch (Exception e) {
			}
		}

		return attributeVal;
	}

	public String getBaseEntityValueAsString(final String baseEntityCode, final String attributeCode) {

		String attrValue = null;

		if (baseEntityCode != null) {

			BaseEntity be = getBaseEntityByCode(baseEntityCode);
			attrValue = getBaseEntityAttrValueAsString(be, attributeCode);
		}

		return attrValue;
	}

	public LocalDateTime getBaseEntityValueAsLocalDateTime(final String baseEntityCode, final String attributeCode) {
		BaseEntity be = getBaseEntityByCode(baseEntityCode);
		Optional<EntityAttribute> ea = be.findEntityAttribute(attributeCode);
		if (ea.isPresent()) {
			return ea.get().getValueDateTime();
		} else {
			return null;
		}
	}

	public LocalDate getBaseEntityValueAsLocalDate(final String baseEntityCode, final String attributeCode) {
		BaseEntity be = getBaseEntityByCode(baseEntityCode);
		Optional<EntityAttribute> ea = be.findEntityAttribute(attributeCode);
		if (ea.isPresent()) {
			return ea.get().getValueDate();
		} else {
			return null;
		}
	}

	public LocalTime getBaseEntityValueAsLocalTime(final String baseEntityCode, final String attributeCode) {

		BaseEntity be = getBaseEntityByCode(baseEntityCode);
		Optional<EntityAttribute> ea = be.findEntityAttribute(attributeCode);
		if (ea.isPresent()) {
			return ea.get().getValueTime();
		} else {
			return null;
		}
	}

	public BaseEntity getParent(String targetCode, String linkCode) {
		return this.getParent(targetCode, linkCode, null);
	}

	public BaseEntity getParent(String targetCode, String linkCode, String linkValue) {

		List<BaseEntity> parents = this.getParents(targetCode, linkCode, linkValue);
		if (parents != null && !parents.isEmpty()) {
			return parents.get(0);
		}

		return null;
	}

	public List<BaseEntity> getParents(final String targetCode) {
		return this.getParents(targetCode, null, null);
	}

	public List<BaseEntity> getParents(final String targetCode, final String linkCode) {
		return this.getParents(targetCode, linkCode, null);
	}

	public List<BaseEntity> getParents(String targetCode, String linkCode, String linkValue) {

		List<BaseEntity> parents = null;
		parents = new CopyOnWriteArrayList<BaseEntity>();

		List<Link> arrayList = getParentLinks(targetCode, linkCode, linkValue);

		for (Link lnk : arrayList) {

			BaseEntity linkedBe = getBaseEntityByCode(lnk.getSourceCode());
			if (linkedBe != null) {
				parents.add(linkedBe);
			}
		}
		return parents;

	}

	public List<Link> getParentLinks(String targetCode, String linkCode, String linkValue) {

		long sTime = System.nanoTime();
		List<Link> arrayList = new ArrayList<>();
		try {
			String beJson = null;
			if (linkCode == null && linkValue == null)
				beJson = QwandaUtils.apiGet(this.qwandaServiceUrl + "/qwanda/entityentitys/" + targetCode + "/parents",
						this.token);
			else if (linkValue == null) {
				beJson = QwandaUtils.apiGet(this.qwandaServiceUrl + "/qwanda/entityentitys/" + targetCode
						+ "/linkcodes/" + linkCode + "/parents", this.token);
			} else {
				beJson = QwandaUtils.apiGet(this.qwandaServiceUrl + "/qwanda/entityentitys/" + targetCode
						+ "/linkcodes/" + linkCode + "/parents/" + linkValue, this.token);
			}
			Link[] linkArray = JsonUtils.fromJson(beJson, Link[].class);
			if (linkArray != null && linkArray.length > 0) {
				arrayList = new CopyOnWriteArrayList<Link>(Arrays.asList(linkArray));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		double difference = (System.nanoTime() - sTime) / 1e6; // get ms
		return arrayList;
	}

	public List<EntityEntity> getLinks(BaseEntity be) {
		return this.getLinks(be.getCode());
	}

	public List<EntityEntity> getLinks(String beCode) {

		List<EntityEntity> links = new CopyOnWriteArrayList<EntityEntity>();
		BaseEntity be = this.getBaseEntityByCode(beCode);
		if (be != null) {

			Set<EntityEntity> linkSet = be.getLinks();
			links.addAll(linkSet);
		}

		return links;
	}

	public BaseEntity getLinkedBaseEntity(String beCode, String linkCode, String linkValue) {

		List<BaseEntity> bes = this.getLinkedBaseEntities(beCode, linkCode, linkValue);
		if (bes != null && bes.size() > 0) {
			return bes.get(0);
		}

		return null;
	}

	public List<BaseEntity> getLinkedBaseEntities(BaseEntity be) {
		return this.getLinkedBaseEntities(be.getCode(), null, null);
	}

	public List<BaseEntity> getLinkedBaseEntities(String beCode) {
		return this.getLinkedBaseEntities(beCode, null, null);
	}

	public List<BaseEntity> getLinkedBaseEntities(BaseEntity be, String linkCode) {
		return this.getLinkedBaseEntities(be.getCode(), linkCode, null);
	}

	public List<BaseEntity> getLinkedBaseEntities(String beCode, String linkCode) {
		return this.getLinkedBaseEntities(beCode, linkCode, null);
	}

	public List<BaseEntity> getLinkedBaseEntities(BaseEntity be, String linkCode, String linkValue) {
		return this.getLinkedBaseEntities(be.getCode(), linkCode, linkValue);
	}

	public List<BaseEntity> getLinkedBaseEntities(String beCode, String linkCode, String linkValue) {
		return this.getLinkedBaseEntities(beCode, linkCode, linkValue, 1);
	}

	public List<BaseEntity> getLinkedBaseEntities(String beCode, String linkCode, String linkValue, Integer level) {

		List<BaseEntity> linkedBaseEntities = new CopyOnWriteArrayList<BaseEntity>();
		try {

			/* We grab all the links from the node passed as a parameter "beCode" */
			List<EntityEntity> links = this.getLinks(beCode);

			/* We loop through all the links */
			for (EntityEntity link : links) {

				if (link != null && link.getLink() != null) {

					Link entityLink = link.getLink();

					/* We get the targetCode */
					String targetCode = entityLink.getTargetCode();
					if (targetCode != null) {

						/* We use the targetCode to get the base entity */
						BaseEntity targetBe = this.getBaseEntityByCode(targetCode);
						if (targetBe != null) {

							/* If a linkCode is passed we filter using its value */
							if (linkCode != null) {
								if (entityLink.getAttributeCode() != null
										&& entityLink.getAttributeCode().equals(linkCode)) {

									/* If a linkValue is passed we filter using its value */
									if (linkValue != null) {
										if (entityLink.getLinkValue() != null
												&& entityLink.getLinkValue().equals(linkValue)) {
											linkedBaseEntities.add(targetBe);
										}
									} else {

										/* If no link value was provided we just pass the base entity */
										linkedBaseEntities.add(targetBe);
									}
								}
							} else {

								/* If not linkCode was provided we just pass the base entity */
								linkedBaseEntities.add(targetBe);
							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

		if (level > 1) {

			List<List<BaseEntity>> nextLevels = linkedBaseEntities.stream()
					.map(item -> this.getLinkedBaseEntities(item.getCode(), linkCode, linkValue, (level - 1)))
					.collect(Collectors.toList());
			for (List<BaseEntity> nextLevel : nextLevels) {
				linkedBaseEntities.addAll(nextLevel);
			}
		}

		return linkedBaseEntities;
	}

	public List<BaseEntity> getBaseEntityWithChildren(String beCode, Integer level) {

		if (level == 0) {
			return null; // exit point;
		}

		level--;
		BaseEntity be = this.getBaseEntityByCode(beCode);
		if (be != null) {

			List<BaseEntity> beList = new CopyOnWriteArrayList<>();

			Set<EntityEntity> entityEntities = be.getLinks();

			// we interate through the links
			for (EntityEntity entityEntity : entityEntities) {

				Link link = entityEntity.getLink();
				if (link != null) {

					// we get the target BE
					String targetCode = link.getTargetCode();
					if (targetCode != null) {

						// recursion
						beList.addAll(this.getBaseEntityWithChildren(targetCode, level));
					}
				}
			}

			return beList;
		}

		return null;
	}

	public Boolean checkIfLinkExists(String parentCode, String linkCode, String childCode) {

		Boolean isLinkExists = false;
		QDataBaseEntityMessage dataBEMessage = QwandaUtils.getDataBEMessage(parentCode, linkCode, this.token);

		if (dataBEMessage != null) {
			BaseEntity[] beArr = dataBEMessage.getItems();

			if (beArr.length > 0) {
				for (BaseEntity be : beArr) {
					if (be.getCode().equals(childCode)) {
						isLinkExists = true;
						return isLinkExists;
					}
				}
			} else {
				isLinkExists = false;
				return isLinkExists;
			}

		}
		return isLinkExists;
	}

	/* Check If Link Exists and Available */
	public Boolean checkIfLinkExistsAndAvailable(String parentCode, String linkCode, String linkValue,
			String childCode) {
		Boolean isLinkExists = false;
		List<Link> links = getLinks(parentCode, linkCode);
		if (links != null) {
			for (Link link : links) {
				String linkVal = link.getLinkValue();

				if (linkVal != null && linkVal.equals(linkValue) && link.getTargetCode().equalsIgnoreCase(childCode)) {
					Double linkWeight = link.getWeight();
					if (linkWeight >= 1.0) {
						isLinkExists = true;
						return isLinkExists;
					}
				}
			}
		}
		return isLinkExists;
	}

	/* returns a duplicated BaseEntity from an existing beCode */
	public BaseEntity duplicateBaseEntityAttributesAndLinks(final BaseEntity oldBe, final String bePrefix,
			final String name) {

		BaseEntity newBe = this.create(oldBe.getCode(), bePrefix, name);
		duplicateAttributes(oldBe, newBe);
		duplicateLinks(oldBe, newBe);
		return getBaseEntityByCode(newBe.getCode());
	}

	public BaseEntity duplicateBaseEntityAttributes(final BaseEntity oldBe, final String bePrefix, final String name) {

		BaseEntity newBe = this.create(oldBe.getCode(), bePrefix, name);
		duplicateAttributes(oldBe, newBe);
		return getBaseEntityByCode(newBe.getCode());
	}

	public BaseEntity duplicateBaseEntityLinks(final BaseEntity oldBe, final String bePrefix, final String name) {

		BaseEntity newBe = this.create(oldBe.getCode(), bePrefix, name);
		duplicateLinks(oldBe, newBe);
		return getBaseEntityByCode(newBe.getCode());
	}

	public void duplicateAttributes(final BaseEntity oldBe, final BaseEntity newBe) {

		List<Answer> duplicateAnswerList = new CopyOnWriteArrayList<>();

		for (EntityAttribute ea : oldBe.getBaseEntityAttributes()) {
			duplicateAnswerList
					.add(new Answer(newBe.getCode(), newBe.getCode(), ea.getAttributeCode(), ea.getAsString()));
		}

		this.saveAnswers(duplicateAnswerList);
	}

	public void duplicateLinks(final BaseEntity oldBe, final BaseEntity newBe) {
		for (EntityEntity ee : oldBe.getLinks()) {
			createLink(newBe.getCode(), ee.getLink().getTargetCode(), ee.getLink().getAttributeCode(),
					ee.getLink().getLinkValue(), ee.getLink().getWeight());
		}
	}

	public void duplicateLink(final BaseEntity oldBe, final BaseEntity newBe, final BaseEntity childBe) {
		for (EntityEntity ee : oldBe.getLinks()) {
			if (ee.getLink().getTargetCode() == childBe.getCode()) {

				createLink(newBe.getCode(), ee.getLink().getTargetCode(), ee.getLink().getAttributeCode(),
						ee.getLink().getLinkValue(), ee.getLink().getWeight());
				break;
			}
		}
	}

	public void duplicateLinksExceptOne(final BaseEntity oldBe, final BaseEntity newBe, String linkValue) {
		for (EntityEntity ee : oldBe.getLinks()) {
			if (ee.getLink().getLinkValue() == linkValue) {
				continue;
			}
			createLink(newBe.getCode(), ee.getLink().getTargetCode(), ee.getLink().getAttributeCode(),
					ee.getLink().getLinkValue(), ee.getLink().getWeight());
		}
	}

	public BaseEntity cloneBeg(final BaseEntity oldBe, final BaseEntity newBe, final BaseEntity childBe,
			String linkValue) {
		duplicateLinksExceptOne(oldBe, newBe, linkValue);
		duplicateLink(oldBe, newBe, childBe);
		return getBaseEntityByCode(newBe.getCode());
	}

	/* clones links of oldBe to newBe from supplied arraylist linkValues */
	public BaseEntity copyLinks(final BaseEntity oldBe, final BaseEntity newBe, final String[] linkValues) {
		log.info("linkvalues   ::   " + Arrays.toString(linkValues));
		for (EntityEntity ee : oldBe.getLinks()) {
			log.info("old be linkValue   ::   " + ee.getLink().getLinkValue());
			for (String linkValue : linkValues) {
				log.info("a linkvalue   ::   " + linkValue);
				if (ee.getLink().getLinkValue().equals(linkValue)) {
					createLink(newBe.getCode(), ee.getLink().getTargetCode(), ee.getLink().getAttributeCode(),
							ee.getLink().getLinkValue(), ee.getLink().getWeight());
					log.info("creating link for   ::   " + linkValue);
				}
			}
		}
		return getBaseEntityByCode(newBe.getCode());
	}

	/*
	 * clones all links of oldBe to newBe except the linkValues supplied in
	 * arraylist linkValues
	 */
	public BaseEntity copyLinksExcept(final BaseEntity oldBe, final BaseEntity newBe, final String[] linkValues) {
		log.info("linkvalues   ::   " + Arrays.toString(linkValues));
		for (EntityEntity ee : oldBe.getLinks()) {
			log.info("old be linkValue   ::   " + ee.getLink().getLinkValue());
			for (String linkValue : linkValues) {
				log.info("a linkvalue   ::   " + linkValue);
				if (ee.getLink().getLinkValue().equals(linkValue)) {
					continue;
				}
				createLink(newBe.getCode(), ee.getLink().getTargetCode(), ee.getLink().getAttributeCode(),
						ee.getLink().getLinkValue(), ee.getLink().getWeight());
				log.info("creating link for   ::   " + linkValue);
			}
		}
		return getBaseEntityByCode(newBe.getCode());
	}

	public void updateBaseEntityStatus(BaseEntity be, String status) {
		this.updateBaseEntityStatus(be.getCode(), status);
	}

	public void updateBaseEntityStatus(String beCode, String status) {

		String attributeCode = "STA_STATUS";
		this.updateBaseEntityAttribute(beCode, beCode, attributeCode, status);
	}

	public void updateBaseEntityStatus(BaseEntity be, String userCode, String status) {
		this.updateBaseEntityStatus(be.getCode(), userCode, status);
	}

	public void updateBaseEntityStatus(String beCode, String userCode, String status) {

		String attributeCode = "STA_" + userCode;
		this.updateBaseEntityAttribute(userCode, beCode, attributeCode, status);

		/* new status for v3 */
		switch (status) {
		case "green":
		case "red":
		case "orange":
		case "yellow": {

			BaseEntity be = this.getBaseEntityByCode(beCode);
			if (be != null) {

				String attributeCodeStatus = "STA_" + status.toUpperCase();
				String existingValueArray = be.getValue(attributeCodeStatus, "[]");
				JsonParser jsonParser = new JsonParser();
				JsonArray existingValues = jsonParser.parse(existingValueArray).getAsJsonArray();
				existingValues.add(userCode);
				this.saveAnswer(new Answer(beCode, beCode, attributeCodeStatus, JsonUtils.toJson(existingValues)));
			}
		}
		default: {
		}
		}
	}

	public void updateBaseEntityStatus(BaseEntity be, List<String> userCodes, String status) {
		this.updateBaseEntityStatus(be.getCode(), userCodes, status);
	}

	public void updateBaseEntityStatus(String beCode, List<String> userCodes, String status) {

		for (String userCode : userCodes) {
			this.updateBaseEntityStatus(beCode, userCode, status);
		}
	}

	public List<Link> getLinks(final String parentCode, final String linkCode) {
		List<Link> links = RulesUtils.getLinks(this.qwandaServiceUrl, this.decodedMapToken, this.token, parentCode,
				linkCode);
		return links;
	}

	public String updateBaseEntity(BaseEntity be) {
		try {
			VertxUtils.writeCachedJson(getRealm(), be.getCode(), JsonUtils.toJson(be));
			return QwandaUtils.apiPutEntity(this.qwandaServiceUrl + "/qwanda/baseentitys", JsonUtils.toJson(be),
					this.token);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String saveSearchEntity(SearchEntity se) { // TODO: Ugly
		String ret = null;
		try {
			if (se != null) {
				if (!se.hasCode()) {
					log.error("ERROR! searchEntity se has no code!");
				}
				if (se.getId() == null) {
					BaseEntity existing = VertxUtils.readFromDDT(getRealm(), se.getCode(), this.token);
					if (existing != null) {
						se.setId(existing.getId());
					}
				}
				VertxUtils.writeCachedJson(getRealm(), se.getCode(), JsonUtils.toJson(se));
				if (se.getId() != null) {
					ret = QwandaUtils.apiPutEntity(this.qwandaServiceUrl + "/qwanda/baseentitys", JsonUtils.toJson(se),
							this.token);

				} else {
					ret = QwandaUtils.apiPostEntity(this.qwandaServiceUrl + "/qwanda/baseentitys", JsonUtils.toJson(se),
							this.token);
				}
				saveBaseEntityAttributes(se);
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
		return ret;
	}

	public String saveBaseEntity(BaseEntity be) { // TODO: Ugly
		String ret = null;
		try {
			if (be != null) {
				if (!be.hasCode()) {
					log.error("ERROR! BaseEntity se has no code!");
				}
				if (be.getId() == null) {
					BaseEntity existing = VertxUtils.readFromDDT(getRealm(), be.getCode(), this.token);
					if (existing != null) {
						be.setId(existing.getId());
					}
				}
				VertxUtils.writeCachedJson(getRealm(), be.getCode(), JsonUtils.toJson(be));
				if (be.getId() != null) {
					ret = QwandaUtils.apiPutEntity(this.qwandaServiceUrl + "/qwanda/baseentitys", JsonUtils.toJson(be),
							this.token);

				} else {
					ret = QwandaUtils.apiPostEntity(this.qwandaServiceUrl + "/qwanda/baseentitys", JsonUtils.toJson(be),
							this.token);
				}
				saveBaseEntityAttributes(be);
			}
		} catch (Exception e) {
			e.printStackTrace();

		}
		return ret;
	}

	public void saveBaseEntityAttributes(BaseEntity be) {
		if ((be == null) || (be.getCode() == null)) {
			throw new NullPointerException("Cannot save be because be is null or be.getCode is null");
		}
		List<Answer> answers = new CopyOnWriteArrayList<Answer>();

		for (EntityAttribute ea : be.getBaseEntityAttributes()) {
			Answer attributeAnswer = new Answer(be.getCode(), be.getCode(), ea.getAttributeCode(), ea.getAsString());
			attributeAnswer.setChangeEvent(false);
			answers.add(attributeAnswer);
		}
		this.saveAnswers(answers);
	}

	public <T extends BaseEntity> T updateCachedBaseEntity(final Answer answer, Class clazz) {
		T cachedBe = this.getBaseEntityByCode(answer.getTargetCode(), true, clazz);
		// Add an attribute if not already there
		try {
			String attributeCode = answer.getAttributeCode();
			if (!attributeCode.startsWith("RAW_")) {
				Attribute attribute = answer.getAttribute();

				if (RulesUtils.attributeMap != null) {
					if (RulesUtils.attributeMap.isEmpty()) {
						RulesUtils.loadAllAttributesIntoCache(token);
					}
					attribute = RulesUtils.attributeMap.get(attributeCode);

					if (attribute != null) {
						answer.setAttribute(attribute);
					}

					if (answer.getAttribute() == null || cachedBe == null) {
						log.info("Null Attribute or null BE , targetCode=[" + answer.getTargetCode() + "]");
					} else {
						cachedBe.addAnswer(answer);
					}
					VertxUtils.writeCachedJson(getRealm(), answer.getTargetCode(), JsonUtils.toJson(cachedBe),
							this.token);
				}
			}

			/*
			 * answer.setAttribute(RulesUtils.attributeMap.get(answer.getAttributeCode()));
			 * if (answer.getAttribute() == null) { log.info("Null Attribute"); } else
			 * cachedBe.addAnswer(answer);
			 * VertxUtils.writeCachedJson(answer.getTargetCode(),
			 * JsonUtils.toJson(cachedBe));
			 *
			 */
		} catch (BadDataException e) {
			e.printStackTrace();
		}
		return cachedBe;
	}

	public <T extends BaseEntity> T updateCachedBaseEntity(final Answer answer) {
		return updateCachedBaseEntity(answer, BaseEntity.class);
	}

	public BaseEntity updateCachedBaseEntity(List<Answer> answers) {
		Answer firstanswer = null;
		if ((answers != null) && (!answers.isEmpty())) {
			firstanswer = answers.get(0);
		} else {
			throw new NullPointerException("Answers cannot be null or empty for updateCacheBaseEntity");
		}
		BaseEntity cachedBe = null;

		if (firstanswer != null) {
			if (firstanswer.getTargetCode() == null) {
				throw new NullPointerException("firstanswer getTargetCode cannot be null for updateCacheBaseEntity");
			}
			// log.info("firstAnswer.targetCode=" + firstanswer.getTargetCode());
			cachedBe = this.getBaseEntityByCode(firstanswer.getTargetCode());
		} else {
			return null;
		}

		if (cachedBe != null) {
			if ((cachedBe == null) || (cachedBe.getCode() == null)) {
				throw new NullPointerException("cachedBe.getCode cannot be null for updateCacheBaseEntity , targetCode="
						+ firstanswer.getTargetCode() + " cacheBe=[" + cachedBe);
			}
		} else {
			throw new NullPointerException(
					"cachedBe cannot be null for updateCacheBaseEntity , targetCode=" + firstanswer.getTargetCode());

		}

		for (Answer answer : answers) {

			if (!answer.getAttributeCode().startsWith("RAW_")) {

				// Add an attribute if not already there
				try {
					if (answer.getAttribute() == null) {
						Attribute attribute = RulesUtils.getAttribute(answer.getAttributeCode(), token);

						if (attribute != null) {
							answer.setAttribute(attribute);
						}
					}
					if (answer.getAttribute() == null) {
						continue;
					}
					cachedBe.addAnswer(answer);
				} catch (BadDataException e) {
					e.printStackTrace();
				}
			}
		}

		VertxUtils.writeCachedJson(getRealm(), cachedBe.getCode(), JsonUtils.toJson(cachedBe), this.token);

		return cachedBe;
	}

	public Link createLink(String sourceCode, String targetCode, String linkCode, String linkValue, Double weight) {

		System.out.println(
				"CREATING LINK between " + sourceCode + " and " + targetCode + " with LINK VALUE = " + linkValue);
		Link link = new Link(sourceCode, targetCode, linkCode, linkValue);
		link.setWeight(weight);
		try {
			BaseEntity source = this.getBaseEntityByCode(sourceCode);
			BaseEntity target = this.getBaseEntityByCode(targetCode);
			Attribute linkAttribute = new AttributeLink(linkCode, linkValue);
			try {
				source.addTarget(target, linkAttribute, weight, linkValue);
				this.updateBaseEntity(source);

				QwandaUtils.apiPostEntity(qwandaServiceUrl + "/qwanda/entityentitys", JsonUtils.toJson(link),
						this.token);
			} catch (BadDataException e) {
				e.printStackTrace();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return link;
	}

	public Link updateLink(String sourceCode, String targetCode, String linkCode, String linkValue, Double weight) {

		System.out
				.println("UPDATING LINK between " + sourceCode + "and" + targetCode + "with LINK VALUE = " + linkValue);
		Link link = new Link(sourceCode, targetCode, linkCode, linkValue);
		link.setWeight(weight);
		try {
			BaseEntity source = this.getBaseEntityByCode(sourceCode);
			BaseEntity target = this.getBaseEntityByCode(targetCode);
			Attribute linkAttribute = new AttributeLink(linkCode, linkValue);
			source.addTarget(target, linkAttribute, weight, linkValue);

			this.updateBaseEntity(source);
			QwandaUtils.apiPutEntity(qwandaServiceUrl + "/qwanda/links", JsonUtils.toJson(link), this.token);
		} catch (IOException | BadDataException e) {
			e.printStackTrace();
		}
		return link;
	}

	public Link updateLink(String groupCode, String targetCode, String linkCode, Double weight) {

		log.info("UPDATING LINK between " + groupCode + "and" + targetCode);
		Link link = new Link(groupCode, targetCode, linkCode);
		link.setWeight(weight);
		try {
			QwandaUtils.apiPutEntity(qwandaServiceUrl + "/qwanda/links", JsonUtils.toJson(link), this.token);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return link;
	}

	public Link[] getUpdatedLink(String parentCode, String linkCode) {
		List<Link> links = this.getLinks(parentCode, linkCode);
		Link[] items = new Link[links.size()];
		items = (Link[]) links.toArray(items);
		return items;
	}

	/*
	 * Gets all the attribute and their value for the given basenentity code
	 */
	public Map<String, String> getMapOfAllAttributesValuesForBaseEntity(String beCode) {

		BaseEntity be = this.getBaseEntityByCode(beCode);
		log.info("The load is ::" + be);
		Set<EntityAttribute> eaSet = be.getBaseEntityAttributes();
		log.info("The set of attributes are  :: " + eaSet);
		Map<String, String> attributeValueMap = new HashMap<String, String>();
		for (EntityAttribute ea : eaSet) {
			String attributeCode = ea.getAttributeCode();
			log.info("The attribute code  is  :: " + attributeCode);
			String value = ea.getAsLoopString();
			attributeValueMap.put(attributeCode, value);
		}

		return attributeValueMap;
	}

	public List getLinkList(String groupCode, String linkCode, String linkValue, String token) {

		// String qwandaServiceUrl = "http://localhost:8280";
		List linkList = null;

		try {
			String attributeString = QwandaUtils.apiGet(GennySettings.qwandaServiceUrl + "/qwanda/entityentitys/"
					+ groupCode + "/linkcodes/" + linkCode + "/children/" + linkValue, token);
			if (attributeString != null) {
				linkList = JsonUtils.fromJson(attributeString, List.class);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return linkList;

	}

	/*
	 * Returns only non-hidden links or all the links based on the includeHidden
	 * value
	 */
	public List getLinkList(String groupCode, String linkCode, String linkValue, Boolean includeHidden) {

		// String qwandaServiceUrl = "http://localhost:8280";
		BaseEntity be = getBaseEntityByCode(groupCode);
		List<Link> links = getLinks(groupCode, linkCode);

		List linkList = null;

		if (links != null) {
			for (Link link : links) {
				String linkVal = link.getLinkValue();

				if (linkVal != null && linkVal.equals(linkValue)) {
					Double linkWeight = link.getWeight();
					if (!includeHidden) {
						if (linkWeight >= 1.0) {
							linkList.add(link);

						}
					} else {
						linkList.add(link);
					}
				}
			}
		}
		return linkList;

	}

	/*
	 * Sorting Columns of a SearchEntity as per the weight in either Ascending or
	 * descending order
	 */
	public List<String> sortEntityAttributeBasedOnWeight(final List<EntityAttribute> ea, final String sortOrder) {

		if (ea.size() > 1) {
			Collections.sort(ea, new Comparator<EntityAttribute>() {

				@Override
				public int compare(EntityAttribute ea1, EntityAttribute ea2) {
					if (ea1.getWeight() != null && ea2.getWeight() != null) {
						if (sortOrder.equalsIgnoreCase("ASC"))
							return (ea1.getWeight()).compareTo(ea2.getWeight());
						else
							return (ea2.getWeight()).compareTo(ea1.getWeight());

					} else
						return 0;
				}
			});
		}

		List<String> searchHeader = new CopyOnWriteArrayList<String>();
		for (EntityAttribute ea1 : ea) {
			searchHeader.add(ea1.getAttributeCode().substring("COL_".length()));
		}

		return searchHeader;
	}

	public BaseEntity baseEntityForLayout(String realm, String token, Layout layout) {

		if (GennySettings.disableLayoutLoading) {
			return null;
		}
		if (layout.getPath() == null) {
			return null;
		}

		String serviceToken = RulesUtils.generateServiceToken(realm, token);
		if (serviceToken != null) {

			BaseEntity beLayout = null;

			/* we check if the baseentity for this layout already exists */
			// beLayout =
			// RulesUtils.getBaseEntityByAttributeAndValue(RulesUtils.qwandaServiceUrl,
			// this.decodedTokenMap, this.token, "PRI_LAYOUT_URI", layout.getPath());
			if (layout.getPath().contains("bucket")) {
				log.info("bucket");
			}
			String precode = String.valueOf(layout.getPath().replaceAll("[^a-zA-Z0-9]", "").toUpperCase().hashCode());
			String layoutCode = ("LAY_" + realm + "_" + precode).toUpperCase();

			log.info("Layout - Handling " + layoutCode);
			// try {
			// Check if in cache first to save time.
			beLayout = VertxUtils.readFromDDT(realm, layoutCode, serviceToken);
			// if (beLayout==null) {
			// beLayout = QwandaUtils.getBaseEntityByCode(layoutCode, serviceToken);
			// if (beLayout != null) {
			// VertxUtils.writeCachedJson(layoutCode, JsonUtils.toJson(beLayout),
			// serviceToken);
			// }
			// }

			// } catch (IOException e) {
			// log.error(e.getMessage());
			// }

			/* if the base entity does not exist, we create it */
			if (beLayout == null) {

				log.info("Layout - Creating base entity " + layoutCode);

				/* otherwise we create it */
				beLayout = this.create(layoutCode, layout.getName());
			}

			// if (beLayout != null) {
			//
			// log.info("Layout - Creating base entity " + layoutCode);
			//
			// /* otherwise we create it */
			// beLayout = this.create(layoutCode, layout.getName());
			// }

			if (beLayout != null) {

				this.addAttributes(beLayout);

				/*
				 * we get the modified time stored in the BE and we compare it to the layout one
				 */
				String beModifiedTime = beLayout.getValue("PRI_LAYOUT_MODIFIED_DATE", null);

				log.debug("*** match layout mod date [" + layout.getModifiedDate() + "] with be layout ["
						+ beModifiedTime);

				/* if the modified time is not the same, we update the layout BE */
				/* setting layout attributes */
				List<Answer> answers = new CopyOnWriteArrayList<>();

				/* download the content of the layout */
				String content = LayoutUtils.downloadLayoutContent(layout);
				int existingLayoutHashcode = layout.getData().trim().hashCode();
				int contentHashcode = content.trim().hashCode();

				Optional<EntityAttribute> primaryLayoutData = beLayout.findEntityAttribute("PRI_LAYOUT_DATA");
				String beData = null;
				int behc = 0;
				if (primaryLayoutData.isPresent()) {
					log.debug("beLayout.findEntityAttribute(\"PRI_LAYOUT_DATA\").get().getAsString().trim().hashcode()="
							+ beLayout.findEntityAttribute("PRI_LAYOUT_DATA").get().getAsString().trim().hashCode());
					beData = beLayout.findEntityAttribute("PRI_LAYOUT_DATA").get().getAsString().trim();
					log.debug("baseentity.hashcode()=" + beData.hashCode());
					behc = beData.hashCode();
				}
				log.debug("layout.getData().hashcode()=" + existingLayoutHashcode);
				log.debug("content.hashcode()=" + contentHashcode);

				if (!GennySettings.disableLayoutLoading && (true /* behc != contentHashcode */))

				{
					log.info("Resaving layout: " + layoutCode);

					Answer newAnswerContent = new Answer(beLayout.getCode(), beLayout.getCode(), "PRI_LAYOUT_DATA",
							content);

					newAnswerContent.setChangeEvent(false);
					answers.add(newAnswerContent);

					Answer newAnswer = new Answer(beLayout.getCode(), beLayout.getCode(), "PRI_LAYOUT_URI",
							layout.getPath());
					answers.add(newAnswer);

					Answer newAnswer2 = new Answer(beLayout.getCode(), beLayout.getCode(), "PRI_LAYOUT_URL",
							layout.getDownloadUrl());
					answers.add(newAnswer2);

					Answer newAnswer3 = new Answer(beLayout.getCode(), beLayout.getCode(), "PRI_LAYOUT_NAME",
							layout.getName());
					answers.add(newAnswer3);

					Answer newAnswer4 = new Answer(beLayout.getCode(), beLayout.getCode(), "PRI_LAYOUT_MODIFIED_DATE",
							layout.getModifiedDate());
					answers.add(newAnswer4);

					this.saveAnswers(answers, false); // No change events required

					/* create link between GRP_LAYOUTS and this new LAY_XX base entity */
					this.createLink("GRP_LAYOUTS", beLayout.getCode(), "LNK_CORE", "LAYOUT", 1.0);
				} else {
					log.info("Already have same layout data - not saving ");
				}
			}

			return beLayout;
		}

		return null;
	}

	/*
	 * copy all the attributes from one BE to another BE sourceBe : FROM targetBe :
	 * TO
	 */
	public BaseEntity copyAttributes(final BaseEntity sourceBe, final BaseEntity targetBe) {

		Map<String, String> map = new HashMap<>();
		map = getMapOfAllAttributesValuesForBaseEntity(sourceBe.getCode());
		RulesUtils.ruleLogger("MAP DATA   ::   ", map);

		List<Answer> answers = new CopyOnWriteArrayList<Answer>();
		try {
			for (Map.Entry<String, String> entry : map.entrySet()) {
				Answer answerObj = new Answer(sourceBe.getCode(), targetBe.getCode(), entry.getKey(), entry.getValue());
				answers.add(answerObj);
			}
			saveAnswers(answers);
		} catch (Exception e) {
		}

		return getBaseEntityByCode(targetBe.getCode());
	}

	public String removeLink(final String parentCode, final String childCode, final String linkCode) {
		Link link = new Link(parentCode, childCode, linkCode);
		try {
			return QwandaUtils.apiDelete(this.qwandaServiceUrl + "/qwanda/entityentitys", JsonUtils.toJson(link),
					this.token);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

	/* Remove link with specific link Value */
	public String removeLink(final String parentCode, final String childCode, final String linkCode,
			final String linkValue) {
		Link link = new Link(parentCode, childCode, linkCode, linkValue);
		try {
			return QwandaUtils.apiDelete(this.qwandaServiceUrl + "/qwanda/entityentitys", JsonUtils.toJson(link),
					this.token);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public String removeBaseEntity(final String baseEntityCode) {
		try {

			String result = QwandaUtils.apiDelete(this.qwandaServiceUrl + "/qwanda/baseentitys/" + baseEntityCode,
					this.token);
			log.info("Result from remove user: " + result);
			return result;

		} catch (Exception e) {
			e.printStackTrace();
			return "Failed";
		}
	}

	public void removeEntityAttribute(BaseEntity be, String attributeCode) {
		try {
			QwandaUtils.apiDelete(
					this.qwandaServiceUrl + "/qwanda/baseentitys/delete/" + be.getCode() + "/" + attributeCode, null,
					this.serviceToken.getToken());
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Find EA
		Optional<EntityAttribute> ea = be.findEntityAttribute(attributeCode);
		if (ea.isPresent()) {
			be.removeAttribute(attributeCode);
			updateBaseEntity(be);
		}
	}

	/*
	 * Returns comma seperated list of all the childcode for the given parent code
	 * and the linkcode
	 */
	public String getAllChildCodes(final String parentCode, final String linkCode) {
		String childs = null;
		List<String> childBECodeList = new CopyOnWriteArrayList<String>();
		List<BaseEntity> childBE = this.getLinkedBaseEntities(parentCode, linkCode);
		if (childBE != null) {
			for (BaseEntity be : childBE) {
				childBECodeList.add(be.getCode());
			}
			childs = "\"" + String.join("\", \"", childBECodeList) + "\"";
			childs = "[" + childs + "]";
		}

		return childs;
	}

	/* Get array String value from an attribute of the BE */
	public List<String> getBaseEntityAttrValueList(BaseEntity be, String attributeCode) {

		String myLoadTypes = be.getValue(attributeCode, null);

		if (myLoadTypes != null) {
			List<String> loadTypesList = new CopyOnWriteArrayList<String>();
			/* Removing brackets "[]" and double quotes from the strings */
			String trimmedStr = myLoadTypes.substring(1, myLoadTypes.length() - 1).toString().replaceAll("\"", "");
			if (trimmedStr != null && !trimmedStr.isEmpty()) {
				loadTypesList = Arrays.asList(trimmedStr.split("\\s*,\\s*"));
				return loadTypesList;
			} else {
				return null;
			}
		} else
			return null;
	}

	public <T extends QMessage> QBulkPullMessage createQBulkPullMessage(T msg) {

		// Put the QBulkMessage into the PontoonDDT
		UUID uuid = UUID.randomUUID();
		// DistMap.getDistBE(gennyToken.getRealm()).put("PONTOON_"+uuid.toString(),
		// JsonUtils.toJson(msg), 2, TimeUnit.MINUTES);
		VertxUtils.writeCachedJson(gennyToken.getRealm(), "PONTOON_" + uuid.toString().toUpperCase(),
				JsonUtils.toJson(msg), this.getGennyToken().getToken(), GennySettings.pontoonTimeout); // 2 minutes

		// DistMap.getDistPontoonBE(gennyToken.getRealm()).put(uuid.toString(),
		// JsonUtils.toJson(msg), 2, TimeUnit.MINUTES);

		QBulkPullMessage pullMsg = new QBulkPullMessage(uuid.toString());
		pullMsg.setToken(this.getGennyToken().getToken());
		// Put the QBulkMessage into the PontoonDDT

		// then create the QBulkPullMessage
		pullMsg.setPullUrl(/* GennySettings.pontoonUrl + */ "api/pull/" + uuid.toString().toUpperCase());
		return pullMsg;

	}

	public QBulkPullMessage createQBulkPullMessage(String msg) {

		// Put the QBulkMessage into the PontoonDDT
		UUID uuid = UUID.randomUUID();
		// DistMap.getDistBE(gennyToken.getRealm()).put("PONTOON_"+uuid.toString(),
		// JsonUtils.toJson(msg), 2, TimeUnit.MINUTES);
		VertxUtils.writeCachedJson(this.getGennyToken().getRealm(), "PONTOON_" + uuid.toString().toUpperCase(), msg,
				this.getGennyToken().getToken(), GennySettings.pontoonTimeout); // 2 minutes

		QBulkPullMessage pullMsg = new QBulkPullMessage(uuid.toString());
		pullMsg.setToken(token);

		// then create the QBulkPullMessage
		pullMsg.setPullUrl(/* GennySettings.pontoonUrl + */ "api/pull/" + uuid.toString().toUpperCase());
		return pullMsg;

	}

	public static QBulkPullMessage createQBulkPullMessage(JsonObject msg) {

		UUID uuid = UUID.randomUUID();
		String token = msg.getString("token");
		String realm = msg.getString("realm");

		// Put the QBulkMessage into the PontoonDDT
		// DistMap.getDistPontoonBE(gennyToken.getRealm()).put(uuid, msg, 2,
		// TimeUnit.MINUTES);
		VertxUtils.writeCachedJson(realm, "PONTOON_" + uuid.toString().toUpperCase(), JsonUtils.toJson(msg), token,
				GennySettings.pontoonTimeout);

		// then create the QBulkPullMessage
		QBulkPullMessage pullMsg = new QBulkPullMessage(uuid.toString());
		pullMsg.setToken(token);
		pullMsg.setPullUrl(/* GennySettings.pontoonUrl + */ "api/pull/" + uuid.toString().toUpperCase());
		return pullMsg;

	}

	/**
	 * @return the gennyToken
	 */
	public GennyToken getGennyToken() {
		return gennyToken;
	}

	/**
	 * @param gennyToken the gennyToken to set
	 */
	public void setGennyToken(GennyToken gennyToken) {
		this.gennyToken = gennyToken;
	}

	@Override
	public String toString() {
		return "BaseEntityUtils [" + (realm != null ? "realm=" + realm : "") + ": "
				+ StringUtils.abbreviateMiddle(token, "...", 30) + "]";
	}

	/**
	 * @return the serviceToken
	 */
	public GennyToken getServiceToken() {
		return serviceToken;
	}

	/**
	 * @param serviceToken the serviceToken to set
	 */
	public void setServiceToken(GennyToken serviceToken) {
		this.serviceToken = serviceToken;
	}

	/**
	 * @param searchBE
	 * @return
	 */
	public List<BaseEntity> getBaseEntitys(final SearchEntity searchBE) {
		List<BaseEntity> results = new ArrayList<BaseEntity>();

		try {
			String resultJsonStr = null;

			if (false /* GennySettings.forceCacheApi */) {// TODO - Work out search25
				Tuple2<String, List<String>> emailhqlTuple = getHql(searchBE);
				String emailhql = emailhqlTuple._1;

				emailhql = Base64.getUrlEncoder().encodeToString(emailhql.getBytes());

				resultJsonStr = QwandaUtils.apiGet(GennySettings.qwandaServiceUrl + "/qwanda/baseentitys/search24/"
						+ emailhql + "/" + searchBE.getPageStart(searchBE.getPageStart(0)) + "/"
						+ searchBE.getPageSize(GennySettings.defaultPageSize), serviceToken.getToken(), 120);
			} else {

				resultJsonStr = QwandaUtils.apiPostEntity2(
						GennySettings.qwandaServiceUrl + "/qwanda/baseentitys/search25/", JsonUtils.toJson(searchBE),
						serviceToken.getToken(), null);
			}

			JsonObject resultJson = null;

			try {
				resultJson = new JsonObject(resultJsonStr);
				io.vertx.core.json.JsonArray result = resultJson.getJsonArray("codes");
				int size = result.size();
				for (int i = 0; i < size; i++) {
					String code = result.getString(i);
					BaseEntity be = getBaseEntityByCode(code);
//					System.out.println("code:" + code + ",index:" + (i+1) + "/" + size);

					be.setIndex(i);
					results.add(be);
				}

			} catch (Exception e1) {
				log.error("Bad Json -> " + resultJsonStr);
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}
		return results;
	}

	public Tuple2<String, List<String>> getHql(SearchEntity searchBE)

	{
		List<String> attributeFilter = new ArrayList<String>();
		List<String> assocAttributeFilter = new ArrayList<String>();

		List<Tuple3> sortFilters = new ArrayList<Tuple3>();
		List<String> beFilters = new ArrayList<String>();
		// List<List<Tuple2>> attributeFilters = new ArrayList<ArrayList<Tuple2>>();
		HashMap<String, ArrayList<String>> attributeFilters = new HashMap<String, ArrayList<String>>();

		String stakeholderCode = null;
		String sourceStakeholderCode = null;
		String linkCode = null;
		String linkValue = null;
		String sourceCode = null;
		String targetCode = null;

		String wildcardValue = null;
		Integer pageStart = searchBE.getPageStart(0);
		Integer pageSize = searchBE.getPageSize(GennySettings.defaultPageSize);

		for (EntityAttribute ea : searchBE.getBaseEntityAttributes()) {

			String attributeCode = removePrefixFromCode(ea.getAttributeCode(), "OR");
			attributeCode = removePrefixFromCode(attributeCode, "AND");

			if (attributeCode.equals("PRI_CODE")) {
				beFilters.add(ea.getAsString());

			} else if (attributeCode.startsWith("SRT_")) {

				String sortCode = null;
				String standardSortString = null;
				String customSortString = null;
				if (attributeCode.startsWith("SRT_PRI_CREATED")) {
					standardSortString = ".created " + ea.getValueString();
				} else if (attributeCode.startsWith("SRT_PRI_UPDATED")) {
					standardSortString = ".updated " + ea.getValueString();
				} else if (attributeCode.startsWith("SRT_PRI_CODE")) {
					standardSortString = ".baseEntityCode " + ea.getValueString();
				} else if (attributeCode.startsWith("SRT_PRI_NAME")) {
					standardSortString = ".pk.baseEntity.name " + ea.getValueString();
				}

				else {
					sortCode = (attributeCode.substring("SRT_".length()));
					Attribute attr = RulesUtils.getAttribute(sortCode, this.token);
					String dtt = attr.getDataType().getClassName();
					Object sortValue = ea.getValue();
					if (dtt.equals("Text")) {
						customSortString = ".valueString " + sortValue.toString();
					} else if (dtt.equals("java.lang.String") || dtt.equals("String")) {
						customSortString = ".valueString " + sortValue.toString();
					} else if (dtt.equals("java.lang.Boolean") || dtt.equals("Boolean")) {
						customSortString = ".valueBoolean " + sortValue.toString();
					} else if (dtt.equals("java.lang.Double") || dtt.equals("Double")) {
						customSortString = ".valueDouble " + sortValue.toString();
					} else if (dtt.equals("java.lang.Integer") || dtt.equals("Integer")) {
						customSortString = ".valueInteger " + sortValue.toString();
					} else if (dtt.equals("java.lang.Long") || dtt.equals("Long")) {
						customSortString = ".valueLong " + sortValue.toString();
					} else if (dtt.equals("java.time.LocalDateTime") || dtt.equals("LocalDateTime")) {
						customSortString = ".valueDateTime " + sortValue.toString();
					} else if (dtt.equals("java.time.LocalDate") || dtt.equals("LocalDate")) {
						customSortString = ".valueDate " + sortValue.toString();
					} else if (dtt.equals("java.time.LocalTime") || dtt.equals("LocalTime")) {
						customSortString = ".valueTime " + sortValue.toString();
					}
				}

				Integer index = null;
				for (Tuple3<String, String, Double> sort : sortFilters) {
					if (ea.getWeight() <= sort._3) {
						index = sortFilters.indexOf(sort);
						break;
					}
				}

				// Order Sorts by weight
				if (index == null) {
					if (standardSortString != null) {
						sortFilters.add(Tuple.of("", standardSortString, ea.getWeight()));
					}
					if (customSortString != null) {
						sortFilters.add(Tuple.of(sortCode, customSortString, ea.getWeight()));
					}
				} else {
					if (standardSortString != null) {
						sortFilters.add(index, Tuple.of("", standardSortString, ea.getWeight()));
					}
					if (customSortString != null) {
						sortFilters.add(index, Tuple.of(sortCode, customSortString, ea.getWeight()));
					}
				}

			} else if (attributeCode.startsWith("SCH_STAKEHOLDER_CODE")) {
				stakeholderCode = ea.getValue();
			} else if (attributeCode.startsWith("SCH_SOURCE_STAKEHOLDER_CODE")) {
				sourceStakeholderCode = ea.getValue();
			} else if (attributeCode.startsWith("SCH_LINK_CODE")) {
				linkCode = ea.getValue();
			} else if (attributeCode.startsWith("SCH_LINK_VALUE")) {
				linkValue = ea.getValue();
			} else if (attributeCode.startsWith("SCH_SOURCE_CODE")) {
				sourceCode = ea.getValue();
			} else if (attributeCode.startsWith("SCH_TARGET_CODE")) {
				targetCode = ea.getValue();
			} else if ((attributeCode.startsWith("COL__")) || (attributeCode.startsWith("CAL_"))) {
				String[] splitCode = attributeCode.substring("COL__".length()).split("__");
				assocAttributeFilter.add(splitCode[0]);
				// assocAttributeFilter.add(splitCode[1]);

			} else if ((attributeCode.startsWith("COL_")) || (attributeCode.startsWith("CAL_"))) {
				// add latittude and longitude to attributeFilter list if the current ea is
				// PRI_ADDRESS_FULL
				if (attributeCode.equals("COL_PRI_ADDRESS_FULL")) {
					attributeFilter.add("PRI_ADDRESS_LATITUDE");
					attributeFilter.add("PRI_ADDRESS_LONGITUDE");
				}
				attributeFilter.add(attributeCode.substring("COL_".length()));
			} else if (attributeCode.startsWith("SCH_WILDCARD")) {
				if (ea.getValueString() != null) {
					if (!StringUtils.isBlank(ea.getValueString())) {
						wildcardValue = ea.getValueString();
						wildcardValue = wildcardValue.replaceAll(("[^A-Za-z0-9 ]"), "");
					}
				}
			} else if ((attributeCode.startsWith("PRI_") || attributeCode.startsWith("LNK_"))
					&& (!attributeCode.equals("PRI_CODE")) && (!attributeCode.equals("PRI_TOTAL_RESULTS"))
					&& (!attributeCode.equals("PRI_INDEX"))) {
				String condition = SearchEntity.convertFromSaveable(ea.getAttributeName());
				if (condition == null) {
					log.error("SQL condition is NULL, " + "EntityAttribute baseEntityCode is:" + ea.getBaseEntityCode()
							+ ", attributeCode is: " + attributeCode + ", ea.getAttributeCode() is: "
							+ ea.getAttributeCode());
				}
				// String aName = ea.getAttributeName();

				if (!((ea.getValueString() != null) && (ea.getValueString().equals("%"))
						&& (ea.getAttributeName().equals("LIKE")))) {
					// Only add a filter if it is not a wildcard
					if (ea.getAttributeCode().startsWith("AND_")) {
						attributeCode = ea.getAttributeCode();
					}
					ArrayList<String> valueList = new ArrayList<String>();
					for (String key : attributeFilters.keySet()) {
						if (key.equals(attributeCode)) {
							valueList = attributeFilters.get(key);
						}
					}
					valueList.add(getAttributeValue(ea, condition));
					attributeFilters.put(attributeCode, valueList);
					// attributeFilters.add(Tuple.of(ea.getAttributeCode(), getAttributeValue(ea,
					// condition)));
				}
			}
		}

		String hql = "select distinct ea.baseEntityCode from EntityAttribute ea";

		int c = 0;
		for (String key : attributeFilters.keySet()) {
			hql += " left outer join EntityAttribute e" + c + " on e" + c + ".baseEntityCode=ea.baseEntityCode";
			hql += " and e" + c + ".attributeCode = '" + removePrefixFromCode(key, "AND") + "'";
			c += 1;
		}

		if (wildcardValue != null) {
			hql += " left outer join EntityAttribute ew on ew.baseEntityCode=ea.baseEntityCode";
		}

		for (int i = 0; i < sortFilters.size(); i++) {
			Tuple3<String, String, Double> sort = sortFilters.get(i);
			if (!sort._1.isEmpty()) {
				hql += " left outer join EntityAttribute ez" + i + " on ez" + i + ".baseEntityCode=ea.baseEntityCode";
				hql += " and ea.baseEntityCode=ez" + i + ".baseEntityCode and ez" + i + ".attributeCode='"
						+ sort._1.toString() + "'";
			}
		}

		if (sourceCode != null || targetCode != null || linkCode != null || linkValue != null) {
			hql += " inner join EntityEntity ee";
			hql += " on (";

			if (sourceCode != null && targetCode == null) {
				targetCode = "ea.baseEntityCode";
				sourceCode = "'" + sourceCode + "'";
			} else if (targetCode != null && sourceCode == null) {
				sourceCode = "ea.baseEntityCode";
				targetCode = "'" + targetCode + "'";
			} else if (sourceCode != null && targetCode != null) {
				sourceCode = "'" + sourceCode + "'";
				targetCode = "'" + targetCode + "'";
			}

			hql += (sourceCode != null
					? " ee.link.sourceCode " + (sourceCode.contains("%") ? "like " : "= ") + sourceCode
					: "");
			hql += (targetCode != null
					? " and ee.link.targetCode " + (targetCode.contains("%") ? "like " : "= ") + targetCode
					: "");

			hql += (linkCode != null
					? " and ee.link.attributeCode " + (linkCode.contains("%") ? "like " : "= ") + "'" + linkCode + "'"
					: "");
			hql += (linkValue != null
					? " and ee.link.linkValue " + (linkValue.contains("%") ? "like " : "= ") + "'" + linkValue + "'"
					: "");

			hql = hql.replace("on ( and", "on (");
			hql += " )";
		}

		if (beFilters.size() > 0 || searchBE.getCode().startsWith("SBE_SEARCHBAR") || attributeFilters.size() > 0
				|| wildcardValue != null || sortFilters.size() > 0) {
			hql += " where";
		}

		if (beFilters.size() > 0) {
			hql += " (";
			for (int i = 0; i < beFilters.size(); i++) {
				if (i > 0) {
					hql += " or";
				}
				hql += " ea.baseEntityCode like '" + beFilters.get(i) + "'";
			}
			hql += " )";
		}

		if (searchBE.getCode().startsWith("SBE_SEARCHBAR")) {
			// search across people and companies
			hql += " and (ea.baseEntityCode like 'PER_%' or ea.baseEntityCode like 'CPY_%')";
		}

		if (attributeFilters.size() > 0) {
			int i = 0;
			for (String key : attributeFilters.keySet()) {
				hql += " and";
				ArrayList<String> valueList = attributeFilters.get(key);
				if (valueList.size() > 1) {
					hql += " (";
				}
				for (String value : valueList) {
					if (valueList.size() > 1) {
						hql += " or";
					}
					hql += (!StringUtils.isBlank(value)) ? (" e" + i + value) : "";
				}
				if (valueList.size() > 1) {
					hql += " )";
				}
				i += 1;
			}
		}
		hql = hql.replace("( or", "(");

		if (wildcardValue != null) {
			hql += " and ew.valueString like '%" + wildcardValue + "%'";
		}

		if (sortFilters.size() > 0) {
			// sort the sorts
			List<Tuple3> sortedFilters = sortFilters.stream()
					.sorted((o1, o2) -> ((Double) (o1._3)).compareTo((Double) (o2._3))).collect(Collectors.toList());
			String orderBy = " order by";
			for (int i = 0; i < sortedFilters.size(); i++) {
				Tuple3<String, String, Double> sort = sortedFilters.get(i);
				if (i > 0) {
					orderBy += ",";
				}
				if (sort._1.isEmpty()) {
					orderBy += " ea" + sort._2.toString();
				} else {
					orderBy += " ez" + i + sort._2.toString() + " nulls last";
				}
			}
			hql += orderBy;
		}

		hql = hql.replace("where and", "where");
		attributeFilter.addAll(assocAttributeFilter);
		return Tuple.of(hql, attributeFilter);
	}

	/**
	 * Quick tool to remove any prefix strings from attribute codes, even if the
	 * prefix occurs multiple times.
	 *
	 * @param code   The attribute code
	 * @param prefix The prefix to remove
	 * @return formatted The formatted code
	 */
	public String removePrefixFromCode(String code, String prefix) {

		String formatted = code;
		while (formatted.startsWith(prefix + "_")) {
			formatted = formatted.substring(prefix.length() + 1);
		}
		return formatted;
	}

	public String getAttributeValue(EntityAttribute ea, String condition) {

		if (ea.getValueString() != null) {
			String val = ea.getValueString();
			if (ea.getValueString().contains(":")) {
				String[] split = ea.getValueString().split(":");
				if (StringUtils.isBlank(split[0])) {
					condition = "LIKE";
					val = "%" + split[1] + "%";
				} else {
					condition = split[0];
					val = split[1];
				}
			}
			return ".valueString " + condition + " '" + val + "'";
		} else if (ea.getValueBoolean() != null) {

			return ".valueBoolean = " + (ea.getValueBoolean() ? "true" : "false");
		} else if (ea.getValueDouble() != null) {
			return ".valueDouble = " + condition + " " + ea.getValueDouble() + "";
		} else if (ea.getValueInteger() != null) {
			return ".valueInteger " + condition + " " + ea.getValueInteger() + "";
		} else if (ea.getValueDate() != null) {
			return ".valueDate " + condition + " '" + ea.getValueDate() + "'";
		} else if (ea.getValueDateTime() != null) {
			return ".valueDateTime " + condition + " '" + ea.getValueDateTime() + "'";
		}
		return null;
	}

	public List<BaseEntity> getRoles() {
		List<BaseEntity> roles = new ArrayList<BaseEntity>();
		BaseEntity be = this.getBaseEntityByCode(this.getGennyToken().getUserCode());
		if ((be != null) && (be.getCode().startsWith("PER_"))) {
			for (EntityAttribute ea : be.getBaseEntityAttributes()) {
				if (ea.getAttributeCode().startsWith("PRI_IS_")) {
					String roleCode = "ROL_" + ea.getAttributeCode().substring("PRI_IS_".length());
					BaseEntity role = this.getBaseEntityByCode(roleCode);
					if (role != null) {
						roles.add(role);
					}
				}
			}
		}

		return roles;
	}

	public String getEmailFromOldCode(String oldCode) {
		String ret = null;
		if (oldCode.contains("_AT_")) {

			if ("PER_JIUNWEI_DOT_LU_AT_CQUMAIL_DOT_COM".equals(oldCode)) {
				oldCode = "PER_JIUN_DASH_WEI_DOT_LU_AT_CQUMAIL_DOT_COM"; // addd dash
			}

			oldCode = oldCode.substring(4);
			// convert to email
			oldCode = oldCode.replaceAll("_PLUS_", "+");
			oldCode = oldCode.replaceAll("_DOT_", ".");
			oldCode = oldCode.replaceAll("_AT_", "@");
			oldCode = oldCode.replaceAll("_DASH_", "-");
			ret = oldCode.toLowerCase();
		}
		return ret;
	}

	public BaseEntity getPersonFromEmail(String email) {
		BaseEntity person = null;

		SearchEntity searchBE = new SearchEntity("SBE_TEST", "email")
				.addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "PER_%")
				.addFilter("PRI_EMAIL", SearchEntity.StringFilter.LIKE, email).addColumn("PRI_CODE", "Name")
				.addColumn("PRI_EMAIL", "Email");

		searchBE.setRealm(realm);
		searchBE.setPageStart(0);
		searchBE.setPageSize(100000);
		Tuple2<String, List<String>> emailhqlTuple = getHql(searchBE);
		String emailhql = emailhqlTuple._1;

		emailhql = Base64.getUrlEncoder().encodeToString(emailhql.getBytes());
		try {
			String resultJsonStr = QwandaUtils.apiGet(
					GennySettings.qwandaServiceUrl + "/qwanda/baseentitys/search24/" + emailhql + "/"
							+ searchBE.getPageStart(0) + "/" + searchBE.getPageSize(GennySettings.defaultPageSize),
					serviceToken.getToken(), 120);

			JsonObject resultJson = null;
			resultJson = new JsonObject(resultJsonStr);
			io.vertx.core.json.JsonArray result2 = resultJson.getJsonArray("codes");
			String internCode = result2.getString(0);
			if (internCode.contains("_AT_")) {
				person = getBaseEntityByCode(internCode);
			}

		} catch (Exception e) {

		}
		return person;
	}

	public JsonObject writeMsg(BaseEntity be) {
		return writeMsg(be, new String[0]);
	}

	public JsonObject writeMsg(BaseEntity be, String... rxList) {
		QDataBaseEntityMessage msg = new QDataBaseEntityMessage(be);
		msg.setToken(this.gennyToken.getToken());
		msg.setReplace(true);
		if ((rxList != null) && (rxList.length > 0)) {
			msg.setRecipientCodeArray(rxList);
			return VertxUtils.writeMsg("project", msg);
		} else {
			return VertxUtils.writeMsg("webdata", msg);
		}
	}

	public void processVideoAttribute(EntityAttribute ea) {
		String value = ea.getValueString();
		if (value != null && !value.startsWith("http")) {

			log.info("My Interview");
			BaseEntity project = this.getBaseEntityByCode("PRJ_" + this.getGennyToken().getRealm().toUpperCase());
			String apiKey = project.getValueAsString("ENV_API_KEY_MY_INTERVIEW");
			String secretToken = project.getValueAsString("ENV_SECRET_MY_INTERVIEW");
			long unixTimestamp = Instant.now().getEpochSecond();
			String apiSecret = apiKey + secretToken + unixTimestamp;
			String hashed = BCrypt.hashpw(apiSecret, BCrypt.gensalt(10));
			String videoId = ea.getValueString();
			String url = "https://api.myinterview.com/2.21.2/getVideo?apiKey=" + apiKey + "&hashTimestamp="
					+ unixTimestamp + "&hash=" + hashed + "&video=" + videoId;
			String url2 = "https://embed.myinterview.com/player.v3.html?apiKey=" + apiKey + "&hashTimestamp="
					+ unixTimestamp + "&hash=" + hashed + "&video=" + videoId + "&autoplay=1&fs=0";

			log.info("MyInterview Hash is " + url);
			log.info("MyInterview Hash2 is " + url2);
			ea.setValue(url2);

		}
	}

	public String getValueSaveAnswer(String beSource, String beTarget, String beValue, String getAttribute,
			String setAttribute, String value) {
		try {
			BaseEntity beValueBE = this.getBaseEntityByCode(beValue);

			value = beValueBE.getValue(getAttribute, null);
			System.out.println(value + ": " + value);
			if (value != null) {
				saveAnswer(new Answer(beSource, beTarget, setAttribute, value));
			}
		} catch (Exception e) {
		}
		return value;
	}

	public void quantumCopy(BaseEntity sourceBE, String sourceAtt, Boolean saveLink, Boolean strip, String userToken,
			String targetBE, String targetAtt) {
		try {

			List<Answer> answers = new ArrayList<>();

			String value = sourceBE.getValue(sourceAtt, null);
			System.out.println("value = " + value);
			if (value != null) {
				if (saveLink) {
					answers.add(new Answer(userToken, targetBE, sourceAtt, value));
					this.saveAnswers(answers);
				}
				if (strip) {
					value = value.replace("\"", "").replace("[", "").replace("]", "");

					BaseEntity valueBE = this.getBaseEntityByCode(value);
					System.out.println("valueBE = " + valueBE);

					if (valueBE != null) {
						String name = valueBE.getValue("PRI_NAME", null);
						System.out.println("name = " + name);

						if (name != null) {
							answers.add(new Answer(userToken, targetBE, targetAtt, name));
							this.saveAnswers(answers);
						} else {
							System.out.println("ERROR: Null String - name");
						}
					} else {
						System.out.println("ERROR: Null BaseEnity - valueBE");
					}
				}

			} else {
				System.out.println("ERROR: Null String - value");
			}

		} catch (Exception e) {
		}
	}

	public void quantumLink(String sourceCode, String targetCode, String focusCode, String attribute) {
		try {

			BaseEntity targetBe = getBaseEntityByCode(targetCode);
			System.out.println(targetBe);
			Optional<String> optLnkApplication = targetBe.getValue(attribute);

			List<Answer> answers = new ArrayList<>();

			if (optLnkApplication.isPresent()) {
				System.out.println("Multiple links detected");

				String lnkApp = optLnkApplication.get();
				System.out.println(attribute + "  before::  " + lnkApp);
				if (lnkApp != null) {
					/* convert to list */
					Gson gson = new Gson();
					List<String> appList = gson.fromJson(lnkApp, List.class);

					/* add the new BE code to list */
					appList.add(focusCode);

					/* convert to string */
					String results = gson.toJson(appList);
					System.out.println(attribute + "  after::  " + results);

					/* save the answer to target */
					answers.add(new Answer(sourceCode, targetCode, attribute, results));
					this.saveAnswers(answers);
				}
			} else {
				/* if no: the intern has not been applied to other applications */
				/* save the answer to internBe */
				System.out.println("No similar links detected");

				String results = "[\"" + focusCode + "\"]";
				System.out.println(attribute + "  ::  " + results);

				answers.add(new Answer(sourceCode, targetCode, attribute, results));
				this.saveAnswers(answers);

			}
		} catch (Exception e) {
		}
	}

	public void removeQuantumLinkElement(String sourceCode, String targetCode, String focusCode, String attribute) {
		try {

			BaseEntity targetBe = this.getBaseEntityByCode(targetCode);
			System.out.println(targetBe);
			Optional<String> optLnkApplication = targetBe.getValue(attribute);

			if (optLnkApplication.isPresent()) {
				System.out.println("Multiple links detected");

				String lnkApp = optLnkApplication.get();
				System.out.println(attribute + "  before::  " + lnkApp);
				if (lnkApp != null) {
					/* convert to list */
					Gson gson = new Gson();
					List<String> appList = gson.fromJson(lnkApp, List.class);

					/* add the new BE code to list */
					appList.remove(focusCode);

					/* convert to string */
					String results = gson.toJson(appList);
					System.out.println(attribute + "  after::  " + results);

					/* save the answer to target */
					this.saveAnswer(new Answer(sourceCode, targetCode, attribute, results));
				}
			} else {
				/* if no: the intern has not been applied to other applications */
				/* save the answer to internBe */
				System.out.println("No similar links detected");
			}
		} catch (Exception e) {
		}
	}

	public String quantumStrip(BaseEntity sourceBe, String attribute, String logString) {

		String linkedCode = sourceBe.getValue(attribute, null);
		if (linkedCode != null) {
			linkedCode = linkedCode.replace("\"", "").replace("[", "").replace("]", "");
			System.out.println(logString + " = " + linkedCode);
		}
		return linkedCode;
	}

	public BaseEntity quantumBe(String linkedCode, String logString) {

		BaseEntity assocBe = null;

		if (linkedCode != null) {
			assocBe = this.getBaseEntityByCode(linkedCode);
			System.out.println(logString + " = " + assocBe);

		}
		return assocBe;
	}

	public String whoAreYou(String targetCode) { // TODO, ths is only internmatch??

		String attribute = null;

		BaseEntity targetBe = this.getBaseEntityByCode(targetCode);
		System.out.println("targetBe: " + targetBe);

		Boolean isI = targetBe.getValue("PRI_IS_INTERN", false);
		Boolean isIS = targetBe.getValue("PRI_IS_INTERNSHIP", false);
		Boolean isEPR = targetBe.getValue("PRI_IS_EDU_PRO_REP", false);
		Boolean isEP = targetBe.getValue("PRI_IS_EDU_PROVIDER", false);
		Boolean isHCR = targetBe.getValue("PRI_IS_HOST_CPY_REP", false);
		Boolean isHC = targetBe.getValue("PRI_IS_HOST_CPY", false);
		Boolean isA = targetBe.getValue("PRI_IS_AGENT", false);
		Boolean isAG = targetBe.getValue("PRI_IS_AGENCY", false);

		System.out.println("===== Which type of user =====");
		System.out.println("Intern:" + isI);
		System.out.println("Internship:" + isIS);
		System.out.println("EPR:" + isEPR);
		System.out.println("EP:" + isEP);
		System.out.println("HCR:" + isHCR);
		System.out.println("HC:" + isHC);
		System.out.println("Agent:" + isA);
		System.out.println("Agency:" + isAG);
		System.out.println("==============================");

		if (isI) {
			attribute = "PRI_IS_INTERN";
		}
		if (isIS) {
			attribute = "PRI_IS_INTERNSHIP";
		}
		if (isEPR) {
			attribute = "PRI_IS_EDU_PRO_REP";
		}
		if (isEP) {
			attribute = "PRI_IS_EDU_PROVIDER";
		}
		if (isHCR) {
			attribute = "PRI_IS_HOST_CPY_REP";
		}
		if (isHC) {
			attribute = "PRI_IS_HOST_CPY";
		}
		if (isA) {
			attribute = "PRI_IS_AGENT";
		}
		if (isAG) {
			attribute = "PRI_IS_AGENCY";
		}
		System.out.println("Who are you? " + attribute);

		return attribute;
	}

	public BaseEntity getDEF(final BaseEntity be) {
		Set<EntityAttribute> newMerge = new HashSet<>();
		List<EntityAttribute> isAs = be.findPrefixEntityAttributes("PRI_IS_");
		if (isAs.size() == 1) {
			// Easy
			BaseEntity defBe = RulesUtils.defs.get(be.getRealm())
					.get("DEF_" + isAs.get(0).getAttributeCode().substring("PRI_IS_".length()));
			return defBe;
		} else if (isAs.isEmpty()) {
			// THIS HANDLES CURRENT BAD BEs
			if (be.getCode().startsWith("CPY_")) {
				BaseEntity defBe = RulesUtils.defs.get(be.getRealm()).get("DEF_COMPANY");
				return defBe;
			} else if (be.getCode().startsWith("PER_")) {
				BaseEntity defBe = RulesUtils.defs.get(be.getRealm()).get("DEF_PERSON");
				return defBe;
			} else if (be.getCode().startsWith("BEG_")) {
				BaseEntity defBe = RulesUtils.defs.get(be.getRealm()).get("DEF_INTERNSHIP");
				return defBe;
			} else if (be.getCode().startsWith("JNL_")) {
				BaseEntity defBe = RulesUtils.defs.get(be.getRealm()).get("DEF_JOURNAL");
				return defBe;
			} else if (be.getCode().startsWith("RUL_")) {
				BaseEntity defBe = RulesUtils.defs.get(be.getRealm()).get("DEF_RULE");
				return defBe;
			} else if (be.getCode().startsWith("ROL_")) {
				BaseEntity defBe = RulesUtils.defs.get(be.getRealm()).get("DEF_ROLE");
				return defBe;
			} else if (be.getCode().startsWith("BKT_")) {
				BaseEntity defBe = RulesUtils.defs.get(be.getRealm()).get("DEF_BUCKET_PAGE");
				return defBe;
			} else if (be.getCode().startsWith("APT_")) {
				BaseEntity defBe = RulesUtils.defs.get(be.getRealm()).get("DEF_APPOINTMENT");
				return defBe;
			} else if (be.getCode().startsWith("DEV_")) {
				BaseEntity defBe = RulesUtils.defs.get(be.getRealm()).get("DEF_DEVICE");
				return defBe;
			} else if (be.getCode().startsWith("FRM_")) {
				BaseEntity defBe = RulesUtils.defs.get(be.getRealm()).get("DEF_FORM");
				return defBe;
			}

			else if (be.getCode().startsWith("APP_")) {
				BaseEntity defBe = RulesUtils.defs.get(be.getRealm()).get("DEF_APPLICATION");
				if (defBe == null) {
					log.error("NO DEF ASSOCIATED WITH APP be " + be.getCode());
					return new BaseEntity("ERR_DEF", "No DEF");
				}
				return defBe;
			}

			log.error("NO DEF ASSOCIATED WITH be " + be.getCode());
			return new BaseEntity("ERR_DEF", "No DEF");
		} else {
			// Create sorted merge code
			String mergedCode = "DEF_" + isAs.stream().sorted(Comparator.comparing(EntityAttribute::getAttributeCode))
					.map(ea -> ea.getAttributeCode()).collect(Collectors.joining("_"));
			mergedCode = mergedCode.replaceAll("_PRI_IS_DELETED", "");
			BaseEntity mergedBe = RulesUtils.defs.get(be.getRealm()).get(mergedCode);
			if (mergedBe == null) {
				log.info("Detected NEW Combination DEF - " + mergedCode);
				// Get primary PRI_IS
				Optional<EntityAttribute> topDog = be.getHighestEA("PRI_IS_");
				if (topDog.isPresent()) {
					String topCode = topDog.get().getAttributeCode().substring("PRI_IS_".length());
					BaseEntity defTopDog = RulesUtils.defs.get(be.getRealm()).get("DEF_" + topCode);
					mergedBe = new BaseEntity(mergedCode, mergedCode); // So this combination DEF inherits top dogs name
					// now copy all the combined DEF eas.
					for (EntityAttribute isea : isAs) {
						BaseEntity defEa = RulesUtils.defs.get(be.getRealm())
								.get("DEF_" + isea.getAttributeCode().substring("PRI_IS_".length()));
						if (defEa != null) {
							for (EntityAttribute ea : defEa.getBaseEntityAttributes()) {
								try {
									mergedBe.addAttribute(ea);
								} catch (BadDataException e) {
									log.error("Bad data in getDEF ea merge " + mergedCode);
								}
							}
						} else {
							log.info(
									"No DEF code -> " + "DEF_" + isea.getAttributeCode().substring("PRI_IS_".length()));
							return null;
						}
					}
					RulesUtils.defs.get(be.getRealm()).put(mergedCode, mergedBe);
					return mergedBe;

				} else {
					log.error("NO DEF EXISTS FOR " + be.getCode());
					return null;
				}
			} else {
				return mergedBe; // return 'merged' composite
			}
		}

	}

	public Boolean hasDefDropdown(final String attributeCode, final BaseEntity target) throws Exception {
		BaseEntity defBe = this.getDEF(target);

		return hasDropdown(attributeCode, defBe);
	}
	
	public Boolean hasDropdown(final String attributeCode, final BaseEntity defBe) throws Exception {
		if (!defBe.getCode().startsWith("DEF_")) {
			log.error("Cannot determine if dropdown exists , Not a DEF! "+defBe.getCode());
			throw new Exception("Cannot determine if dropdown exists , Not a DEF!");
		}

		// Check if attribute code exists as a SER
		Optional<EntityAttribute> searchAtt = defBe.findEntityAttribute("SER_" + attributeCode); // SER_
		if (searchAtt.isPresent()) {
			// temporary enable check
			String serValue = searchAtt.get().getValueString();
			log.info("Attribute exists in "+defBe.getCode()+" for SER_" + attributeCode+" --> "+serValue);
			JsonObject serJson = new JsonObject(serValue);
			if (serJson.containsKey("enabled")) {
				Boolean isEnabled = serJson.getBoolean("enabled");
				return isEnabled;
			} else {
				log.info("Attribute exists in "+defBe.getCode()+" for SER_" + attributeCode+" --> but NOT enabled!");
			}
		} else {
			log.info("No attribute exists in "+defBe.getCode()+" for SER_" + attributeCode);
		}
		return false;

	}


}
