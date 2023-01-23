package il.co.topq.difido.reporters;

import java.io.File;

import org.testng.IInvokedMethod;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestResult;

import il.co.topq.difido.model.Enums.ElementType;
import il.co.topq.difido.model.Enums.Status;

/**
 *  The reporter interface should be implemented by all classes that are added
 *  to the ReportManager and performs as reporters.
 *  The lifecycle is represented by the order of the method in this interface.
 */
public interface Reporter {

    /**
     * Triggered once at the beginning of the execution
     *
     * @param host Local host which runs the execution
     * @param outputDir Directory to write reports
     */
    void onExecutionStart(String host, String outputDir);

    /**
     * Triggered at beginning of each suite
     *
     * @param suite Interface defining a test suite
     */
    void onSuiteStart(ISuite suite);

    /**
     * Triggered at the beginning of each test. Notice that is not a test method
     * @param context The context of the test case
     */
    void onTestStart(ITestContext context);

    /**
     * Triggered before the setup methods and before the test method
     *
     * @param method The setup method that is about to be invoked
     * @param testResult The result
     */
    void beforeSetup(IInvokedMethod method, ITestResult testResult);

    /**
     * Invoked after the setup methods and before the test method
     *
     * @param method The method that was invoked
     * @param testResult The result so far
     */
    void afterSetup(IInvokedMethod method, ITestResult testResult);

    /**
     * Invoked at the beginning of the test method
     *
     * @param result Result so far
     */
    void onTestMethodStart(ITestResult result);

    /**
     * Add file to the report
     *
     * @param file File to add to the report
     */
    void addFile(File file);

    /**
     * This method will be called by the ListenerManager when new report is
     * added. note: this method added to manage situation with warning status
     *
     * @param title   the report title.
     * @param message the report message
     * @param status  pass/fail/warning
     * @param type The type of the element
     */
    void log(String title, String message, Status status, ElementType type);

    /**
     * Add free property to the current test
     *
     * @param name property name
     * @param value property value
     */
    void addTestProperty(String name, String value);

    /**
     * Add free property to the whole run
     *
     * @param name property name
     * @param value property value
     */
    void addRunProperty(String name, String value);

    /**
     * Invoked when test method finishes with success
     * @param result Test method result
     */
    void onTestMethodSuccess(ITestResult result);

    /**
     * Invoked when test method finishes with failure
     * @param result Test method result
     */
    void onTestMethodFailure(ITestResult result);
    /**
     * Invoked when test method is skipped
     * @param result Test method result
     */
    void onTestMethodSkipped(ITestResult result);

    /**
     * Invoked before the teatdown methods
     * @param method The method that is about to be invoked
     * @param testResult The result so far
     */
    void beforeTeardown(IInvokedMethod method, ITestResult testResult);

    /**
     * Invoked after teardown method
     * @param method The method that was invoked
     * @param testResult The result so far
     */
    void afterTeardown(IInvokedMethod method, ITestResult testResult);

    /**
     * Invoked when test finishes. Note that this is not test method
     * @param context
     */
    void onTestFinish(ITestContext context);

    /**
     * Event for end of suite
     *
     * @param suite Interface the represents suite
     */
    void onSuiteFinish(ISuite suite);

    /**
     * Invoked when the execution is finished
     */
    void onExecutionFinish();

    /**
     * Get the reporter unique name.
     *
     * @return the reporter name.
     */
    String getName();

}
