package il.co.topq.difido.client;


import org.junit.After;
import org.junit.Before;

public class AbstractTestCase {
	
	protected DifidoClient client;
	
	private String baseUri = "http://localhost:8090/api";

	@Before
	public void setUp() {
		System.out.println("@Before - About to connect client to " + baseUri);
		client = DifidoClient.build(baseUri);
	}
	
	@After
	public void tearDown(){
		client.close();
	}

	
}
