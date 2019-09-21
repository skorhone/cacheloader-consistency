package net.codefreezer.test.cache.guava;

import java.util.function.Function;

import org.junit.Test;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.codefreezer.test.cache.support.AbstractConsistencyTest;

// Guava is broken
public class GuavaCacheConsistencyTest extends AbstractConsistencyTest {
	@Test(expected = AssertionError.class)
	public void shouldNotReturnEvicted() throws Exception {
		Object lock = new Object();
		
		LoadingCache<String, Integer> cache = CacheBuilder.newBuilder()
				.maximumSize(1000).initialCapacity(1000)
				.build(new TestGuavaCacheLoader(loadFunction(lock)));
		
		test(lock,
				cache::getUnchecked,
				cache::invalidate,
				cache::getIfPresent);
	}
	
	public static class TestGuavaCacheLoader extends CacheLoader<String, Integer> {
		private Function<String, Integer> loader;
		
		public TestGuavaCacheLoader(Function<String, Integer> loader) {
			this.loader = loader;
		}
		
		@Override
		public Integer load(String key) throws Exception {
			return loader.apply(key);
		}
	}
}
