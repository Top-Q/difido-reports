package il.co.topq.report.model.execution;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

public abstract class ReportedNodeWithChildren extends ReportedNode {
	
	private List<ReportedNode> children;
	
	public ReportedNodeWithChildren(){
		
	}
	
	public ReportedNodeWithChildren(String name){
		super(name);
	}
	
	@JsonIgnore
	public void addChild(ReportedNode node){
		if (null == children){
			children = new ArrayList<ReportedNode>();
		}
		node.setParent(this);
		children.add(node);
	}

	public List<ReportedNode> getChildren() {
		return children;
	}
	
	@JsonIgnore
	public List<ReportedNode> getChildren(boolean recursivly) {
		if (!recursivly){
			return children;
		}
		List<ReportedNode> allChildren = new ArrayList<>();
		if (null == children){
			return allChildren;
		}
		for (ReportedNode child : children){
			allChildren.add(child);
			if (child instanceof ReportedNodeWithChildren){
				allChildren.addAll(((ReportedNodeWithChildren)child).getChildren(true));
			}
		}
		return allChildren;
	}


	public void setChildren(List<ReportedNode> children) {
		this.children = children;
	}
	
	
	
}
