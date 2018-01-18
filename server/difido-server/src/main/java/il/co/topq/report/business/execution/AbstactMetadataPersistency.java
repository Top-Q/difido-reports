package il.co.topq.report.business.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import il.co.topq.report.StopWatch;

@Component
public abstract class AbstactMetadataPersistency implements MetadataPersistency {

	private static final long MAX_TIME_TO_KEEP_EXECUTION_IN_SECONDS = 60 * 10;

	private final Logger logger;

	// Package private for unit testing
	private Map<Integer, ExecutionMetadata> executionsCache;

	private final AtomicInteger lastId = new AtomicInteger(0);

	protected abstract void readFromPersistency();

	protected abstract void writeToPersistency();

	public AbstactMetadataPersistency() {
		logger = LoggerFactory.getLogger(AbstactMetadataPersistency.class);
		lastId.set(getLastId());
	}

	/**
	 * Get the last execution id
	 * 
	 * @return The largest id. 0 if none exists
	 */
	private int getLastId() {
		StopWatch stopWatch = new StopWatch(logger).start("Reading from persistency");
		readFromPersistency();
		stopWatch.stopAndLog();
		if (isCachedEmpty()) {
			return 0;
		}
		return Collections.max(executionsCache.values()).getId();
	}

	@Override
	public int advanceId() {
		return lastId.incrementAndGet();
	}

	/**
	 * Will release execution object from metadata objects that have ended more
	 * then <code>MAX_TIME_TO_KEEP_EXECUTION_IN_SECONDS</code> seconds ago.<br>
	 * The reason we can't simply do it when execution is over is that there may
	 * be plugin and controllers that are using the execution object when after
	 * the execution is over. From the other hand, we can't keep them in memory
	 * since it is a memory leak.<br>
	 * This is what you get when you design stateful system
	 * issue #181
	 * 
	 */
	@Scheduled(fixedRate = 1000 * 60 * 10)
	private void scheduledReleaseOfExecutionObjects() {
		logger.debug("About to check if there is a need to free the execution object from executions");
		StopWatch stopWatch = new StopWatch(logger).start("Checking if there is a need to free execution objects");
		// @formatter:off
		final long currentTime = System.currentTimeMillis();
		executionsCache
			.values()
			.stream()
			// filter for all the executions without execution object
			.filter(metadata -> null != metadata.getExecution())
			// We will clean only execution objects of executions that are no longer active
			.filter(metadata -> !metadata.isActive())
			// And only if the last access time is longer then the maximum specified
			.filter(metadata -> (currentTime - metadata.getLastAccessedTime()) / 60 >= MAX_TIME_TO_KEEP_EXECUTION_IN_SECONDS )
			.forEach(metadata -> {
				logger.debug("Releasing execution object from execution " + metadata.getId());
				metadata.setExecution(null);
			});
		// @formatter:on
		stopWatch.stopAndLog();
	}

	@Override
	public synchronized void add(ExecutionMetadata metadata) {
		readFromPersistency();
		executionsCache.put(metadata.getId(), metadata);
		StopWatch stopWatch = new StopWatch(logger).start("Writing to persistency");
		if (metadata.isDirty()) {
			writeToPersistency();
			metadata.setDirty(false);
		}
		stopWatch.stopAndLog();
	}

	@Override
	public synchronized void remove(int id) {
		readFromPersistency();
		if (null == executionsCache.remove(id)) {
			logger.warn("Tried to delete execution with id " + id + " which is not exists");
		}
		StopWatch stopWatch = new StopWatch(logger).start("Writing to persistency");
		writeToPersistency();
		stopWatch.stopAndLog();

	}

	@Override
	public synchronized void update(ExecutionMetadata metadata) {
		add(metadata);
	}

	@Override
	public synchronized ExecutionMetadata get(int executionId) {
		readFromPersistency();
		return executionsCache.get(executionId);
	}

	@Override
	public synchronized List<ExecutionMetadata> getAll() {
		readFromPersistency();
		return new ArrayList<ExecutionMetadata>(executionsCache.values());
	}

	@Override
	public void dump() {
		initCache();
	}

	protected void initCache() {
		executionsCache = Collections.synchronizedMap(new HashMap<Integer, ExecutionMetadata>());
		lastId.set(0);
	}

	protected void populateCache(Map<Integer, ExecutionMetadata> data) {
		executionsCache = Collections.synchronizedMap(data);
		for (ExecutionMetadata meta : executionsCache.values()) {
			meta.setActive(false);
		}
	}

	protected boolean isCachedEmpty() {
		return (null == executionsCache || executionsCache.isEmpty());
	}

	protected boolean isCacheInitialized() {
		return (null != executionsCache);
	}

	protected Map<Integer, ExecutionMetadata> getExecutionsCache() {
		return executionsCache;
	}

}
