package net.codefreezer.test.cache.support;

import static org.junit.Assert.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

public class AbstractConsistencyTest {
	public void test(
			Object lock,
			Function<String, Integer> getFunction,
			Consumer<String> evictFunction,
			Function<String, Integer> getIfPresentFunction) throws Exception {
		
		String key = "awesome-key";
		Thread.currentThread().setName("T1");
		ThreadFactory tf = new ThreadFactory() {
			int i = 2;
			
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setName("T" + i++);
				return t;
			}
		};
		ExecutorService es = Executors.newFixedThreadPool(2, tf);
		
		try {
			Future<Integer> getFuture;
			synchronized(lock) {
				log("Wait for Get " + key);
				getFuture = es.submit(() -> {
					log("Get " + key);
					return getFunction.apply(key);
				});
				lock.wait();
			}
			Future<?> evictFuture = es.submit(() -> {
				log("Evicting " + key);
				evictFunction.accept(key);
			});
			try {
				log("Wait for eviction");
				evictFuture.get(5, TimeUnit.SECONDS);
				log("Evicted " + key);
				synchronized(lock) {
					log("Notify loader");
					lock.notify();
				}
			} catch (TimeoutException e) {
				log("Evict seems to be blocked by pending load");
				synchronized(lock) {
					log("Notify loader");
					lock.notify();
				}
				log("Wait for eviction");
				evictFuture.get();
				log("Evicted " + key);
			}
			log("Validate get value");
			assertNotNull(getFuture.get());
			log("Validate cache state");
			assertNull(getIfPresentFunction.apply(key));
		} finally {
			es.shutdown();
		}
	}
	
	private void log(String text) {
		System.out.println(Thread.currentThread().getName() + ": " + text);
	}
	
	public Function<String, Integer> loadFunction(Object lock) {
		return (s) -> {
			synchronized (lock) {
				lock.notify();
				log("Loading "  + s + "...");
			}
			synchronized (lock) {
				try {
					log("Wait for notification");
					lock.wait();
				} catch (Exception exception) {
					throw new RuntimeException(exception);
				}
				log("Loaded " + s);
			}
			return 1;
		};
	}
}
