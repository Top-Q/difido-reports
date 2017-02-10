
package il.co.topq.report.business.elastic.client.response;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "name",
    "uid",
    "executionId",
    "execution",
    "parent",
    "status",
    "executionTimestamp",
    "machine",
    "description",
    "timestamp",
    "url",
    "duration",
    "parameters",
    "properties",
    "scenarioProperties"
})
public class ElasticSource {

    @JsonProperty("name")
    private String name;
    @JsonProperty("uid")
    private String uid;
    @JsonProperty("executionId")
    private Integer executionId;
    @JsonProperty("execution")
    private String execution;
    @JsonProperty("parent")
    private Object parent;
    @JsonProperty("status")
    private String status;
    @JsonProperty("executionTimestamp")
    private String executionTimestamp;
    @JsonProperty("machine")
    private String machine;
    @JsonProperty("description")
    private String description;
    @JsonProperty("timestamp")
    private String timestamp;
    @JsonProperty("url")
    private String url;
    @JsonProperty("duration")
    private Integer duration;
    @JsonProperty("parameters")
    private Object parameters;
    @JsonProperty("properties")
    private Properties properties;
    @JsonProperty("scenarioProperties")
    private Object scenarioProperties;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("name")
    public String getName() {
        return name;
    }

    @JsonProperty("name")
    public void setName(String name) {
        this.name = name;
    }

    @JsonProperty("uid")
    public String getUid() {
        return uid;
    }

    @JsonProperty("uid")
    public void setUid(String uid) {
        this.uid = uid;
    }

    @JsonProperty("executionId")
    public Integer getExecutionId() {
        return executionId;
    }

    @JsonProperty("executionId")
    public void setExecutionId(Integer executionId) {
        this.executionId = executionId;
    }

    @JsonProperty("execution")
    public String getExecution() {
        return execution;
    }

    @JsonProperty("execution")
    public void setExecution(String execution) {
        this.execution = execution;
    }

    @JsonProperty("parent")
    public Object getParent() {
        return parent;
    }

    @JsonProperty("parent")
    public void setParent(Object parent) {
        this.parent = parent;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("executionTimestamp")
    public String getExecutionTimestamp() {
        return executionTimestamp;
    }

    @JsonProperty("executionTimestamp")
    public void setExecutionTimestamp(String executionTimestamp) {
        this.executionTimestamp = executionTimestamp;
    }

    @JsonProperty("machine")
    public String getMachine() {
        return machine;
    }

    @JsonProperty("machine")
    public void setMachine(String machine) {
        this.machine = machine;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @JsonProperty("timestamp")
    public String getTimestamp() {
        return timestamp;
    }

    @JsonProperty("timestamp")
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @JsonProperty("url")
    public String getUrl() {
        return url;
    }

    @JsonProperty("url")
    public void setUrl(String url) {
        this.url = url;
    }

    @JsonProperty("duration")
    public Integer getDuration() {
        return duration;
    }

    @JsonProperty("duration")
    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    @JsonProperty("parameters")
    public Object getParameters() {
        return parameters;
    }

    @JsonProperty("parameters")
    public void setParameters(Object parameters) {
        this.parameters = parameters;
    }

    @JsonProperty("properties")
    public Properties getProperties() {
        return properties;
    }

    @JsonProperty("properties")
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    @JsonProperty("scenarioProperties")
    public Object getScenarioProperties() {
        return scenarioProperties;
    }

    @JsonProperty("scenarioProperties")
    public void setScenarioProperties(Object scenarioProperties) {
        this.scenarioProperties = scenarioProperties;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
