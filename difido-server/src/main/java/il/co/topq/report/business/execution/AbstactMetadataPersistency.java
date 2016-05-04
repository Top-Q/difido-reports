package il.co.topq.report.business.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstactMetadataPersistency implements MetadataPersistency {

	private final Logger log = LoggerFactory.getLogger(AbstactMetadataPersistency.class);

	// Package private for unit testing
	private Map<Integer, ExecutionMetadata> executionsCache;;

	private int lastId;

	protected abstract void readFromPersistency();

	protected abstract void writeToPersistency();

	public AbstactMetadataPersistency() {
		lastId = getLastId();
	}

	/**
	 * Get the last execution id
	 * 
	 * @return The largest id. 0 if none exists
	 */
	private int getLastId() {
		readFromPersistency();
		if (isCachedEmpty()) {
			return 0;
		}
		final List<ExecutionMetadata> result = new ArrayList<ExecutionMetadata>();
		result.addAll(executionsCache.values());
		Collections.sort(result);
		return result.get(0).getId();
	}

	public int advanceId() {
		return ++lastId;
	}

	public void add(ExecutionMetadata metadata) {
		readFromPersistency();
		executionsCache.put(metadata.getId(), metadata);
		writeToPersistency();
	}

	public void remove(int id) {
		readFromPersistency();
		if (null == executionsCache.remove(id)) {
			log.warn("Tried to delete execution with id " + id + " which is not exists");
		}
		writeToPersistency();
	}

	public void update(ExecutionMetadata metadata) {
		add(metadata);
	}

	public ExecutionMetadata get(int executionId) {
		readFromPersistency();
		return executionsCache.get(executionId);
	}

	public List<ExecutionMetadata> getAll() {
		readFromPersistency();
		final List<ExecutionMetadata> result = new ArrayList<ExecutionMetadata>();
		result.addAll(executionsCache.values());
		Collections.sort(result);
		return result;

	}

	public void dump() {
		initCache();
	}

	protected void initCache() {
		executionsCache = Collections.synchronizedMap(new HashMap<Integer, ExecutionMetadata>());
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
