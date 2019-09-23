package net.codefreezer.test.cache.guava;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.Test;

import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.codefreezer.test.cache.support.AbstractConsistencyTest;

public class GuavaCacheConsistencyTest extends AbstractConsistencyTest {
	// We expect Guava to be broken
	@Test(expected = AssertionError.class)
	public void shouldReturnEvicted() throws Exception {
		Object lock = new Object();
		
		LoadingCache<String, Integer> cache = CacheBuilder.newBuilder()
				.maximumSize(1000).initialCapacity(1000)
				.build(new GuavaCacheLoader(loadFunction(lock)));
		
		test(lock,
				cache::getUnchecked,
				cache::invalidate,
				cache::getIfPresent);
	}
	
	// We expect following work-around to fix this issue
	//
	// Work-around is to cache memoizes instead of caching direct
	// values. Do not try this 'fix' on distributed caches - it won't work
	@Test
	public void shouldNotReturnEvicted() throws Exception {
		Object lock = new Object();
		
		LoadingCache<String, Supplier<Integer>> cache = CacheBuilder.newBuilder()
				.maximumSize(1000).initialCapacity(1000)
				.build(new GuavaMemoizeCacheLoader(loadFunction(lock)));
		
		test(lock,
				(k) -> cache.getUnchecked(k).get(),
				cache::invalidate,
				(k) -> Optional.ofNullable(cache.getIfPresent(k)).map(Supplier::get).orElse(null));
	}
	
	public static class GuavaCacheLoader extends CacheLoader<String, Integer> {
		private Function<String, Integer> loader;
		
		public GuavaCacheLoader(Function<String, Integer> loader) {
			this.loader = loader;
		}
		
		@Override
		public Integer load(String key) throws Exception {
			return loader.apply(key);
		}
	}
	
	public static class GuavaMemoizeCacheLoader extends CacheLoader<String, Supplier<Integer>> {
		private Function<String, Integer> loader;
		
		public GuavaMemoizeCacheLoader(Function<String, Integer> loader) {
			this.loader = loader;
		}
		
		@Override
		public Supplier<Integer> load(String key) throws Exception {
			return Suppliers.memoize(() -> loader.apply(key));
		}
	}
}