package il.co.topq.report.model.execution;



import il.co.topq.report.model.Enums.Status;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;

@JsonTypeInfo(
	    use = JsonTypeInfo.Id.NAME,
	    include = JsonTypeInfo.As.PROPERTY,
	    property = "type")
	@JsonSubTypes({
	    @Type(value = ReportedScenario.class, name = "scenario"),
	    @Type(value = ReportedTest.class, name = "test"),
	    @Type(value = ReportedMachine.class, name = "machine") })

public abstract class ReportedNode {

	private String name;

	private Status status = Status.success;

	@JsonIgnore
	private ReportedNodeWithChildren parent;

	public ReportedNode() {

	}

	public ReportedNode(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		if (getStatus().ordinal() < status.ordinal()) {
			this.status = status;

		}
		if (getParent() != null){
			getParent().setStatus(status);
		}
	}

	public ReportedNodeWithChildren getParent() {
		return parent;
	}

	public void setParent(ReportedNodeWithChildren parent) {
		this.parent = parent;
	}

}
