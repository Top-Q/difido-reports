package il.co.topq.report;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

public class DateTimeConverter {

	private static final DateTimeFormatter ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER = DateTimeFormatter
			.ofPattern("yyyy/MM/dd HH:mm:ss");

	public FromDate fromDateObject(Date date) {
		return new FromDate(date);
	}
	
	public FromDate fromNowDateObject() {
		return new FromDate(new Date());
	}

	public class FromDate {
		private final Date date;

		public FromDate(Date date) {
			super();
			if (null == date) {
				throw new IllegalStateException("No date object was defined");
			}

			this.date = date;
		}

		public String toElasticTimestampString() {
			return date.toInstant().atZone(ZoneId.systemDefault()).format(ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER);
		}
	}

	public class FromString {
		private final String dateString;

		public FromString(String dateString) {
			super();
			if (StringUtils.isEmpty(dateString)) {
				throw new IllegalStateException("No date string was defined");
			}
			this.dateString = dateString;
		}
		
		public Date toGMTDateObject() {
			ZonedDateTime gmtZonedDt = LocalDateTime.parse(dateString, ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER)
					.atZone(ZoneId.systemDefault())
					.withZoneSameInstant(ZoneId.of("GMT"));
			return toDateObject(gmtZonedDt.format(ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER));
		}
		
		private Date toDateObject(final String aDateString) {
			LocalDateTime ldt = LocalDateTime.parse(aDateString, ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER);
			return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
		}

		public Date toDateObject() {
			return toDateObject(this.dateString);
		}
	}

	public FromString fromString(String dateString) {
		return new FromString(dateString);
	}
}
