package il.co.topq.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum Configuration {

	INSTANCE;

	public enum ConfigProps {

		// @formatter:off
		DOC_ROOT_FOLDER("doc.root.folder", "docRoot"),
		KIBANA_URL("kibana.url","http://localhost:5601/app/kibana#/dashboard/Main?_g=(refreshInterval:(display:Off,pause:!f,value:0),time:(from:now-7d,mode:quick,to:now))&_a=(filters:!(),options:(darkTheme:!t),panels:!((col:8,id:Status-per-Test,panelIndex:1,row:3,size_x:5,size_y:3,type:visualization),(col:8,id:Number-of-Tests,panelIndex:2,row:1,size_x:5,size_y:2,type:visualization),(col:1,id:Tests-per-Execution,panelIndex:3,row:1,size_x:7,size_y:5,type:visualization),(col:10,id:Top-Duration-of-Tests,panelIndex:4,row:20,size_x:3,size_y:3,type:visualization),(col:1,id:Average-duration-per-test,panelIndex:5,row:6,size_x:7,size_y:5,type:visualization),(col:8,id:Number-of-tests-per-execution,panelIndex:6,row:6,size_x:5,size_y:4,type:visualization),(col:8,id:Learn-query-string,panelIndex:7,row:10,size_x:5,size_y:1,type:visualization),(col:1,id:Execution-comparison,panelIndex:9,row:11,size_x:9,size_y:6,type:visualization),(col:10,id:Most-frequent-test-failures,panelIndex:10,row:11,size_x:3,size_y:6,type:visualization),(col:1,id:Test-status-per-execution,panelIndex:11,row:17,size_x:12,size_y:3,type:visualization),(col:1,id:Duration-of-last-executions,panelIndex:12,row:20,size_x:9,size_y:3,type:visualization)),query:(query_string:(analyze_wildcard:!t,query:'*')),title:Main,uiState:(P-1:(vis:(colors:(error:%23E24D42,failure:%23EF843C,success:%237EB26D,warning:%23EAB839),legendOpen:!f)),P-3:(vis:(colors:(error:%23E24D42,failure:%23EF843C,success:%237EB26D,warning:%23EAB839),legendOpen:!f))))"),
		KIBANA_FIELDS("kibana.fields","machine;properties.Class"),
		MAX_EXECUTION_IDLE_TIME_IN_SEC("max.execution.idle.time.in.seconds","600"),
		STORE_IN_ELASTIC_ONLY_AT_EXECUTION_END("store.in.elastic.only.at.execution.end", "false"),
		ELASTIC_ENABLED("elastic.enabled","true"),
		ELASTIC_HOST("elastic.host","localhost"),
		ELASTIC_HTTP_PORT("elastic.http.port","9200"),
		ELASTIC_TRANSPORT_TCP_PORT("elastic.transport.tcp.port","9300"),
		ENABLE_HTML_REPORTS("enable.html.reports", "true"), 
		ENABLE_ARCHIVED_RESOURCES("enable.archived.resources","true"),
		DAYS_TO_KEEP_HTML_REPORTS("days.to.keep.html.reports","0"),
		EXTERNAL_LINKS("external.links",""),
		ENABLE_MAIL("enable.mail", "false"), MAIL_USER_NAME("mail.user.name",""), 
		MAIL_PASSWORD("mail.password", ""),
		MAIL_SSL("mail.ssl","false"), 
		MAIL_SMTP_HOST("mail.smtp.host", ""),
		MAIL_SMTP_PORT("mail.smtp.port", ""),
		MAIL_FROM_ADDRESS("mail.from.address",""), 
		MAIL_TO_ADDRESS("mail.to.address",""),
		MAIL_CC_ADDRESS("mail.cc.address", ""),
		EXECUTION_TABLE_HEADERS("execution.table.headers", ""),
		
		/**
		 * semicolon separated list of custom properties that can be added to
		 * each execution. If none was specified, no filter will be applied and
		 * clients could add any property
		 */
		CUSTOM_EXECUTION_PROPERTIES("custom.execution.properties", ""),
		PLUGIN_CLASSES("plugin.classes", "il.co.topq.report.plugins.mail.DefaultMailPlugin"),
		LAST_REPORTS_INTERVAL_IN_SEC("last.reports.interval.in.sec", "10"),
		LAST_REPORTS_NUM_OF_EXECUTIONS("last.reports.num.of.executions", "4"),
		LAST_REPORTS_FILTER("last.reports.filter","");
		// @formatter:off

		private final String propName;

		private final String defaultValue;

		private ConfigProps(String value, String defaultValue) {
			this.propName = value;
			this.defaultValue = defaultValue;
		}

		public String getPropName() {
			return propName;
		}

		public String getDefaultValue() {
			return defaultValue;
		}

	}

	private final Logger log = LoggerFactory.getLogger(Configuration.class);

	private final static String CONFIG_PROP_NAME = "difido_config.properties";

	private Properties configProperties = new Properties();

	private Configuration() {
		if (!new File(Common.CONFIUGRATION_FOLDER_NAME, CONFIG_PROP_NAME).exists()) {
			useDefaultProperties();
			return;
		}
		readConfigurationFromFile();
		if (configProperties.isEmpty()) {
			useDefaultProperties();
		}

	}

	private void readConfigurationFromFile() {
		try (FileReader reader = new FileReader(new File(Common.CONFIUGRATION_FOLDER_NAME, CONFIG_PROP_NAME))) {
			configProperties.load(reader);

		} catch (Exception e) {
			log.warn("Failure in reading file " + CONFIG_PROP_NAME + ". Rolling back to default properties", e);
		}

	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Object key : configProperties.keySet()) {
			sb.append(key).append(":").append(configProperties.getProperty(String.valueOf(key))).append("\n");
		}
		return sb.toString();
	}

	private void useDefaultProperties() {
		log.info("No configuration file found - Creating one with default parameters in "
				+ new File(Common.CONFIUGRATION_FOLDER_NAME, CONFIG_PROP_NAME).getAbsolutePath());
		for (ConfigProps prop : ConfigProps.values()){
			addPropWithDefaultValue(prop);
		}
		try (FileOutputStream out = new FileOutputStream(
				new File(Common.CONFIUGRATION_FOLDER_NAME, CONFIG_PROP_NAME))) {
			configProperties.store(out, "Default difido server properties");
		} catch (Exception e) {
			log.warn("Failed writing default configuration file", e);
		}
	}

	private void addPropWithDefaultValue(ConfigProps configProp) {
		configProperties.put(configProp.getPropName(), configProp.getDefaultValue());
	}

	public boolean readBoolean(ConfigProps prop) {
		return !"false".equals(readString(prop));
	}

	public int readInt(ConfigProps prop) {
		final String value = readString(prop);
		if (value != null && !value.isEmpty()) {
			return Integer.parseInt(value);
		}
		return 0;
	}

	public List<String> readList(ConfigProps prop) {
		final String value = configProperties.getProperty(prop.getPropName());
		if (StringUtils.isEmpty(value)) {
			return new ArrayList<String>();
		}
		return Arrays.asList(value.split(";"));
	}

	public String readString(ConfigProps prop) {
		final String value = configProperties.getProperty(prop.getPropName());
		if (null == value) {
			return prop.getDefaultValue();
		}
		return value.trim();
	}
	
}