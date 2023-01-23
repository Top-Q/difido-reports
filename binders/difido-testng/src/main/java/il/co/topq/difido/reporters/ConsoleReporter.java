package il.co.topq.difido.reporters;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.concurrent.TimeUnit;

import org.testng.IInvokedMethod;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestResult;

import il.co.topq.difido.model.Enums.ElementType;
import il.co.topq.difido.model.Enums.Status;

/**
 * @author Rony Byalsky
 */
public class ConsoleReporter implements Reporter {

	private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss:");
	private boolean testStarted = false;

	@Override
	public void onTestMethodStart(ITestResult result) {
		if (!testStarted) {
			print("------------------------------------------------------------------------");
			print("[TEST START]: " +  result.getName());
			print("------------------------------------------------------------------------");
			testStarted = true;
		}
	}

	@Override
	public void onTestMethodSuccess(ITestResult result) {
		if (testStarted) {
			long testDuration = result.getEndMillis() - result.getStartMillis();
			print("------------------------------------------------------------------------");
			print("[TEST END]: " + result.getName()); 
			print("duration: " + TimeUnit.MILLISECONDS.toSeconds(testDuration) + " seconds");
			print("status: success");
			print("------------------------------------------------------------------------");
			testStarted = false;
		}
	}

	@Override
	public void onTestMethodFailure(ITestResult result) {
		if (testStarted) {
			long testDuration = result.getEndMillis() - result.getStartMillis();
			print("------------------------------------------------------------------------");
			print("[TEST END]: " + result.getName()); 
			print("duration: " + TimeUnit.MILLISECONDS.toSeconds(testDuration) + " seconds");
			print("status: failure");
			print("------------------------------------------------------------------------");
			testStarted = false;
		}
	}

	@Override
	public void onTestMethodSkipped(ITestResult result) {
		print("------------------------------------------------------------------------");
		print("[TEST SKIPPED]: " +  result.getName());
		print("------------------------------------------------------------------------");
	}

	@Override
	public void onTestStart(ITestContext context) {
	}

	@Override
	public void onTestFinish(ITestContext context) {
	}

	@Override
	public void addFile(File file) {
		print("Adding file: " +  file.getName());
	}

	@Override
	public void onExecutionStart(String host, String outputDir) {

	}

	@Override
	public void onSuiteStart(ISuite suite) {

	}

	@Override
	public void onSuiteFinish(ISuite suite) {

	}

	@Override
	public void onExecutionFinish() {

	}

	@Override
	public void log(String title, String message, Status status, ElementType type) {
		
		StringBuilder sb = new StringBuilder();

		switch(type) {
		case regular:
			break;
		case startLevel:
			sb.append("[START LEVEL]: ");
			break;
		case step:
			sb.append("[STEP]: ");
			break;
		case stopLevel:
			sb.append("[STOP LEVEL]");
			break;
		case html:
			sb.append("[HTML]: ");
			break;
		case img:
			sb.append("[IMAGE]: ");
			break;
		case lnk:
			sb.append("[LINK]: ");
			break;
		}
		
		switch(status) {
		case in_progress:
		case success:
			break;
		case warning:
			sb.append("[WARNING]: ");
			break;
		case error:
			sb.append("[ERROR]: ");
			break;
		case failure:
			sb.append("[FAILURE]: ");
			break;
		}

		if (title != null && !title.equals("")) {
			
			if (message != null && !message.equals("")) {
				sb.append(title + " - " + message);
			}
			else {
				sb.append(title);
			}
		}
		
		print(sb.toString());
	}

	@Override
	public void addTestProperty(String name, String value) {
	}

	@Override
	public void addRunProperty(String name, String value) {
	}

	@Override
	public String getName() {
		return "ConsoleReporter";
	}

	private void print(String message) {
		System.out.println(TIME_FORMAT.format(System.currentTimeMillis()) + " " + message);
	}

	@Override
	public void beforeTeardown(IInvokedMethod method, ITestResult testResult) {
	}

	@Override
	public void beforeSetup(IInvokedMethod method, ITestResult testResult) {
	}

	@Override
	public void afterTeardown(IInvokedMethod method, ITestResult testResult) {
	}

	@Override
	public void afterSetup(IInvokedMethod method, ITestResult testResult) {
	}

}
