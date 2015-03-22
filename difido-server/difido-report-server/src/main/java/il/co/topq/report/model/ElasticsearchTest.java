package il.co.topq.report.model;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 
 * 
 * @author Itai.Agmon
 *
 */
public class ElasticsearchTest {
	
	@JsonProperty("name")
	private String name;
	
	@JsonProperty("uid")
	private String uid;
	
	@JsonProperty("execution")
	private String execution;
	
	@JsonProperty("parent")
	private String parent;
	
	@JsonProperty("status")
	private String status;
	
	@JsonProperty("executionTimestamp")
	private String executionTimeStamp;
	
	@JsonProperty("machine")
	private String machine;
	
	@JsonProperty("description")
	private String description;
	
	@JsonProperty("timestamp")
	private String timeStamp;
	
	@JsonProperty("url")
	private String url;
	
	@JsonProperty("duration")
	private long duration;
	
	@JsonProperty("parameters")
	private Map<String, String> parameters;

	@JsonProperty("properties")
	private Map<String, String> properties;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getExecution() {
		return execution;
	}

	public void setExecution(String execution) {
		this.execution = execution;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getExecutionTimeStamp() {
		return executionTimeStamp;
	}

	public void setExecutionTimeStamp(String executionTimeStamp) {
		this.executionTimeStamp = executionTimeStamp;
	}

	public String getMachine() {
		return machine;
	}

	public void setMachine(String machine) {
		this.machine = machine;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(String timeStamp) {
		this.timeStamp = timeStamp;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("Name: ").append(name);
		sb.append(" UID: ").append(uid);
		sb.append(" Machine: ").append(machine);
		sb.append(" Status: ").append(status);
		return sb.toString();
	}

	
}
