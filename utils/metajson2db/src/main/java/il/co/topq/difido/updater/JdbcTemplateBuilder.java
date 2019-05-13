package il.co.topq.difido.updater;

import org.h2.jdbcx.JdbcDataSource;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcTemplateBuilder {

	public static JdbcTemplate build() {
		JdbcDataSource ds = new JdbcDataSource();
		ds.setURL("jdbc:h2:./database");
		ds.setUser("sa");
		ds.setPassword("");
		return new JdbcTemplate(ds);
	}

}
