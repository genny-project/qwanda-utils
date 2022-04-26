package life.genny.qwandautils;

import life.genny.models.GennyToken;

public interface GennyCacheInterface {
	public Object readCache(final String realm, final String key, final GennyToken token);
	public void writeCache(final String realm, final String key, final String value, final GennyToken token, long ttl_seconds);
	public void clear(final String realm);
}
