package net.codefreezer.test.cache.hazelcast;

import org.junit.Test;

import com.hazelcast.cache.ICache;
import com.hazelcast.config.CacheSimpleConfig;
import com.hazelcast.config.Config;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

import net.codefreezer.test.cache.support.AbstractConsistencyTest;

public class HazelcastCacheConsistencyTest extends AbstractConsistencyTest {
	@Test
	public void shouldNotReturnEvicted() throws Exception {
		Object lock = new Object();
		
		CacheSimpleConfig cacheConfig = new CacheSimpleConfig()
				.setReadThrough(true)
				.setCacheLoaderFactory(TestHazelcastCacheLoaderFactory.class.getName())
				.setBackupCount(0)
				.setName("test");
	    Config cfg = new Config();
	    cfg.addCacheConfig(cacheConfig);
	    
	    TestHazelcastCacheLoader.loader = loadFunction(lock);
	    
	    HazelcastInstance instance = Hazelcast.newHazelcastInstance(cfg);
	    try {
		    ICache<String, Integer> cache = instance.getCacheManager().getCache("test");
			
			test(lock,
					cache::get,
					cache::remove,
					(k) -> cache.containsKey(k) ? cache.get(k) : null);
	    } finally {
	    	instance.shutdown();
	    }
	}
}
