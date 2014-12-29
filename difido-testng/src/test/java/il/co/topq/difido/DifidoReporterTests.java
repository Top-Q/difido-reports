package il.co.topq.difido;

import il.co.topq.difido.model.Enums.ElementType;
import il.co.topq.difido.model.Enums.Status;

import org.testng.annotations.Test;

public class DifidoReporterTests {

	ReportManager report = ReportManager.getInstance();

	@Test
	public void simpleReportCall0() {
		report.log("some title", "Some message", Status.success, ElementType.regular);
	}

	@Test
	public void simpleReportCall1() {
		report.log("some title", "Some message", Status.success, ElementType.regular);
	}

	@Test
	public void failure() throws Exception {
		report.log("About to fail", "Some message", Status.success, ElementType.regular);
		throw new Exception("This is my failure");
	}

}
