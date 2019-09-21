package net.codefreezer.test.cache.ehcache3;

import java.util.function.Function;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.spi.loaderwriter.CacheLoaderWriter;
import org.junit.Test;

import net.codefreezer.test.cache.support.AbstractConsistencyTest;

public class EHCacheConsistencyTest extends AbstractConsistencyTest {
	@Test
	public void shouldNotReturnEvicted() throws Exception {
		Object lock = new Object();
		CacheManager manager = CacheManagerBuilder.newCacheManagerBuilder()
				.build(true);
		Cache<String, Integer> cache = manager.createCache("test",
				CacheConfigurationBuilder.newCacheConfigurationBuilder(String.class, Integer.class, ResourcePoolsBuilder.heap(10))
					.withLoaderWriter(new TestLoaderWriter(loadFunction(lock))));
		try {
			test(lock,
					cache::get,
					cache::remove,
					(k) -> cache.containsKey(k) ? cache.get(k) : null);
		} finally {
			manager.close();
		}
	}
	
	public static class TestLoaderWriter implements CacheLoaderWriter<String, Integer> {
		private Function<String, Integer> loader;

		public TestLoaderWriter(Function<String, Integer> loader) {
			this.loader = loader;
		}

		@Override
		public Integer load(String key) throws Exception {
			return loader.apply(key);
		}

		@Override
		public void write(String key, Integer value) throws Exception {
			throw new UnsupportedOperationException();
		}

		@Override
		public void delete(String key) throws Exception {
		}
	}
}
