package il.co.topq.difido.model.execution;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

public abstract class NodeWithChildren extends Node {
	
	private List<Node> children;
	
	public NodeWithChildren(){
		
	}
	
	public NodeWithChildren(String name){
		super(name);
	}
	
	@JsonIgnore
	public void addChild(Node node){
		if (null == children){
			children = new ArrayList<Node>();
		}
		node.setParent(this);
		children.add(node);
	}

	public List<Node> getChildren() {
		return children;
	}
	
	@JsonIgnore
	public List<Node> getChildren(boolean recursivly) {
		if (!recursivly){
			return children;
		}
		List<Node> allChildren = new ArrayList<>();
		if (null == children){
			return allChildren;
		}
		for (Node child : children){
			allChildren.add(child);
			if (child instanceof NodeWithChildren){
				allChildren.addAll(((NodeWithChildren)child).getChildren(true));
			}
		}
		return allChildren;
	}


	public void setChildren(List<Node> children) {
		this.children = children;
	}
	
	
	
}
