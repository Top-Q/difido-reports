package il.co.topq.report.model.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

@JsonPropertyOrder({ "name", "description", "timestamp", "duration", "parameters", "properties", "reportElements" })
public class TestDetails {

	@JsonProperty("name")
	private String name;

	@JsonProperty("description")
	private String description;

	@JsonProperty("timestamp")
	private String timeStamp;

	@JsonProperty("duration")
	private long duration;

	@JsonProperty("parameters")
	private Map<String, String> parameters;

	@JsonProperty("properties")
	private Map<String, String> properties;

	@JsonProperty("reportElements")
	private List<ReportElement> reportElements;

	public TestDetails(String name) {
		this.name = name;
	}

	public TestDetails() {

	}

	@JsonIgnore
	public void addReportElement(ReportElement element) {
		if (null == reportElements) {
			reportElements = new ArrayList<ReportElement>();
		}
		reportElements.add(element);
	}

	@JsonIgnore
	public void addProperty(String key, String value) {
		if (properties == null) {
			properties = new HashMap<String, String>();
		}
		properties.put(key, value);
	}

	@JsonIgnore
	public void addParameter(String key, String value) {
		if (parameters == null) {
			parameters = new HashMap<String, String>();
		}
		parameters.put(key, value);
	}

	@Override
	@JsonIgnore
	public int hashCode() {
		int hash = 1;
		if (parameters != null) {
			hash = hash * 17 + parameters.hashCode();
		}
		if (properties != null) {
			hash = hash * 13 + properties.hashCode();
		}
		if (name != null) {
			hash = hash * 31 + name.hashCode();
		}
		return hash;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public List<ReportElement> getReportElements() {
		return reportElements;
	}

	public void setReportElements(List<ReportElement> reportElements) {
		this.reportElements = reportElements;
	}

}
