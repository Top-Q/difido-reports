package il.co.topq.difido;

import java.io.File;

public interface TestNGReporter {

	public enum Style {
		REGULAR("r"), BOLD("b"), ITALIC("i");

		private final String value;

		private Style(String value) {
			this.value = value;
		}

		public String getValue() {
			return value;
		}
		
	}

	public enum Color {
		RED, BLUE, YELLOW, GREEN
	}

	public void log(String s);

	public void log(final String s, Style style);

	public void log(final String s, Style style, Color color);

	public void log(String title, String body);

	public void log(String title, String body, Color color);

	public void startLogToggle(String title);

	public void startLogToggle(String title, Color color);

	public void stopLogToggle();

	public void logImage(String title, final File file);

	public void logFile(String title, final File file);
}
