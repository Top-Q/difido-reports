package il.co.topq.report.controller.resource;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.execution.MachineNode;
import il.co.topq.difido.model.execution.Node;
import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.difido.model.execution.TestNode;
import il.co.topq.report.controller.listener.ListenersManager;
import il.co.topq.report.model.Session;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/executions/{execution}/machines/{machine}/scenarios/{scenario}/tests")
public class TestResource {

	/**
	 * Add a new test
	 * 
	 * @param executionId
	 * @param machineId
	 * @param scenarioId
	 * @param test
	 * @return
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public int post(@PathParam("execution") int executionId, @PathParam("machine") int machineId,
			@PathParam("scenario") int scenarioId, TestNode test) {
		ScenarioNode scenario = Session.INSTANCE.getExecution(executionId).getMachines().get(machineId)
				.getAllScenarios().get(scenarioId);
		test.setIndex(Session.INSTANCE.incrementAndGetTestIndex());
		scenario.addChild(test);
		ListenersManager.INSTANCE.notifyTestAdded(test);
		return scenario.getChildren().indexOf(test);
	}

	/**
	 * Updates a test. Will be used mostly for updating the test status.
	 * 
	 * @param executionId
	 * @param machineId
	 * @param scenarioId
	 * @param testId
	 * @param aTest
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/{test}")
	public void put(@PathParam("execution") int executionId, @PathParam("machine") int machineId,
			@PathParam("scenario") int scenarioId, @PathParam("test") int testId, TestNode aTest) {
		if (null == aTest) {
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
		if (aTest.getDuration() > 0) {
			test.setDuration(aTest.getDuration());
		}
		if (aTest.getTimestamp() != null && !aTest.getTimestamp().isEmpty()) {
			test.setTimestamp(aTest.getTimestamp());
		}
		if (aTest.getStatus() != null) {
			test.setStatus(aTest.getStatus());
		}
	}

	/**
	 * Signals that this specific test was ended.
	 * 
	 * @param executionId
	 * @param machineId
	 * @param scenarioId
	 * @param testId
	 * @param aTest
	 */
	@DELETE
	@Path("/{test}")
	public void delete(@PathParam("execution") int executionId, @PathParam("machine") int machineId,
			@PathParam("scenario") int scenarioId, @PathParam("test") int testId) {
		final Execution execution = Session.INSTANCE.getExecution(executionId);
		final MachineNode machine = execution.getMachines().get(machineId);
		final ScenarioNode scenario = machine.getAllScenarios().get(scenarioId);
		final Node node = scenario.getChildren().get(testId);
		if (!(node instanceof TestNode)) {
			// TODO: return error
		}
		ListenersManager.INSTANCE.notifyTestEnded((TestNode) node);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{test}")
	public TestNode get(@PathParam("execution") int executionId, @PathParam("machine") int machineId,
			@PathParam("scenario") int scenarioId, @PathParam("test") int testId) {
		final Execution execution = Session.INSTANCE.getExecution(executionId);
		final MachineNode machine = execution.getMachines().get(machineId);
		final ScenarioNode scenario = machine.getAllScenarios().get(scenarioId);
		final Node node = scenario.getChildren().get(testId);
		if (!(node instanceof TestNode)) {
			// TODO: return error
		}
		final TestNode test = (TestNode) node;
		TestNode testCopy = TestNode.newInstance((TestNode) test);
		testCopy.setParent(null);
		return testCopy;
	}

}
