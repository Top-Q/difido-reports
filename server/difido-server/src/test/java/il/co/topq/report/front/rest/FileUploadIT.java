package il.co.topq.report.front.rest;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.remote.ExecutionDetails;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.business.execution.AbstractResourceTest;

public class FileUploadIT extends AbstractResourceTest {

	private static final int FILE_SIZE_IN_MB = 512;
	private int executionId;
	private String uid;
	private TestDetails details;
	private File tempFile;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		final ExecutionDetails description = new ExecutionDetails("Testing", true);
		executionId = client.addExecution(description);
		final MachineNode machine = new MachineNode("myMachine");
		final int machineId = client.addMachine(executionId, machine);
		final ScenarioNode scenario = new ScenarioNode("mySceanrio");
		machine.addChild(scenario);
		final TestNode test = new TestNode(0, "myTest", "0");
		uid = String.valueOf(Math.abs(new Random().nextInt()));
		test.setUid(uid);
		scenario.addChild(test);
		client.updateMachine(executionId, machineId, machine);
		details = new TestDetails(uid);
		client.addTestDetails(executionId, details);

	}

	@Test
	public void uploadLargeFile() throws Exception {
		String fileName = "test_file.tmp";
		tempFile = new File(System.getProperty("java.io.tmpdir"), fileName);
		writeContentToFile(tempFile, FILE_SIZE_IN_MB);
		long expectedFileSize = tempFile.length();
		client.addFileFromFileSystem(executionId, uid, tempFile);
		waitForTasksToFinish();
		File uploadedFiles[] = findFilesInTest(uid, fileName);
		Assert.assertEquals(1, uploadedFiles.length);
		Assert.assertNotNull(uploadedFiles[0]);
		Assert.assertEquals(expectedFileSize, uploadedFiles[0].length());
	}

	private void writeContentToFile(File file, int sizeInMB) throws IOException {
		try (FileWriter writer = new FileWriter(file)) {
			String content = "foo bar foo bar foo bar foo bar foo bar foo bar foo bar foo bar foo bar\n ";
			for (int length = 0; length <= sizeInMB * 1e+6; length += content.length()) {
				writer.write(content);
			}
		}
	}

	@After
	public void tearDown() {
		try {
			client.endExecution(executionId);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (tempFile != null && tempFile.exists()) {
			tempFile.delete();
		}
	}

}
