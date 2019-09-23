package net.codefreezer.test.cache.ehcache2;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.junit.Test;

import net.codefreezer.test.cache.support.AbstractConsistencyTest;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Status;
import net.sf.ehcache.loader.CacheLoader;

public class EHCacheConsistencyTest extends AbstractConsistencyTest {
	// We expect EHCache 2.x to be broken :(
	//
	// EHCache is usually used as a distributed cache, so there's nothing
	// we can do to fix it
	@Test(expected = AssertionError.class)
	public void shouldNotReturn() throws Exception {
		Object lock = new Object();
		CacheManager manager = CacheManager.create();
		try {
			Ehcache cache = manager.addCacheIfAbsent("test");
			CacheLoader loader = new TestEhCacheLoader(loadFunction(lock));
			
			test(lock,
					(k) -> (Integer)cache.getWithLoader(k, loader, "x").getObjectValue(),
					(k) -> cache.remove(k),
					(k) -> Optional.ofNullable(cache.get(k)).map(e -> (Integer)e.getObjectValue()).orElse(null));
		} finally {
			manager.shutdown();
		}
	}

	public static class TestEhCacheLoader implements CacheLoader {
		private Function<String, Integer> loader;

		public TestEhCacheLoader(Function<String, Integer> loader) {
			this.loader = loader;
		}

		@Override
		public Object load(Object key) throws CacheException {
			throw new UnsupportedOperationException();
		}

		@Override
		public Map loadAll(Collection keys) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object load(Object key, Object argument) {
			return loader.apply((String) key);
		}

		@Override
		public Map loadAll(Collection keys, Object argument) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getName() {
			throw new UnsupportedOperationException();
		}

		@Override
		public CacheLoader clone(Ehcache cache) throws CloneNotSupportedException {
			throw new UnsupportedOperationException();
		}

		@Override
		public void init() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void dispose() throws CacheException {
			throw new UnsupportedOperationException();
		}

		@Override
		public Status getStatus() {
			return Status.STATUS_ALIVE;
		}
	}
}
