package il.co.topq.difido.model.remote;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExecutionMetadataUpdateRequest {

	@JsonProperty("executionId")
	private int executionId;
	
	@JsonProperty("executionDescription")
	private String executionDescription;
	
	@JsonProperty("executionComment")
	private String executionComment;

	public int getExecutionId() {
		return executionId;
	}

	public void setExecutionId(int executionId) {
		this.executionId = executionId;
	}

	public String getExecutionDescription() {
		return executionDescription;
	}

	public void setExecutionDescription(String executionDescription) {
		this.executionDescription = executionDescription;
	}

	public String getExecutionComment() {
		return executionComment;
	}

	public void setExecutionComment(String executionComment) {
		this.executionComment = executionComment;
	}
}
