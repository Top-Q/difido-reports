package il.co.topq.difido;

import org.junit.Test;

import jsystem.framework.TestProperties;
import junit.framework.SystemTestCase4;

public class TestsWithHtmlElements extends SystemTestCase4 {

	@Test
	@TestProperties(name = "Test with color paragraphs")
	public void testWithColorParagraphs() {
		report.reportHtml("Red Text", "<p style=\"color:red\">This is in red</p>", true);
		report.reportHtml("Orange Text", "<p style=\"color:orange\">This is in orange</p>", true);
		report.reportHtml("Cyan Text", "<p style=\"color:cyan\">This is in cyan</p>", true);

		report.reportHtml("Red Background", "<p style=\"background-color:red\">Background-color set by using red</p>",
				true);
		report.reportHtml("Orange Background",
				"<p style=\"background-color:orange\">Background-color set by using orange</p>", true);
		report.reportHtml("Cyan Background",
				"<p style=\"background-color:cyan\">Background-color set by using cyan</p>", true);
	}

	@Test
	@TestProperties(name = "Test with toggled table")
	public void testWithToggledTable() {
		report.reportHtml("Click to see table", getHtmlTable(), true);
	}

	@Test
	@TestProperties(name = "Test with table with failure")
	public void testWithTableWithFailure() {
		report.step("This should fail");
		report.reportHtml("Click to see table", getHtmlTable(), true);
	}

	@Test
	@TestProperties(name = "Test with not rendered table")
	public void testWithNotRenderedTable() {
		report.step("The following table is not supposed to be rendered");
		report.report(getHtmlTable());
	}

	@Test
	@TestProperties(name = "Test with toggled form")
	public void testToggeledForm() {
		report.reportHtml("Click to see form", getHtmlForm(), true);
	}

	private static String getHtmlTable() {
		final StringBuilder sb = new StringBuilder();
		sb.append("<table style=\"width:100%\"  border=\"1\">");
		sb.append("<tr>");
		sb.append("<td>Jill</td>");
		sb.append("<td>Smith</td> ");
		sb.append("<td>50</td>");
		sb.append("</tr>");
		sb.append("<tr>");
		sb.append("<td>Eve</td>");
		sb.append("<td>Jackson</td> ");
		sb.append("<td>94</td>");
		sb.append("</tr>");
		sb.append("</table>");
		return sb.toString();
	}

	private static String getHtmlForm() {
		final StringBuilder sb = new StringBuilder();
		sb.append("<form>");
		sb.append("First name:<br>");
		sb.append("<input type=\"text\" name=\"firstname\">");
		sb.append("<br>");
		sb.append(" Last name:<br>");
		sb.append("<input type=\"text\" name=\"lastname\">");
		sb.append("</form>");
		return sb.toString();
	}

}
