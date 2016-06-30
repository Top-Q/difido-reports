package il.co.topq.difido;

import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.internal.IResultListener2;

public class ReportManagerHook implements IResultListener2, ISuiteListener {

	/**
	 * From some reason, TestNG is calling the suite events number of times.
	 * From that reason, we are keeping a counter to know when to close the
	 * execution. Also, from another strange reason, TestNG is registering the
	 * hook for every class that has the annotation. From this reason, it is
	 * important to keep the counter static
	 */
	private static int numOfSuites;

	@Override
	public void onTestStart(ITestResult result) {
		ReportManager.getInstance().onTestStart(result);

	}

	@Override
	public void onTestSuccess(ITestResult result) {
		ReportManager.getInstance().onTestSuccess(result);

	}

	@Override
	public void onTestFailure(ITestResult result) {
		ReportManager.getInstance().onTestFailure(result);
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		ReportManager.getInstance().onTestSkipped(result);

	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
	}

	@Override
	public void onStart(ITestContext context) {
		ReportManager.getInstance().onStart(context);

	}

	@Override
	public void onFinish(ITestContext context) {
		ReportManager.getInstance().onFinish(context);

	}

	@Override
	public void onConfigurationSuccess(ITestResult itr) {

	}

	@Override
	public void onConfigurationFailure(ITestResult itr) {

	}

	@Override
	public void onConfigurationSkip(ITestResult itr) {

	}

	@Override
	public void beforeConfiguration(ITestResult tr) {

	}

	@Override
	public void onStart(ISuite suite) {
		if (0 != numOfSuites++) {
			return;
		}
		ReportManager.getInstance().onStart(suite);

	}

	@Override
	public void onFinish(ISuite suite) {
		if (0 != --numOfSuites) {
			return;
		}
		ReportManager.getInstance().onFinish(suite);

	}
}
