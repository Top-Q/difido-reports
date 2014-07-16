package il.co.topq.report.controller.resource;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.report.controller.listener.ListenersManager;
import il.co.topq.report.model.Session;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/executions")
public class ExecutionResource {

	@POST
	@Produces(MediaType.TEXT_PLAIN)
	public String post() {
		int executionIndex = Session.INSTANCE.addExecution();
		ListenersManager.INSTANCE.notifyExecutionAdded(Session.INSTANCE.getExecution(executionIndex));
		return Integer.toString(executionIndex);
	}

	/**
	 * Use it to signal that the execution has ended. This is very important
	 * since in some cases, the HTML reports will not be created or created only
	 * partially if this method will not be called
	 */
	@DELETE
	@Path("/{execution}")
	public void delete(@PathParam("execution") int executionIndex) {
		ListenersManager.INSTANCE.notifyExecutionEnded(Session.INSTANCE.getExecution(executionIndex));
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Execution get() {
		return Session.INSTANCE.getExecution();
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{execution}")
	public Execution get(@PathParam("execution") int execution) {
		return Session.INSTANCE.getExecution(execution);
	}

}
