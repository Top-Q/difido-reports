package il.co.topq.report.controller.resource;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

public class UploadResourceTests extends AbstractResourceTestCase {

	@Test
	public void uploadTest() {
		
		File uploadedFile = new File("C:\\large_pdf_document.pdf");
		String destinationDirRelativePath = "subDir1\\subDir2";
		String fileSavePath = client.uploadFile(uploadedFile, destinationDirRelativePath);
		assertTrue(fileSavePath.contains(destinationDirRelativePath + File.separator + uploadedFile.getName()));
	}
}
