package il.co.topq.difido.model.execution;

public class TestNode extends Node {

	private int index;

	private long duration;

	private String timestamp;

	public TestNode() {

	}

	public TestNode(int index, String name) {
		super(name);
		this.index = index;
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
