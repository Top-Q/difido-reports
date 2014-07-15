package il.co.topq.report.resource;

import il.co.topq.difido.model.execution.ScenarioNode;
import il.co.topq.report.listener.ListenersManager;
import il.co.topq.report.model.Session;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/executions/{execution}/machines/{machine}/scenarios")
public class ScenarioResource {

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public int post(@PathParam("execution") int executionId, @PathParam("machine") int machineId, ScenarioNode scenario) {
		Session.INSTANCE.getExecution(executionId).getMachines().get(machineId).addChild(scenario);
		ListenersManager.INSTANCE.notifyScenarioAdded(scenario);
		return Session.INSTANCE.getExecution(executionId).getMachines().get(machineId).getChildren().indexOf(scenario);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	@Path("/{scenario}")
	public int post(@PathParam("execution") int executionId, @PathParam("machine") int machineId,
			@PathParam("scenario") int parentScenarioId, ScenarioNode scenario) {
		final ScenarioNode parentScenario = Session.INSTANCE.getExecution(executionId).getMachines().get(machineId)
				.getAllScenarios().get(parentScenarioId);
		parentScenario.addChild(scenario);
		ListenersManager.INSTANCE.notifyScenarioAdded(scenario);
		return Session.INSTANCE.getExecution(executionId).getMachines().get(machineId).getAllScenarios()
				.indexOf(scenario);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{scenario}")
	public ScenarioNode get(@PathParam("execution") int executionId, @PathParam("machine") int machineId,
			@PathParam("scenario") int scenarioId) {
		final ScenarioNode scenario = Session.INSTANCE.getExecution(executionId).getMachines().get(machineId)
				.getAllScenarios().get(scenarioId);
		final ScenarioNode scenarioClone = ScenarioNode.newInstance(scenario);
		// The parent is causing problems when serializing the object
		scenarioClone.setParent(null);
		scenarioClone.setChildren(null);
		return scenarioClone;
	}
}
