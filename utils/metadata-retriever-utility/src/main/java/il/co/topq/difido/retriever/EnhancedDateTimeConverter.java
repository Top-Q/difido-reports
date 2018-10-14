package il.co.topq.difido.retriever;

import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;

/**
 * Utility class for converting date and time objects to text representation of
 * different kinds and vice versa.
 * 
 * @author Itai.Agmon
 *
 */
public class EnhancedDateTimeConverter {

	private static final DateTimeFormatter ELASTICSEARCH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

	private static final DateTimeFormatter ELASTICSEARCH_FORMATTER_SUFFIX = DateTimeFormatter
			.ofPattern("yyyy/MM/dd HH:mm:ss:");

	private static final DateTimeFormatter REVERSE_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

	private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss:SS");

	private static final DateTimeFormatter TIME_FORMATTER_OLD = DateTimeFormatter.ofPattern("HH:mm:ss:SSS");

	private EnhancedDateTimeConverter() {
		// Static builder
	}

	public static FromDate fromDateObject(Date date) {
		return new FromDate(date);
	}

	public static FromDate fromNowDateObject() {
		return new FromDate(new Date());
	}

	public static FromDateTimeString fromElasticString(String dateString) {
		if (null == dateString || dateString.isEmpty()) {
			throw new IllegalArgumentException("Date string can't be null");
		}
		LocalDateTime ldt = null;
		try {
			ldt = LocalDateTime.parse(dateString, ELASTICSEARCH_FORMATTER);
		} catch (DateTimeParseException e) {
			System.out.println("Failed " + dateString);
			ldt = LocalDateTime.parse(dateString, ELASTICSEARCH_FORMATTER_SUFFIX);
		}
		return new FromDateTimeString(ldt);
	}

	public static FromDateString fromDateString(String dateString) {
		if (null == dateString || dateString.isEmpty()) {
			throw new IllegalArgumentException("Date string can't be null");
		}
		return new FromDateString(LocalDate.parse(dateString, DATE_FORMATTER));
	}

	public static FromDateString fromReverseDateString(String dateString) {
		if (null == dateString || dateString.isEmpty()) {
			throw new IllegalArgumentException("Date string can't be null");
		}
		return new FromDateString(LocalDate.parse(dateString, REVERSE_DATE_FORMATTER));
	}

	public static FromTimeString fromTimeString(String timeString) {
		if (null == timeString || timeString.isEmpty()) {
			throw new IllegalArgumentException("Time string can't be null");
		}
		LocalTime lt = null;
		try {
			lt = LocalTime.parse(timeString, TIME_FORMATTER);
		} catch (DateTimeParseException e) {
			lt = LocalTime.parse(timeString, TIME_FORMATTER_OLD);
		}
		return new FromTimeString(lt);
	}

	public static class FromDate {
		private final Date date;

		public FromDate(Date date) {
			super();
			if (null == date) {
				throw new IllegalStateException("No date object was defined");
			}

			this.date = date;
		}

		public String toElasticString() {
			return date.toInstant().atZone(ZoneId.systemDefault()).format(ELASTICSEARCH_FORMATTER);
		}

		public String toDateString() {
			return date.toInstant().atZone(ZoneId.systemDefault()).format(DATE_FORMATTER);
		}

		public String toReverseDateString() {
			return date.toInstant().atZone(ZoneId.systemDefault()).format(REVERSE_DATE_FORMATTER);
		}

		public String toTimeString() {
			return date.toInstant().atZone(ZoneId.systemDefault()).format(TIME_FORMATTER);
		}
	}

	public static class FromDateString {

		private final LocalDate ld;

		public FromDateString(LocalDate ld) {
			super();
			if (null == ld) {
				throw new IllegalStateException("Date is null");
			}
			this.ld = ld;
		}

		public Date toDateObject() {
			return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant());
		}

		public LocalDate toLocalDate() {
			return ld;
		}

	}

	public static class FromTimeString {

		private final LocalTime lt;

		public FromTimeString(LocalTime lt) {
			super();
			if (null == lt) {
				throw new IllegalStateException("Time is null");
			}
			this.lt = lt;
		}

		public Date toDateObject() {
			Instant instant = lt.atDate(LocalDate.now()).atZone(ZoneId.systemDefault()).toInstant();
			return toDate(instant);
		}

		private Date toDate(Instant instant) {
			BigInteger milis = BigInteger.valueOf(instant.getEpochSecond()).multiply(BigInteger.valueOf(1000));
			milis = milis.add(BigInteger.valueOf(instant.getNano()).divide(BigInteger.valueOf(1_000_000)));
			return new Date(milis.longValue());
		}

		public LocalTime toLocalTime() {
			return lt;
		}

	}

	public static class FromDateTimeString {

		private final LocalDateTime ldt;

		public FromDateTimeString(LocalDateTime ldt) {
			super();
			if (null == ldt) {
				throw new IllegalStateException("Date time is null");
			}
			this.ldt = ldt;
		}

		public Date toGMTDateObject() {
			ZonedDateTime gmtZonedDt = ldt.atZone(ZoneId.systemDefault()).withZoneSameInstant(ZoneId.of("GMT"));
			String gmtZoneText = gmtZonedDt.format(ELASTICSEARCH_FORMATTER);
			LocalDateTime localTimeZone = LocalDateTime.parse(gmtZoneText, ELASTICSEARCH_FORMATTER);
			return Date.from(localTimeZone.atZone(ZoneId.systemDefault()).toInstant());
		}

		public Date toDateObject() {
			return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
		}

		public LocalDateTime toLocalDateTime() {
			return ldt;
		}
	}

}
