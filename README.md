# cacheloader-consistency-test
This is a test suite for validating, if cache provider implements strong consistency on
concurrent actions.

Use of provider that does not provide strong consistency, may result to stale data in cache.

Currently this test suite contains tests for eviction

## Providers in suite
Test suite contains tests for following providers:

* EhCache2 (fail)
* EhCache3 (ok)
* Guava (fail)
* Coffeine (ok)

## Test algorithm: eviction
Test algorithm is implemented using three threads (T1, T2, T3).

When cache provider blocks (uses locks) on eviction, flow is:

1. T1: Wait for Get awesome-key
2. T2: Get awesome-key
3. T2: Loading awesome-key...
4. T2: Wait for notification
5. T1: Wait for eviction
6. T3: Evicting awesome-key
7. T1: Evict seems to be blocked by pending load
8. T1: Notify loader
9. T1: Wait for eviction
10. T2: Loaded awesome-key
11. T1: Evicted awesome-key
12. T1: Validate get value
13. T1: Validate cache state

If cache provider does not block on eviction, flow is:

1. T1: Wait for Get awesome-key
2. T2: Get awesome-key
3. T2: Loading awesome-key...
4. T2: Wait for notification
5. T1: Wait for eviction
6. T3: Evicting awesome-key
7. T1: Evicted awesome-key
8. T1: Notify loader
9. T1: Validate get value
10. T2: Loaded awesome-key
11. T1: Validate cache state

Unfortunately currently all providers that do not block, fail the test. Passing non-blocking
implementation requires cache provider to implement eventual consistency based on timestamps
(or counters). Updates during loading should *always* cause loaded data to be discarded from
cache.
