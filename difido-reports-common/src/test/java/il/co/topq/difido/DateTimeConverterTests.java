package il.co.topq.difido;

import static il.co.topq.difido.DateTimeConverter.fromDateObject;
import static il.co.topq.difido.DateTimeConverter.fromDateString;
import static il.co.topq.difido.DateTimeConverter.fromElasticString;
import static il.co.topq.difido.DateTimeConverter.fromTimeString;
import static org.junit.Assert.assertEquals;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

public class DateTimeConverterTests {

	@Test
	public void testDateToElasticString() throws ParseException {
		String expectedDateString = "2017/12/28 20:38:32";
		Date date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(expectedDateString);
		String actualDateString = fromDateObject(date).toElasticString();
		assertEquals(expectedDateString, actualDateString);
	}
	
	@Test
	public void testElasticStingToDate() throws ParseException {
		String dateString = "2017/12/28 20:38:32";
		Date actDate = fromElasticString(dateString).toDateObject();
		Date expDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse(dateString);
		assertEquals(actDate.getTime(), expDate.getTime());
	}

	@Test
	public void testElasticStingToGmtDate() throws ParseException {
		Date actDate = fromElasticString("2017/12/28 20:38:32").toGMTDateObject();
		Date expDate = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").parse("2017/12/28 18:38:32");
		assertEquals(actDate.getTime(), expDate.getTime());
	}
	
	@Test
	public void testDateConversion() {
		String expDateString = "21/11/2017";
		Date date = fromDateString(expDateString).toDateObject();
		String actDateString = fromDateObject(date).toDateString();
		assertEquals(expDateString, actDateString);
	}
	
	@Test
	public void testTimeConversion() {
		String expTimeString = "16:47:51:22";
		Date date = fromTimeString(expTimeString).toDateObject();
		String actTimeString = fromDateObject(date).toTimeString();
		assertEquals(expTimeString, actTimeString);
	}
	
	@Test
	public void testOldTimeConversion() {
		Date date = fromTimeString("16:47:51:222").toDateObject();
		String actTimeString = fromDateObject(date).toTimeString();
		assertEquals("16:47:51:22", actTimeString);
	}


	

}
