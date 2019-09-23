package net.codefreezer.test.cache.hazelcast;

import java.util.Map;
import java.util.function.Function;

import javax.cache.integration.CacheLoader;
import javax.cache.integration.CacheLoaderException;

public class TestHazelcastCacheLoader implements CacheLoader<String, Integer> {
	// This is a quick hack
	public static Function<String, Integer> loader;
	
	@Override
	public Integer load(String key) {
		return loader.apply(key);
	}

	@Override
	public Map<String, Integer> loadAll(Iterable<? extends String> keys) throws CacheLoaderException {
		throw new UnsupportedOperationException();
	}
}
