package il.co.topq.report;

import java.text.SimpleDateFormat;

import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.transport.TransportClient;

public class Common {

	public static final String EXECUTION_REPORT_FOLDER_PREFIX = "exec";

	public static final SimpleDateFormat EXECUTION_REPROT_TIMESTAMP_FORMATTER = new SimpleDateFormat(
			"yyyy_MM_dd__HH_mm_ss_SS");

	public static final SimpleDateFormat API_DATE_FORMATTER = new SimpleDateFormat("dd/MM/yyyy");

	public static final SimpleDateFormat API_TIME_FORMATTER = new SimpleDateFormat("HH:mm:ss:SS");

	public static final SimpleDateFormat ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER = new SimpleDateFormat(
			"yyyy/MM/dd HH:mm:ss");

	public static final String REPORTS_FOLDER_NAME = "reports";

	public static TransportClient elasticsearchJavaClient;
	
	public static RestClient elasticsearchRestClient;

	public static final String ELASTIC_INDEX = "report";

	public static final String CONFIUGRATION_FOLDER_NAME = "config";

}
