package il.co.topq.report.business.execution;

public interface MetadataProvider {

	ExecutionMetadata getMetadata(int executionId);

	ExecutionMetadata[] getAllMetaData();
	
	/**
	 * Get the first, active shared execution
	 * @return shared execution metadata or null if none was found
	 */
	ExecutionMetadata getShared();


}
