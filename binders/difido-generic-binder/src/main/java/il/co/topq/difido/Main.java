package il.co.topq.difido;

import java.io.File;

import il.co.topq.difido.binders.JMeterXmlBinder;
import il.co.topq.difido.engine.LocalReportEngine;
import il.co.topq.difido.engine.ReportEngine;

public class Main {
	
	public static void main(String[] args) throws Exception {
		ReportEngine engine = new LocalReportEngine(new File("jmeter.xml"),new File("difido"));
		engine.init(new JMeterXmlBinder());
		engine.run();
	}
}
