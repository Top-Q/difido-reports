package il.co.topq.report.business.elastic;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class EsControllerInitIT {
	
	private List<String> hosts = new ArrayList<String>();	
	private List<String> ports = new ArrayList<String>();
	
	@Before
	public void setup() {
		hosts.clear();
		ports.clear();
	}
	
	@Test
	public void testBuildClientWithBiggerHostsListThenPorts() {
		hosts.add("127.0.0.1");
		hosts.add("127.0.0.2");

		ports.add("9200");
				
		ESController.buildClient(hosts, ports);
	}
	
	@Test
	public void testBuildClientWithPortListEqualsToHostsList() {
		hosts.add("127.0.0.1");
		hosts.add("127.0.0.2");
		
		ports.add("9200");
		ports.add("9201");
				
		ESController.buildClient(hosts, ports);
	}

	
}
