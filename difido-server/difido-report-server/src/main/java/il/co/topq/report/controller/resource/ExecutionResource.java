package il.co.topq.report.controller.resource;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.report.controller.listener.ListenersManager;
import il.co.topq.report.model.ExecutionManager;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/executions")
public class ExecutionResource {

	private static final Logger log = LoggerFactory.getLogger(ExecutionResource.class);

	@POST
	@Produces(MediaType.TEXT_PLAIN)
	public int post() {
		log.debug("POST - Add execution");
		return addExecution();
	}

	private int addExecution() {
		return ExecutionManager.INSTANCE.addExecution();
	}

	/**
	 * Use it to signal that the execution has ended. This is very important
	 * since in some cases, the HTML reports will not be created or created only
	 * partially if this method will not be called
	 */
	@DELETE
	@Path("/{execution}")
	public void delete(@PathParam("execution") int executionIndex) {
		log.debug("DELETE - Delete execution with id " + executionIndex);
		ListenersManager.INSTANCE.notifyExecutionEnded(executionIndex, ExecutionManager.INSTANCE.getExecution(executionIndex));
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
		final int index = ExecutionManager.INSTANCE.getLastExecutionIndexAndAddIfNoneExist();
		log.debug("GET - Last execution id. Id is " + index);
		return index;
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{execution: [0-9]+}")
	public Execution get(@PathParam("execution") int execution) {
		log.debug("GET - Get execution with id " + execution);
		return ExecutionManager.INSTANCE.getExecution(execution);
	}

}
