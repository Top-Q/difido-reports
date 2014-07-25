package il.co.topq.report.controller.resource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

@Path("upload")
public class UploadResource {

	private static final String uploadedFilesDirPath = "C:\\jersey_uploads";
	
	@POST
	@Path("/file")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces(MediaType.TEXT_PLAIN)
	public String uploadMultipart(FormDataMultiPart multiPart) throws IOException {
		
		String destinationDirRelativePath = multiPart.getField("destinationDirRelativePath").getEntityAs(String.class);
		
		FormDataBodyPart fileBodyPart = multiPart.getField("file");
		
		InputStream fileStream = fileBodyPart.getValueAs(InputStream.class);
		FormDataContentDisposition fileDisposition = fileBodyPart.getFormDataContentDisposition();
		String fileName = fileDisposition.getFileName();
		
		String destinationDirPath = uploadedFilesDirPath + File.separator + destinationDirRelativePath;

		File destinationDir = new File(destinationDirPath);
		if (!destinationDir.exists()) {
			destinationDir.mkdirs();
		}
		
		String fileSavePath = destinationDirPath + File.separator + fileName;
		saveFile(fileStream, fileSavePath);
		
		return fileSavePath;
	}
	
	private void saveFile(InputStream inputStream, String filePath) throws IOException {
		OutputStream outputStream = new FileOutputStream(filePath);
		IOUtils.copy(inputStream, outputStream);
		outputStream.close();
	}
}
