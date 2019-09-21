package net.codefreezer.test.cache.caffeine;

import java.util.function.Function;

import org.junit.Test;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;

import net.codefreezer.test.cache.support.AbstractConsistencyTest;

public class CaffeineCacheConsistencyTest extends AbstractConsistencyTest {
	@Test
	public void shouldNotReturnEvicted() throws Exception {
		Object lock = new Object();
		
		LoadingCache<String, Integer> cache = Caffeine.newBuilder()
				.maximumSize(1000).initialCapacity(1000)
				.build(new TestCaffeineCacheLoader(loadFunction(lock)));
		
		test(lock,
				cache::get,
				cache::invalidate,
				cache::getIfPresent);
	}
	
	public static class TestCaffeineCacheLoader implements CacheLoader<String, Integer> {
		private Function<String, Integer> loader;
		
		public TestCaffeineCacheLoader(Function<String, Integer> loader) {
			this.loader = loader;
		}
		
		@Override
		public Integer load(String key) {
			return loader.apply(key);
		}
	}
}
