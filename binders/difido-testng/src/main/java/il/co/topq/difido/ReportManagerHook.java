package il.co.topq.difido;

import org.testng.*;
import org.testng.internal.IResultListener2;

import java.io.File;

/**
 * The ReportManagerHook responsibility is to translate TestNG events to events that
 * are more suitable for the reporters.
 *
 */
public class ReportManagerHook implements IResultListener2, ISuiteListener, IInvokedMethodListener, IExecutionListener {

	/**
	 * Since we need more information in the start execution event, we can't use
	 * the one that is triggered from the IExecutionListener, so we use the onSuiteStart event
	 * This flag helps in using the onSuiteStart event for creating the onExecutionStart event
	 */
	private static boolean firstSuite = true;

	@Override
	public void onStart(ISuite suite) {
		if (firstSuite) {
			firstSuite = false;
			ReportManager.getInstance().onExecutionStart(suite.getHost(), new File(suite.getOutputDirectory()).getParent());
		}
		ReportManager.getInstance().onSuiteStart(suite);
	}

	@Override
	public void onStart(ITestContext context) {
		ReportManager.getInstance().onTestStart(context);
	}

	private void beforeSetup(IInvokedMethod method, ITestResult testResult) {
		ReportManager.getInstance().beforeSetup(method, testResult);
	}

	private void afterSetup(IInvokedMethod method, ITestResult testResult) {
		ReportManager.getInstance().afterSetup(method, testResult);
	}

	@Override
	public void onTestStart(ITestResult result) {
		ReportManager.getInstance().onTestMethodStart(result);
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		ReportManager.getInstance().onTestMethodSuccess(result);
	}

	@Override
	public void onTestFailure(ITestResult result) {
		ReportManager.getInstance().onTestMethodFailure(result);
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		ReportManager.getInstance().onTestMethodSkipped(result);
	}

	private void beforeTeardown(IInvokedMethod method, ITestResult testResult) {
		ReportManager.getInstance().beforeTeardown(method, testResult);
	}

	private void afterTeardown(IInvokedMethod method, ITestResult testResult) {
		ReportManager.getInstance().afterTeardown(method, testResult);
	}

	@Override
	public void onFinish(ITestContext context) {
		ReportManager.getInstance().onTestFinish(context);
	}

	@Override
	public void onFinish(ISuite suite) {
		ReportManager.getInstance().onSuiteFinish(suite);
	}

	public void onExecutionFinish() {
		ReportManager.getInstance().onExecutionFinish();
	}

	@Override
	public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
		if (method.getTestMethod().isBeforeClassConfiguration() 
				| method.getTestMethod().isBeforeGroupsConfiguration()
				| method.getTestMethod().isBeforeMethodConfiguration()
				| method.getTestMethod().isBeforeSuiteConfiguration()
				| method.getTestMethod().isBeforeTestConfiguration()) {
			afterSetup(method, testResult);
		} else if (method.getTestMethod().isAfterClassConfiguration() 
				| method.getTestMethod().isAfterGroupsConfiguration()
				| method.getTestMethod().isAfterMethodConfiguration()
				| method.getTestMethod().isAfterSuiteConfiguration()
				| method.getTestMethod().isAfterTestConfiguration()) {
			afterTeardown(method, testResult);
		}
	}
	
	@Override
	public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
		if (method.getTestMethod().isBeforeClassConfiguration() 
				| method.getTestMethod().isBeforeGroupsConfiguration()
				| method.getTestMethod().isBeforeMethodConfiguration()
				| method.getTestMethod().isBeforeSuiteConfiguration()
				| method.getTestMethod().isBeforeTestConfiguration()) {
			beforeSetup(method, testResult);
		} else if (method.getTestMethod().isAfterClassConfiguration() 
				| method.getTestMethod().isAfterGroupsConfiguration()
				| method.getTestMethod().isAfterMethodConfiguration()
				| method.getTestMethod().isAfterSuiteConfiguration()
				| method.getTestMethod().isAfterTestConfiguration()) {
			beforeTeardown(method, testResult);
		}

	}

	// ############### Unused methods ##################

	@Override
	public void onExecutionStart() {
		// Since we need more information on start, we ignore this even and creates
		// One by our self in the onSutieStart event
	}

	@Override
	public void beforeConfiguration(ITestResult tr) {
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
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
	}

}
