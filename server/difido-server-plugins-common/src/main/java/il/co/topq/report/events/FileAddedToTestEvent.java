package il.co.topq.report.events;

public class FileAddedToTestEvent extends AbsMetadataEvent {

	private final String testUid;
	private final byte[] fileContent;
	private final String fileName;

	public FileAddedToTestEvent(int executionId, String testUid, byte[] fileContent, String fileName) {
		super(executionId);
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
