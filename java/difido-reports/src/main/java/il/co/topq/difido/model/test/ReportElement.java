package il.co.topq.difido.model.test;


import il.co.topq.difido.model.Enums.ElementType;
import il.co.topq.difido.model.Enums.Status;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.annotate.JsonPropertyOrder;

@JsonPropertyOrder({ "title", "message", "status", "type", "time", })
public class ReportElement {

	@JsonProperty("title")
	private String title = null;

	@JsonProperty("message")
	private String message = null;

	@JsonProperty("status")
	private Status status;

	@JsonProperty("type")
	private ElementType type;

	@JsonProperty("time")
	private String time;

	@JsonProperty("title")
	public String getTitle() {
		return title;
	}

	@JsonProperty("title")
	public void setTitle(String title) {
		this.title = title;
	}

	@JsonProperty("message")
	public String getMessage() {
		return message;
	}

	@JsonProperty("message")
	public void setMessage(String message) {
		this.message = message;
	}

	@JsonProperty("status")
	public Status getStatus() {
		return status;
	}

	@JsonProperty("status")
	public void setStatus(Status status) {
		this.status = status;
	}

	@JsonProperty("type")
	public ElementType getType() {
		return type;
	}

	@JsonProperty("type")
	public void setType(ElementType type) {
		this.type = type;
	}

	@JsonProperty("time")
	public String getTime() {
		return time;
	}

	@JsonProperty("time")
	public void setTime(String time) {
		this.time = time;
	}

}
