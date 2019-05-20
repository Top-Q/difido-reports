package il.co.topq.report.updater;

import org.h2.jdbcx.JdbcDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcTemplateBuilder {

	public static JdbcTemplate build() {
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:./docRoot/reports/database");
		ds.setUser("sa");
		ds.setPassword("");
		return new JdbcTemplate(ds);
	}

}
