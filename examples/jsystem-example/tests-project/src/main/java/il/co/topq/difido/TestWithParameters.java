package il.co.topq.difido;

import java.io.File;
import java.util.Date;

import org.junit.Test;

import jsystem.framework.TestProperties;
import junit.framework.SystemTestCase4;

public class TestWithParameters extends SystemTestCase4 {

	private int i;
	private String s;
	private float f;
	private long l;
	private File file;
	private Date date;

	@Test
	@TestProperties(name = "Test with parameters")
	public void testWithParameters() {
		report.report(String.valueOf(i));
		report.report(String.valueOf(f));
		report.report(String.valueOf(l));
		report.report(s);
		report.report(file != null ? file.getAbsolutePath() : "");
		report.report(file != null ? date.toString() : "");

	}

	public int getI() {
		return i;
	}

	public void setI(int i) {
		this.i = i;
	}

	public String getS() {
		return s;
	}

	public void setS(String s) {
		this.s = s;
	}

	public float getF() {
		return f;
	}

	public void setF(float f) {
		this.f = f;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public long getL() {
		return l;
	}

	public void setL(long l) {
		this.l = l;
	}

}
