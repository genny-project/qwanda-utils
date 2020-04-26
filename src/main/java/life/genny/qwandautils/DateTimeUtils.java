package life.genny.qwandautils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

public class DateTimeUtils {

	
	static public LocalDate getLocalDateFromString(final String dateStr)
	{
		LocalDate ld = LocalDate.parse(dateStr );
		return ld;
	}	
	
	static public LocalDateTime getLocalDateTimeFromString(final String dateTimeStr)
	{
		return getLocalDateTimeFromString(dateTimeStr,ZoneOffset.UTC);
	}	
	
	static public  LocalDateTime getLocalDateTimeFromString(final String dateTimeStr, ZoneOffset zoneOffset)
	{
		TemporalAccessor ta = DateTimeFormatter.ISO_INSTANT.parse(dateTimeStr);
	    Instant i = Instant.from(ta);
	    LocalDateTime dt  = LocalDateTime.ofInstant(i, zoneOffset);
	    return dt;
	}
	
	static public String getNiceDateStr(LocalDate dt)
	{
		return getNiceDateStr(dt,"EEEE, MMMM d");
	}
	
	static public String getNiceDateStr(LocalDate dt, String pattern)
	{
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		String formattedString = dt.format(formatter);
		return formattedString;
	}
	
	
}
