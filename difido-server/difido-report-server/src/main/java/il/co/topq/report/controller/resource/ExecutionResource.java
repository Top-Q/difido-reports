package il.co.topq.report.controller.resource;

import il.co.topq.difido.model.execution.Execution;
import il.co.topq.difido.model.remote.ExecutionDetails;
import il.co.topq.report.controller.listener.ListenersManager;
import il.co.topq.report.model.ExecutionManager;
import il.co.topq.report.model.ExecutionManager.ExecutionMetaData;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Path("/executions")
public class ExecutionResource {

	private static final Logger log = LoggerFactory.getLogger(ExecutionResource.class);

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public int post(ExecutionDetails executionDetails) {
		if (null == executionDetails) {
			log.debug("POST - Adding new execution - No description was specified");
			return ExecutionManager.INSTANCE.addExecution();

		}
		log.debug("POST - Adding new execution " + executionDetails);
		if (executionDetails.isShared() && !executionDetails.isForceNew()) {
			return ExecutionManager.INSTANCE.getSharedExecutionIndexAndAddIfNoneExist(executionDetails);
		}
		return ExecutionManager.INSTANCE.addExecution(executionDetails);

	}

	/**
	 * Used to update that a single execution should not be active any more.
	 * This is Irreversible.
	 * 
	 * @param executionIndex
	 *            the id of the execution
	 * @param active
	 *            - Should the execution set to not active
	 */
	@PUT
	@Path("/{execution: [0-9]+}")
	public void put(@PathParam("execution") int executionIndex, @QueryParam("active") boolean active) {
		log.debug("PUT - Upating execution with id " + executionIndex + ". to active: " + active);
		if (!active) {
			ListenersManager.INSTANCE.notifyExecutionEnded(executionIndex,
					ExecutionManager.INSTANCE.getExecution(executionIndex));
		}
	}

	/**
	 * Delete a single execution from the server. A notification will be sent to delete it from all the listeners.
	 * @param executionIndex
	 */
	@DELETE
	@Path("/{execution: [0-9]+}")
	public void delete(@PathParam("execution") int executionIndex) {
		log.debug("DELETE - Delete execution with id " + executionIndex);
		final ExecutionMetaData executionMetaData = ExecutionManager.INSTANCE.getExecutionMetaData(executionIndex);
		if (null == executionMetaData) {
			log.warn("Trying to delete execution with index " + executionIndex + " which is not exist");
			return;
		}
		if (executionMetaData.isActive()) {
			log.warn("Trying to delete execution with index " + executionIndex + " which is still active");
			return;
		}

		ListenersManager.INSTANCE.notifyExecutionDeleted(executionIndex);
	}

	// ********* Deprecated methods

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/{execution: [0-9]+}")
	@Deprecated
	public Execution get(@PathParam("execution") int execution) {
		log.debug("GET - Get execution with id " + execution);
		return ExecutionManager.INSTANCE.getExecution(execution);
	}

	/**
	 * In case the client doesn't know the execution id, since it was opened
	 * from a different client, it can call to this service and receive the last
	 * execution index. If no execution is currently active. A new execution
	 * would be created
	 * 
	 * @deprecated Use the post service with the execution details.
	 * 
	 * @return The id of the last active execution.
	 */
	@GET
	@Path("/lastId")
	@Produces(MediaType.TEXT_PLAIN)
	@Deprecated
	public int getLastExecutionId() {
		final int index = ExecutionManager.INSTANCE.getSharedExecutionIndexAndAddIfNoneExist(null);
		log.debug("GET - Last execution id. Id is " + index);
		return index;
	}

	// *****************************

}
