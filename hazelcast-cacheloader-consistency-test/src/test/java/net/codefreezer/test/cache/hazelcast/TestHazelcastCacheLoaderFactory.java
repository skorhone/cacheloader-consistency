package net.codefreezer.test.cache.hazelcast;

import javax.cache.configuration.Factory;

public class TestHazelcastCacheLoaderFactory implements Factory<TestHazelcastCacheLoader> {
	private static final long serialVersionUID = 8789365606349020887L;

	@Override
	public TestHazelcastCacheLoader create() {
		return new TestHazelcastCacheLoader();
	}
}
