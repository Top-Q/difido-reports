package il.co.topq.difido.reporters;

import java.io.File;

import org.testng.IInvokedMethod;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestResult;

import il.co.topq.difido.model.Enums.ElementType;
import il.co.topq.difido.model.Enums.Status;

public interface Reporter {

	void onTestStart(ITestResult result);

	void onTestSuccess(ITestResult result);

	void onTestFailure(ITestResult result);

	void onTestSkipped(ITestResult result);

	void onStart(ITestContext context);

	void onFinish(ITestContext context);
	
	void beforeTeardown(IInvokedMethod method, ITestResult testResult);

	void beforeSetup(IInvokedMethod method, ITestResult testResult);

	void afterTeardown(IInvokedMethod method, ITestResult testResult);

	void afterSetup(IInvokedMethod method, ITestResult testResult);

	void addFile(File file);

	/**
	 * Event for start of suite
	 * 
	 * @param suite
	 * 		Suite that is getting started
	 */
	void onStart(ISuite suite);

	/**
	 * Event for end of suite
	 * 
	 * @param suite
	 * 		Suite that finished
	 */
	void onFinish(ISuite suite);

	/**
	 * This method will be called by the ListenerManager when new report is
	 * added. note: this method added to manage situation with warning status
	 * 
	 * @param title
	 *            the report title.
	 * @param message
	 *            the report message
	 * @param status
	 *            pass/fail/warning
	 * @param type
	 * 			  The type of the element to report
	 */
	void log(String title, String message, Status status, ElementType type);

	/**
	 * Add free property to the current test
	 * 
	 * @param name
	 * @param value
	 */
	void addTestProperty(String name, String value);

	/**
	 * Add free property to the whole run
	 * 
	 * @param name
	 * @param value
	 */
	void addRunProperty(String name, String value);

	/**
	 * Get the reporter unique name.
	 * 
	 * @return the reporter name.
	 */
	String getName();

	File getCurrentTestFolder();

}
