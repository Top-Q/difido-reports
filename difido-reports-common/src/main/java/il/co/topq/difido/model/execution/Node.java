package il.co.topq.difido.model.execution;

import il.co.topq.difido.model.Enums.Status;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({ @Type(value = ScenarioNode.class, name = "scenario"), @Type(value = TestNode.class, name = "test"),
		@Type(value = MachineNode.class, name = "machine") })
public abstract class Node {

	private String name;

	private Status status = Status.success;

	@JsonIgnore
	private NodeWithChildren<? extends Node> parent;

	public Node() {

	}

	@JsonIgnore
	@Override
	public int hashCode() {
		int result = 17;
		if (name != null) {
			result = 31 * result + name.hashCode();
		}
		if (status != null) {
			result = 31 * result + status.ordinal();
		}
		return result;
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
		if (getParent() != null) {
			getParent().setStatus(status);
		}
	}

	@JsonIgnore
	public NodeWithChildren<? extends Node> getParent() {
		return parent;
	}

	@JsonIgnore
	public void setParent(NodeWithChildren<? extends Node> parent) {
		this.parent = parent;
	}
	
	@Override
	public boolean equals (Object otherNode) {
		if (otherNode instanceof Node) {
			return this.getName().equals(((Node) otherNode).getName());
		} return false;
	}

}
