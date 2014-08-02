package il.co.topq.report;

import java.text.SimpleDateFormat;

public class Common {

	public static final String EXECUTION_REPORT_FOLDER_PREFIX = "execution";

	public static final SimpleDateFormat EXECUTION_REPROT_TIMESTAMP_FORMATTER = new SimpleDateFormat(
			"yyyy_MM_dd__HH_mm_ss_SS");

	public static final SimpleDateFormat API_DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyy");

	public static final SimpleDateFormat API_TIME_FORMATTER = new SimpleDateFormat("HH:mm:ss:SS");
	
	public static final String REPORTS_FOLDER_NAME = "reports";

}
