package life.genny.data;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Set;
import java.util.HashSet;

import org.jboss.logging.Logger;

import life.genny.models.GennyToken;
import life.genny.utils.VertxUtils;

/**
 * A Bridge ID management class for data message route selection.
 * 
 * @author Byron Aguirre
 * @author Jasper Robison
 */
public class BridgeSwitch {

	static final Logger log = Logger.getLogger(BridgeSwitch.class);

	public static String BRIDGE_INFO_PREFIX = "BIF";
	public static String BRIDGE_SWITCH_KEY = "ACTIVE_BRIDGE_IDS";

	public static Set<String> activeBridgeIds = new HashSet<String>();

	/**
	 * A child class used to store bridge mappings for individual users.
	 */
	public static class BridgeInfo {

		public BridgeInfo() {}

		public ConcurrentMap<String, String> mappings = new ConcurrentHashMap<>();
	}

	/**
	* Cache active Bridge Ids
	*
	* @param gennyToken
	 */
	public static void addActiveBridgeId(GennyToken gennyToken, String bridgeId) {

		String realm = gennyToken.getRealm();
		String token = gennyToken.getToken();
		Set<String> activeBridgeIds = VertxUtils.getObject(realm, "", BRIDGE_SWITCH_KEY, Set.class, token);

		if (activeBridgeIds == null) {
			activeBridgeIds = new HashSet<String>();
		}

		activeBridgeIds.add(bridgeId);

		VertxUtils.putObject(realm, "", BRIDGE_SWITCH_KEY, activeBridgeIds);
	}

	/**
	* Find an active bridge ID
	*
	* @param gennyToken Used to find the realm
	* @return String An active Bridge ID
	 */
	public static String findActiveBridgeId(GennyToken gennyToken) {

		String realm = gennyToken.getRealm();
		String token = gennyToken.getToken();

		Set<String> activeBridgeIds = VertxUtils.getObject(realm, "", BRIDGE_SWITCH_KEY, Set.class, token);

		if (activeBridgeIds.iterator().hasNext()) {
			return activeBridgeIds.iterator().next();
		}

		return null;
	}

	/**
	* Put an entry into the users BridgeInfo item in the cache
	*
	* @param gennyToken The users GennyToken
	* @param bridgeId The ID of the bridge used in communication
	 */
	public static void put(GennyToken gennyToken, String bridgeId) {

		String realm = gennyToken.getRealm();
		String token = gennyToken.getToken();
		String key = BRIDGE_INFO_PREFIX + "_" + gennyToken.getUserCode();
		
		// grab from cache or create if null
		// BridgeInfo info = CacheUtils.getObject(realm, key, BridgeInfo.class);
		BridgeInfo info = VertxUtils.getObject(realm, "", key, BridgeInfo.class, token);
		
		if (info == null) {
			info = new BridgeInfo();
		}

		// add entry for jti and update in cache
		String jti = gennyToken.getUniqueId();
		info.mappings.put(jti, bridgeId);

		// CacheUtils.putObject(realm, key, info);
		VertxUtils.putObject(realm, "", key, info);
	}

	/**
	* Get the corresponding bridgeId from the users BridgeInfo 
	* object in the cache.
	*
	* @param gennyToken The users GennyToken
	* @return String The corresponding bridgeId
	 */
	public static String get(GennyToken gennyToken) {

		String realm = gennyToken.getRealm();
		String token = gennyToken.getToken();
		String key = BRIDGE_INFO_PREFIX + "_" + gennyToken.getUserCode();
		
		// grab from cache
		BridgeInfo info = VertxUtils.getObject(realm, "", key, BridgeInfo.class, token);
		
		if (info == null) {
			log.error("No BridgeInfo object found for user " + gennyToken.getUserCode());
			return null;
		}

		// grab entry for jti
		String jti = gennyToken.getUniqueId();
		String bridgeId = info.mappings.get(jti);

		return bridgeId;
	}
}
