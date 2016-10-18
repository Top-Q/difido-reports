package il.co.topq.difido.model.test;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import il.co.topq.difido.model.Enums.ElementType;
import il.co.topq.difido.model.Enums.Status;

/**
 * 
 * @author itai
 *
 */
// It is very important that the reportElements member will be the last one for
// cases in which we want to append to the file
@JsonPropertyOrder({ "uid", "reportElements" })
public class TestDetails {

	/**
	 * Required for updating the statuses of all the start level elements if one
	 * of the contained elements status is not success.
	 */
	@JsonIgnore
	private List<ReportElement> levelElementsBuffer;

	@JsonProperty("uid")
	private String uid;

	@JsonProperty("reportElements")
	private List<ReportElement> reportElements;

	public TestDetails(String uid) {
		this.uid = uid;
	}

	public TestDetails() {

	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Uid: ").append(uid).append("\n");
		return sb.toString();
	}

	@JsonIgnore
	public void addReportElement(ReportElement element) {
		if (null == reportElements) {
			reportElements = new ArrayList<ReportElement>();
		}
		if (element.getStatus() == null) {
			element.setStatus(Status.success);
		}
		element.setParent(this);
		reportElements.add(element);
		updateLevelElementsBuffer(element);
		updateLevelElementsStatuses(element);
	}

	/**
	 * Update the status of all the level elements in the buffer according to
	 * the specified element;
	 * 
	 * @param element
	 */
	@JsonIgnore
	private void updateLevelElementsStatuses(final ReportElement element) {
		if (null == levelElementsBuffer) {
			// The levelElementsBuffer should have been initialized in the
			// updateLevelElementsBuffer method. If this never happened, that
			// means that we never started a level
			return;
		}
		if (element == null || element.getType() == null) {
			return;
		}
		if (element.getStatus() == Status.success) {
			// Nothing to do
			return;
		}
		for (ReportElement startElement : levelElementsBuffer) {
			if (element.getStatus().ordinal() > startElement.getStatus().ordinal()) {
				startElement.setStatus(element.getStatus());
			}
		}
	}

	/**
	 * Adds start level elements to the buffer or remove element if the
	 * specified element is stop element
	 * 
	 * @param element
	 */
	@JsonIgnore
	private void updateLevelElementsBuffer(final ReportElement element) {
		if (element == null || element.getType() == null) {
			return;
		}
		if (element.getType() == ElementType.startLevel) {
			if (null == levelElementsBuffer) {
				levelElementsBuffer = new ArrayList<>();
			}
			levelElementsBuffer.add(element);
		} else if (element.getType() == ElementType.stopLevel) {
			if (levelElementsBuffer == null || levelElementsBuffer.size() == 0) {
				// Never should happen
				return;
			}
			levelElementsBuffer.remove(levelElementsBuffer.size() - 1);
		}
	}

	public List<ReportElement> getReportElements() {
		return reportElements;
	}

	public void setReportElements(List<ReportElement> reportElements) {
		this.reportElements = reportElements;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

}
