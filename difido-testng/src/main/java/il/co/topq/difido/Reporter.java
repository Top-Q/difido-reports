package il.co.topq.difido;

import java.io.File;

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
	 * @param bold
	 *            true if bold
	 */
	void log(String title, String message, Status status, ElementType type);

	/**
	 * Get the reporter unique name.
	 * 
	 * @return the reporter name.
	 */
	String getName();

	File getCurrentTestFolder();

}
