package il.co.topq.difido;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class MyRetryAnalyzer implements IRetryAnalyzer {

	private int retryCount = 0;
	private int maxRetryCount = 3;

	public boolean retry(ITestResult result) {
		System.out.println("Here");
		if (retryCount < maxRetryCount) {
			retryCount++;
			return true;
		}
		return false;
	}

}
