package il.co.topq.difido.model;

import il.co.topq.difido.model.Enums.ElementType;
import il.co.topq.difido.model.Enums.Status;
import il.co.topq.difido.model.test.ReportElement;
import il.co.topq.difido.model.test.TestDetails;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestTestDetails {

	@Test
	public void testTestDetails() throws JsonGenerationException, JsonMappingException, IOException {
		TestDetails t = new TestDetails("TestOne");
		t.setDescription("This is the test description");
		t.setDuration(34323l);
		t.addParameter("param0", "val0");
		t.addParameter("param1", "val1");
		t.addParameter("param2", "val2");
		t.addProperty("prop0", "val0");
		t.addProperty("prop1", "val1");
		t.setTimeStamp("23/12/2013");
		ReportElement e0 = new ReportElement();
		e0.setTitle("this is the title");
		e0.setMessage("This is the message");
		e0.setType(ElementType.step);
		e0.setTime("23:34");
		e0.setStatus(Status.failure);
		t.addReportElement(e0);
		
		ReportElement e1 = new ReportElement();
		e1.setTitle("this is the title");
		e1.setMessage("This is the message");
		e1.setType(ElementType.html);
		e1.setTime("34:34");
		e1.setStatus(Status.success);
		t.addReportElement(e1);

		
		ObjectMapper mapper = new ObjectMapper();
		String str = mapper.writeValueAsString(t);
		System.out.println(str);

	}

}
