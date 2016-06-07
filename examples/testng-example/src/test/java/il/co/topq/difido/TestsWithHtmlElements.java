package il.co.topq.difido;

import org.testng.annotations.Test;

import il.co.topq.difido.model.Enums.Status;

public class TestsWithHtmlElements extends AbstractDifidoTestCase {

	@Test(description = "Test with color paragraphs")
	public void testWithColorParagraphs() {
		report.logHtml("<p style=\"color:red\">This is in red</p>", Status.success);
		report.logHtml("<p style=\"color:orange\">This is in orange</p>", Status.success);
		report.logHtml("<p style=\"color:cyan\">This is in cyan</p>", Status.success);

		report.logHtml("<p style=\"background-color:red\">Background-color set by using red</p>", Status.success);
		report.logHtml("<p style=\"background-color:orange\">Background-color set by using orange</p>", Status.success);
		report.logHtml("<p style=\"background-color:cyan\">Background-color set by using cyan</p>", Status.success);
	}

	@Test(description = "Test with toggled table")
	public void testWithToggledTable() {
		report.log("Click to see table", getHtmlTable(), Status.success);
	}

	@Test(description = "Test with table")
	public void testWithTable() {
		report.logHtml(getHtmlTable(), Status.success);
	}
	
	@Test(description = "Test with table with failure")
	public void testWithTableWithFailure() {
		report.step("This should fail");
		report.logHtml(getHtmlTable(), Status.failure);
		report.logHtml("Click to see table",getHtmlTable(), Status.failure);
	}

	@Test(description = "Test with not rendered table")
	public void testWithNotRenderedTable() {
		report.step("The following table is not supposed to be rendered");
		report.log(getHtmlTable(), Status.success);
	}

	@Test(description = "Test with toggled form")
	public void testToggeledForm() {
		report.log("Click to see form", getHtmlForm(), Status.success);
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
