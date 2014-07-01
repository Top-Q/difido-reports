package il.co.topq.difido.model.execution;



import il.co.topq.difido.model.Enums.Status;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonSubTypes;
import org.codehaus.jackson.annotate.JsonSubTypes.Type;
import org.codehaus.jackson.annotate.JsonTypeInfo;

@JsonTypeInfo(
	    use = JsonTypeInfo.Id.NAME,
	    include = JsonTypeInfo.As.PROPERTY,
	    property = "type")
	@JsonSubTypes({
	    @Type(value = ScenarioNode.class, name = "scenario"),
	    @Type(value = TestNode.class, name = "test"),
	    @Type(value = MachineNode.class, name = "machine") })

public abstract class Node {

	private String name;

	private Status status = Status.success;

	@JsonIgnore
	private NodeWithChildren parent;

	public Node() {

	}

	public Node(String name) {
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

	public NodeWithChildren getParent() {
		return parent;
	}

	public void setParent(NodeWithChildren parent) {
		this.parent = parent;
	}

}
