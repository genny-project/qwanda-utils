package life.genny.qwandautils;

public interface GennyCacheInterface {
	Object readCache(final String key, final String token);
	void writeCache(final String key, final String value, final String token, long totalSeconds);
	void clear();
}
