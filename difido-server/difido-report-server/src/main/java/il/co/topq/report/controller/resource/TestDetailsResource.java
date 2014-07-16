package il.co.topq.report.controller.resource;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.Node;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.difido.model.test.ReportElement;
import il.co.topq.difido.model.test.TestDetails;
import il.co.topq.report.controller.listener.ListenersManager;
import il.co.topq.report.model.Session;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/executions/{execution}/machines/{machine}/scenarios/{scenario}/tests/{test}/details")
public class TestDetailsResource {

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
