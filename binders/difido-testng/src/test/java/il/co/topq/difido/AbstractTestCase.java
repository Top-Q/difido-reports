package il.co.topq.difido;

import org.testng.annotations.Listeners;

@Listeners(il.co.topq.difido.ReportManagerHook.class)
public abstract class AbstractTestCase {
	
	protected ReportDispatcher report = ReportManager.getInstance();

}
