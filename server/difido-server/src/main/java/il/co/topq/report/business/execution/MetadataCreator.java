package il.co.topq.report.business.execution;

import il.co.topq.difido.model.remote.ExecutionDetails;

public interface MetadataCreator {
	
	ExecutionMetadata createMetadata(ExecutionDetails executionDetails);
}
