package il.co.topq.report.resource;

import il.co.topq.report.model.Test;

import java.io.IOException;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/tests")
public class TestResource {

	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Test get(){
		Test t = new Test();
		t.setClazz("my class");
		t.setName("My test name");
		return t;
	}
	/**
	 * Create new test
	 * 
	 * @return
	 */
	@POST
	@Path("/{name}")
	@Produces(MediaType.TEXT_PLAIN)
	public String post(@PathParam("name") String name) {
		return "Test " + name + " was added";
	}

	/**
	 * Ends a previously started test. The test can end with various statues.
	 * Success, Error, failure or warning.
	 * 
	 * @return
	 * @throws IOException
	 */
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String put(Test t) throws IOException {
		return t.toString();
	}

}
