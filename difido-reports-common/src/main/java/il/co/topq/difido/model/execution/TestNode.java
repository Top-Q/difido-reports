package il.co.topq.difido.model.execution;

import org.codehaus.jackson.annotate.JsonIgnore;

public class TestNode extends Node {

	private int index;

	private long duration;

	private String timestamp;

	public TestNode() {

	}

	public TestNode(String name) {
		super(name);
	}

	public TestNode(int index, String name) {
		super(name);
		if (index < 0) {
			throw new IllegalArgumentException("index can't be smaller then 0");
		}
		this.index = index;
	}

	/**
	 * Copy constructor
	 * 
	 * @param aTestNode
	 * @return
	 */
	@JsonIgnore
	public static TestNode newInstance(TestNode aTestNode) {
		TestNode testNodeCopy = new TestNode(aTestNode.getIndex(), aTestNode.getName());
		testNodeCopy.setDuration(aTestNode.getDuration());
		testNodeCopy.setParent(aTestNode.getParent());
		testNodeCopy.setStatus(aTestNode.getStatus());
		testNodeCopy.setTimestamp(aTestNode.getTimestamp());
		return testNodeCopy;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(String timestamp) {
		this.timestamp = timestamp;
	}

}
