package il.co.topq.report.controller.resource;

import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.controller.listener.ListenersManager;
import il.co.topq.report.view.HtmlViewGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

@Path("/executions/{execution}/details")
public class TestDetailsResource {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void post(@PathParam("execution") int executionId, TestDetails details) {
		if (null == details) {
			// TODO: return error;
		}

		ListenersManager.INSTANCE.notifyTestDetailsAdded(details);
	}

	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Path("/{uid}/file")
	public void postFile(@PathParam("execution") int executionId, @PathParam("uid") int uid, FormDataMultiPart multiPart) {

		FormDataBodyPart fileBodyPart = multiPart.getFields().values().iterator().next().get(0);

		InputStream fileStream = fileBodyPart.getValueAs(InputStream.class);
		FormDataContentDisposition fileDisposition = fileBodyPart.getFormDataContentDisposition();
		String fileName = fileDisposition.getFileName();

		File executionDestinationFolder = HtmlViewGenerator.getInstance().getExecutionDestinationFolder();

		String destinationDirPath = executionDestinationFolder + File.separator + "tests" + File.separator + "test_"
				+ uid;

		File destinationDir = new File(destinationDirPath);
		if (!destinationDir.exists()) {
			destinationDir.mkdirs();
		}

		String fileSavePath = destinationDirPath + File.separator + fileName;
		saveFile(fileStream, fileSavePath);

	}

	private void saveFile(InputStream inputStream, String filePath) {

		try {
			OutputStream outputStream = new FileOutputStream(filePath);
			IOUtils.copy(inputStream, outputStream);
			outputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
