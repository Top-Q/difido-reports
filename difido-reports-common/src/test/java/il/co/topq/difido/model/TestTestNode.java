package il.co.topq.difido.model;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import il.co.topq.difido.model.Enums.Status;
import il.co.topq.difido.model.execution.TestNode;

public class TestTestNode {

	@Test
	public void testTestNodeHashCodeIsEquals() {
		TestNode test0 = generateTestNode();
		TestNode test1 = generateTestNode();
		Assert.assertTrue(test0.hashCode() == test1.hashCode());
	}
	
	@Test
	public void testTestNodeHashCodeIsNotEquals() {
		TestNode test0 = generateTestNode();
		TestNode test1 = generateTestNode();
		test1.setIndex(2);
		Assert.assertTrue(test0.hashCode() != test1.hashCode());
	}


	@Test
	public void testTestNodeEquals() {
		TestNode test0 = generateTestNode();
		TestNode test1 = generateTestNode();
		Assert.assertTrue(test0.equals(test1));
	}

	@Test
	public void testTestNodeCopyConstructore() {
		TestNode test0 = generateTestNode();
		TestNode test1 = new TestNode(test0);
		Assert.assertFalse(test0 == test1);
		Assert.assertTrue(test0.equals(test1));
	}

	private TestNode generateTestNode() {
		TestNode test = new TestNode("myTest", "aaa");
		test.setIndex(23);
		test.setClassName("ff");
		test.setDate("2016/10/01");
		test.setDescription("test equals");
		test.setDuration(23234l);
		Map<String, String> params = new HashMap<String, String>();
		params.put("param0", "val0");
		params.put("param1", "val1");
		test.setParameters(params);
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("prop0", "val0");
		properties.put("prop1", "val1");
		test.setProperties(properties);
		test.setStatus(Status.success);
		test.setTimestamp("16:59:30");
		return test;

	}

}
