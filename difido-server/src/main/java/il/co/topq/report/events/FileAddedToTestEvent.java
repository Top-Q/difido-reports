package il.co.topq.report.events;

import il.co.topq.report.business.execution.ExecutionMetadata;

public class FileAddedToTestEvent extends AbsMetadataEvent {

	private final String testUid;
	private final byte[] fileContent;
	private final String fileName;

	public FileAddedToTestEvent(ExecutionMetadata executionMetaData, String testUid, byte[] fileContent,
			String fileName) {
		super(executionMetaData);
		this.testUid = testUid;
		this.fileContent = fileContent;
		this.fileName = fileName;
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
