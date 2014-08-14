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
	public int post() {
		return addExecution();
	}

	private int addExecution() {
		int executionIndex = Session.INSTANCE.addExecution();
		ListenersManager.INSTANCE.notifyExecutionAdded(Session.INSTANCE.getExecution(executionIndex));
		return executionIndex;
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

	/**
	 * In case the client doesn't know the execution id, since it was opened
	 * from a different client, it can call to this service and receive the last
	 * execution index. If no execution is currently active. A new execution
	 * would be created
	 * 
	 * @return The id of the last active execution.
	 */
	@GET
	@Path("/lastId")
	@Produces(MediaType.TEXT_PLAIN)
	public int getLastExecutionId() {
		final Execution execution = Session.INSTANCE.getLastActiveExecution();
		if (null == execution) {
			return addExecution();
		}
		return Session.INSTANCE.getExecutions().indexOf(execution);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{execution: [0-9]+}")
	public Execution get(@PathParam("execution") int execution) {
		return Session.INSTANCE.getExecution(execution);
	}

}
