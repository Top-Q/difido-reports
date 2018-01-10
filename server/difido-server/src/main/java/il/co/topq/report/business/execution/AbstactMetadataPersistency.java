package il.co.topq.report.business.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import il.co.topq.report.StopWatch;

public abstract class AbstactMetadataPersistency implements MetadataPersistency {

	private final Logger logger;

	// Package private for unit testing
	private Map<Integer, ExecutionMetadata> executionsCache;

	private int lastId;

	protected abstract void readFromPersistency();

	protected abstract void writeToPersistency();

	public AbstactMetadataPersistency() {
		logger = LoggerFactory.getLogger(AbstactMetadataPersistency.class);
		lastId = getLastId();
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
		final List<ExecutionMetadata> result = new ArrayList<ExecutionMetadata>();
		result.addAll(executionsCache.values());
		Collections.sort(result);
		return result.get(0).getId();
	}

	@Override
	public int advanceId() {
		return ++lastId;
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
		final List<ExecutionMetadata> result = new ArrayList<ExecutionMetadata>();
		result.addAll(executionsCache.values());
		// This synchronized is very important. See issue #81
		synchronized (this) {
			Collections.sort(result);
		}
		return result;

	}

	@Override
	public void dump() {
		initCache();
	}

	protected void initCache() {
		executionsCache = Collections.synchronizedMap(new HashMap<Integer, ExecutionMetadata>());
		lastId = 0;
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
