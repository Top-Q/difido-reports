package il.co.topq.difido.model.execution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

public abstract class NodeWithChildren<T extends Node> extends Node {

	private List<T> children;

	public NodeWithChildren() {

	}

	public NodeWithChildren(String name) {
		super(name);
	}

	@JsonIgnore
	public void addChild(T node) {
		if (null == children) {
			children = new ArrayList<T>();
		}
		node.setParent(this);
		children.add(node);
	}

	public List<T> getChildren() {
		return children;
	}

	/**
	 * Get all the children of the node. If specified recursively, will get also
	 * all the descendants
	 * 
	 * @param recursivly
	 * @return list of all the children.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@JsonIgnore
	public List<T> getChildren(boolean recursivly) {
		if (!recursivly) {
			return children;
		}
		List<T> allChildren = new ArrayList<>();
		if (null == children) {
			return allChildren;
		}
		for (T child : children) {
			allChildren.add(child);
			if (child instanceof NodeWithChildren) {
				allChildren.addAll(((NodeWithChildren) child).getChildren(true));
			}
		}
		return allChildren;
	}

	public void setChildren(List<T> children) {
		this.children = children;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Name: ").append(getName()).append("\n");
		if (null != getChildren()){
			sb.append("Children").append(Arrays.toString(getChildren().toArray())).append("\n");
		}
		return sb.toString();
	}

}
