package il.co.topq.report.business.elastic;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

	@JsonProperty("executionId")
	private int executionId;

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

	@JsonProperty("scenarioProperties")
	private Map<String, String> scenarioProperties;

	public ElasticsearchTest(final String uid, final String executionTimeStamp, final String timeStamp) {
		this.uid = uid;
		this.executionTimeStamp = executionTimeStamp;
		this.timeStamp = timeStamp;
	}

	public ElasticsearchTest() {
	}
	
	/**
	 * Copy constructor
	 * @param other
	 */
	public ElasticsearchTest(ElasticsearchTest other){
		this.uid = other.getUid();
		this.executionTimeStamp = other.executionTimeStamp;
		this.timeStamp = other.timeStamp;
		this.description = other.description;
		this.duration = other.duration;
		this.machine = other.machine;
		this.name = other.name;
		this.executionId = other.executionId;
		this.parent = other.parent;
		this.status = other.status;
		this.url = other.url;
		if (other.parameters != null) {
			this.parameters = new HashMap<String,String>(other.parameters);
		}
		if (other.properties != null) {
			this.properties = new HashMap<String,String>(other.properties);
		}
		if (other.scenarioProperties != null){
			this.scenarioProperties = new HashMap<String,String>(other.scenarioProperties);
		}
	
	}
	
	@JsonIgnore
	@Override
	public boolean equals(Object other){
		if (null == other){
			return false;
		}
		if (other.hashCode() != hashCode()){
			return false;
		}
		return true;
	}

	@JsonIgnore
	@Override
	public int hashCode() {
		int result = 31;
		if (uid != null) {
			result = 31 * result + uid.hashCode();
		}
		if (name != null) {
			result = 31 * result + name.hashCode();
		}
		result = 31 * result + executionId;
		if (execution != null) {
			result = 31 * result + execution.hashCode();
		}
		if (parent != null) {
			result = 31 * result + parent.hashCode();
		}
		if (status != null) {
			result = 31 * result + status.hashCode();
		}
		if (executionTimeStamp != null) {
			result = 31 * result + executionTimeStamp.hashCode();
		}
		if (machine != null) {
			result = 31 * result + machine.hashCode();
		}
		if (description != null) {
			result = 31 * result + description.hashCode();
		}
		if (timeStamp != null) {
			result = 31 * result + timeStamp.hashCode();
		}
		if (url != null) {
			result = 31 * result + url.hashCode();
		}
		result = 31 * result + new Long(duration).intValue();

		if (parameters != null) {
			result = 31 * result + parameters.hashCode();
		}
		if (properties != null) {
			result = 31 * result + properties.hashCode();
		}
		if (scenarioProperties != null) {
			result = 31 * result + scenarioProperties.hashCode();
		}
		return result;
	}

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

	public int getExecutionId() {
		return executionId;
	}

	public void setExecutionId(int executionId) {
		this.executionId = executionId;
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

	public Map<String, String> getScenarioProperties() {
		return scenarioProperties;
	}

	public void setScenarioProperties(Map<String, String> scenarioProperties) {
		this.scenarioProperties = scenarioProperties;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Name: ").append(name);
		sb.append(" UID: ").append(uid);
		sb.append(" Machine: ").append(machine);
		sb.append(" Status: ").append(status);
		sb.append(" Execution Id: ").append(executionId);
		return sb.toString();
	}

}
