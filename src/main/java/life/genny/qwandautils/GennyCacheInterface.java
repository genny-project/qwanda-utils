package life.genny.qwandautils;

public interface GennyCacheInterface {
	public Object readCache(final String key, final String token);
	public void writeCache(final String key, final String value, final String token, long ttl_seconds);
	public void clear();
}
