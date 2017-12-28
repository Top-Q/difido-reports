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

	public FromString fromElasticString(String dateString) {
		if (StringUtils.isEmpty(dateString)) {
			throw new IllegalArgumentException("Date string can't be null");
		}
		return new FromString(LocalDateTime.parse(dateString, ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER));
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

		private final LocalDateTime ldt;

		public FromString(LocalDateTime ldt) {
			super();
			if (null == ldt) {
				throw new IllegalStateException("Date time is null");
			}
			this.ldt = ldt;
		}

		public Date toGMTDateObject() {
			ZonedDateTime gmtZonedDt = ldt.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("GMT"));
			String gmtZoneText = gmtZonedDt.format(ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER);
			LocalDateTime localTimeZone = LocalDateTime.parse(gmtZoneText, ELASTIC_SEARCH_TIMESTAMP_STRING_FORMATTER);
			return Date.from(localTimeZone.atZone(ZoneId.systemDefault()).toInstant());
		}

		public Date toDateObject() {
			return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
		}
	}

}
