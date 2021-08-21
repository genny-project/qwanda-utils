package life.genny.utils;

import java.lang.invoke.MethodHandles;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.Logger;

import io.vertx.core.json.JsonObject;
import life.genny.models.GennyToken;
import life.genny.qwanda.entity.BaseEntity;
import life.genny.qwanda.entity.SearchEntity;
import life.genny.qwandautils.GennySettings;

public class DefUtils {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	protected static final Logger log = org.apache.logging.log4j.LogManager
			.getLogger(MethodHandles.lookup().lookupClass().getCanonicalName());

    static public Map<String,Map<String,BaseEntity>> defs = new ConcurrentHashMap<>();  // realm and DEF lookup

	/**
	 * @param realm
	 */
	public static void loadDEFS(String realm) {
		log.info("Loading in DEFS for realm "+realm);
		
		SearchEntity searchBE = new SearchEntity("SBE_DEF", "DEF test")
				.addSort("PRI_NAME", "Created", SearchEntity.Sort.ASC)
				.addFilter("PRI_CODE", SearchEntity.StringFilter.LIKE, "DEF_%")
				
				.addColumn("PRI_NAME", "Name");

		searchBE.setRealm(realm);
		searchBE.setPageStart(0);
		searchBE.setPageSize(10000);

		JsonObject tokenObj = VertxUtils.readCachedJson(GennySettings.GENNY_REALM, "TOKEN" + realm.toUpperCase());
		String sToken = tokenObj.getString("value");
		GennyToken serviceToken = new GennyToken("PER_SERVICE", sToken);

		if ((serviceToken == null) || ("DUMMY".equalsIgnoreCase(serviceToken.getToken()))) {
			log.error("NO SERVICE TOKEN FOR " + realm + " IN CACHE");
			return;
		}

		BaseEntityUtils beUtils = new BaseEntityUtils(serviceToken,serviceToken);

		List<BaseEntity> items = beUtils.getBaseEntitys(searchBE);
			log.info("Loaded "+items.size()+" DEF baseentitys");

		defs.put(realm,new ConcurrentHashMap<String,BaseEntity>());	
			
		for (BaseEntity item : items) {
			item.setFastAttributes(true); // make fast
			
			// Now go through all the searches and see what the total is of the searches.
			// if less than or equal to dropdown size then generate the dropdown items message and save into an attribute called "DDI_<attributeCode>"
			
			
			
			defs.get(realm).put(item.getCode(),item);
			log.info("Saving ("+realm+") DEF "+item.getCode());
		}
		log.info("Saved "+items.size()+" yummy DEFs!");
	}
	
}
