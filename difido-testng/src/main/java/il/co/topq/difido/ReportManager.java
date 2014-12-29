package il.co.topq.difido;

import il.co.topq.difido.model.Enums.ElementType;
import il.co.topq.difido.model.Enums.Status;

import java.util.ArrayList;
import java.util.List;

import org.testng.ITestContext;
import org.testng.ITestResult;

public class ReportManager {

	private static ReportManager instance;

	private List<Reporter> reporters = new ArrayList<Reporter>();

	private ReportManager() {
		reporters.add(new LocalDifidoReporter());
	}

	public static ReportManager getInstance() {
		if (null == instance) {
			instance = new ReportManager();
		}
		return instance;
	}

	public void onTestStart(ITestResult result) {
		for (Reporter reporter : reporters) {
			reporter.onTestStart(result);
		}

	}

	public void log(String title, String message, Status status, ElementType type) {
		for (Reporter reporter : reporters) {
			reporter.log(title, message, status, type);
		}
	}

	public void onTestSuccess(ITestResult result) {
		for (Reporter reporter : reporters) {
			reporter.onTestSuccess(result);
		}

	}

	public void onTestFailure(ITestResult result) {
		for (Reporter reporter : reporters) {
			reporter.onTestFailure(result);
		}

	}

	public void onTestSkipped(ITestResult result) {
		for (Reporter reporter : reporters) {
			reporter.onTestSkipped(result);
		}

	}

	public void onStart(ITestContext context) {
		for (Reporter reporter : reporters) {
			reporter.onStart(context);
		}
	}

	public void onFinish(ITestContext context) {
		for (Reporter reporter : reporters) {
			reporter.onFinish(context);
		}

	}



}
