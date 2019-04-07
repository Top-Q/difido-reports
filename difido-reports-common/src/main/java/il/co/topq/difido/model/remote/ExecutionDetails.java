package il.co.topq.difido.model.remote;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * This holds information on the execution that the user want to create. It
 * should be specified each time the add execution service is called. <br>
 * This is used mainly for the remote server and it has no use when working with
 * local reports
 * 
 * @author Itai.Agmon
 *
 */
public class ExecutionDetails {

	/**
	 * The description of the execution as described by the user the triggered
	 * it
	 */
	@JsonProperty("description")
	private String description;

	/**
	 * Free list of properties that can be specified by the user
	 */
	@JsonProperty("executionProperties")
	private HashMap<String, String> executionProperties;

	/**
	 * Is this execution can be shared between different machines.
	 */
	@JsonProperty("shared")
	private boolean shared;

	/**
	 * If set to true, The server will create a new execution even if this
	 * execution is a shared one
	 */
	@JsonProperty("forceNew")
	private boolean forceNew = false;

	public ExecutionDetails() {
	}

	public ExecutionDetails(String description) {
		this.description = description;
	}

	public ExecutionDetails(String description, boolean shared) {
		this.description = description;
		this.shared = shared;
	}

	public ExecutionDetails(String description, boolean shared, boolean forceNew) {
		this.description = description;
		this.shared = shared;
		this.forceNew = forceNew;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Description: ").append(description).append(", ");
		sb.append("ExecutionProperties: ").append(executionProperties).append(", ");
		sb.append("Shared: ").append(shared).append(", ");
		sb.append("ForceNew: ").append(forceNew);
		return sb.toString();
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public HashMap<String, String> getExecutionProperties() {
		return executionProperties;
	}

	public void setExecutionProperties(HashMap<String, String> executionProperties) {
		this.executionProperties = executionProperties;
	}

	public boolean isShared() {
		return shared;
	}

	public void setShared(boolean shared) {
		this.shared = shared;
	}

	public boolean isForceNew() {
		return forceNew;
	}

	public void setForceNew(boolean forceNew) {
		this.forceNew = forceNew;
	}

}
