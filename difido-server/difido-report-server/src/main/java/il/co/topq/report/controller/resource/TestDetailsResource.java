package il.co.topq.report.controller.resource;

import il.co.topq.difido.PersistenceUtils;
import il.co.topq.difido.model.Enums.ElementType;
import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.Node;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.test.ReportElement;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.controller.listener.ListenersManager;
import il.co.topq.report.model.Session;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.commons.io.IOUtils;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;

@Path("/executions/{execution}/machines/{machine}/scenarios/{scenario}/tests/{test}/details")
public class TestDetailsResource {

	private static final String uploadedFilesDirPath = "C:\\difido_uploads";
	
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public void post(@PathParam("execution") int executionId, @PathParam("machine") int machineId,
			@PathParam("scenario") int scenarioId, @PathParam("test") int testId, TestDetails details) {
		if (null == details) {
			// TODO: return error;
		}
		final Execution execution = Session.INSTANCE.getExecution(executionId);
		final MachineNode machine = execution.getMachines().get(machineId);
		final ScenarioNode scenario = machine.getAllScenarios().get(scenarioId);
		final Node node = scenario.getChildren().get(testId);
		if (!(node instanceof TestNode)) {
			// TODO: return error
		}
		final TestNode test = (TestNode) node;
		Session.INSTANCE.addTestDetails(test, details);
		ListenersManager.INSTANCE.notifyTestDetailsAdded(test, details);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public TestDetails get(@PathParam("execution") int executionId, @PathParam("machine") int machineId,
			@PathParam("scenario") int scenarioId, @PathParam("test") int testId) {
		final Execution execution = Session.INSTANCE.getExecution(executionId);
		final MachineNode machine = execution.getMachines().get(machineId);
		final ScenarioNode scenario = machine.getAllScenarios().get(scenarioId);
		final Node node = scenario.getChildren().get(testId);
		if (!(node instanceof TestNode)) {
			// TODO: return error
		}
		final TestNode test = (TestNode) node;
		return Session.INSTANCE.getTestDetails(test);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/element")
	public void postElement(@PathParam("execution") int executionId, @PathParam("machine") int machineId,
			@PathParam("scenario") int scenarioId, @PathParam("test") int testId, ReportElement element) {
		if (null == element) {
			// TODO: return error;
		}
		final Execution execution = Session.INSTANCE.getExecution(executionId);
		final MachineNode machine = execution.getMachines().get(machineId);
		final ScenarioNode scenario = machine.getAllScenarios().get(scenarioId);
		final Node node = scenario.getChildren().get(testId);
		if (!(node instanceof TestNode)) {
			// TODO: return error
		}
		final TestNode test = (TestNode) node;
		final TestDetails details = Session.INSTANCE.getTestDetails(test);
		if (null == details) {
			// TODO: return error
		}
		details.addReportElement(element);
		ListenersManager.INSTANCE.notifyReportElementAdded(test, element);
	}
	
	@POST
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Path("/file")
	public void postFile(@PathParam("execution") int executionId, @PathParam("machine") int machineId,
			@PathParam("scenario") int scenarioId, @PathParam("test") int testId, FormDataMultiPart multiPart) {
		
		final Execution execution = Session.INSTANCE.getExecution(executionId);
		final MachineNode machine = execution.getMachines().get(machineId);
		final ScenarioNode scenario = machine.getAllScenarios().get(scenarioId);
		final Node node = scenario.getChildren().get(testId);
		if (!(node instanceof TestNode)) {
			// TODO: return error
		}
		final TestNode test = (TestNode) node;
		final TestDetails details = Session.INSTANCE.getTestDetails(test);
		if (null == details) {
			// TODO: return error
		}
		
		FormDataBodyPart fileBodyPart = multiPart.getField("file");
		
		InputStream fileStream = fileBodyPart.getValueAs(InputStream.class);
		FormDataContentDisposition fileDisposition = fileBodyPart.getFormDataContentDisposition();
		String fileName = fileDisposition.getFileName();
		
		//TODO!
//		PersistenceUtils.writeTest(details, executionDestinationFolder, new File(executionDestinationFolder, "tests"
//				+ File.separator + "test_" + test.getIndex()));
		
		
		String destinationDirPath = uploadedFilesDirPath + File.separator +
				"execution_" + executionId + File.separator +
				"machine_" + machineId + File.separator + 
				"scenario_" + scenarioId + File.separator +
				"test_" + testId;

		File destinationDir = new File(destinationDirPath);
		if (!destinationDir.exists()) {
			destinationDir.mkdirs();
		}
		
		String fileSavePath = destinationDirPath + File.separator + fileName;
		saveFile(fileStream, fileSavePath);
		
		ReportElement element = new ReportElement();
		element.setType(ElementType.lnk);
		element.setTitle(fileName);
		element.setMessage(fileSavePath);
		
		details.addReportElement(element);
		ListenersManager.INSTANCE.notifyReportElementAdded(test, element);
	}

	private void saveFile(InputStream inputStream, String filePath) {
		
		try {
			OutputStream outputStream = new FileOutputStream(filePath);
			IOUtils.copy(inputStream, outputStream);
			outputStream.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// @GET
	// @Path("/element")
	// @Produces(MediaType.APPLICATION_JSON)
	// public ReportElement[] getElement(@PathParam("execution") int
	// executionId, @PathParam("machine") int machineId,
	// @PathParam("scenario") int scenarioId, @PathParam("test") int testId) {
	// final Execution execution = Session.INSTANCE.getExecution(executionId);
	// final MachineNode machine = execution.getMachines().get(machineId);
	// final ScenarioNode scenario = machine.getAllScenarios().get(scenarioId);
	// final Node node = scenario.getChildren().get(testId);
	// if (!(node instanceof TestNode)) {
	// // TODO: return error
	// }
	// final TestNode test = (TestNode) node;
	// if (null == test.getDetails()) {
	// // TODO: return error
	// }
	// final ReportElement[] elements = test.getDetails().getReportElements()
	// .toArray(new
	// ReportElement[test.getDetails().getReportElements().size()]);
	// return elements;
	// }

}
