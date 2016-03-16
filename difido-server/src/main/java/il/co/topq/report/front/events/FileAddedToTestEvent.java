package il.co.topq.report.front.events;

import il.co.topq.report.business.execution.ExecutionManager.ExecutionMetadata;

public class FileAddedToTestEvent extends AbsMetadataEvent {

	private final int executionId;
	private final String testUid;
	private final byte[] fileContent;
	private final String fileName;

	public FileAddedToTestEvent(int executionId, ExecutionMetadata executionMetaData, String testUid,
			byte[] fileContent, String fileName) {
		super(executionMetaData);
		this.executionId = executionId;
		this.testUid = testUid;
		this.fileContent = fileContent;
		this.fileName = fileName;
	}

	public int getExecutionId() {
		return executionId;
	}

	public String getTestUid() {
		return testUid;
	}

	public byte[] getFileContent() {
		return fileContent;
	}

	public String getFileName() {
		return fileName;
	}

}
