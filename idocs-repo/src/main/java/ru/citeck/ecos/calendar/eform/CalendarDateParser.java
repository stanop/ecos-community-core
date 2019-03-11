package ru.citeck.ecos.calendar.eform;

import org.alfresco.util.ISO8601DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

class CalendarDateParser {

    private static final SimpleDateFormat SLASH_TIME = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    private static final SimpleDateFormat SLASH_DATE = new SimpleDateFormat("yyyy/MM/dd");

    private static final SimpleDateFormat DASH_TIME = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final SimpleDateFormat DASH_DATE = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Gets the date from the String, trying the various formats
     * (New and Legacy) until one works...
     */
    static Date parseDate(String date) {

        // Is there one at all?
        if (date == null || date.length() == 0) {
            return null;
        }

        // Today's Date - special case
        if (date.equalsIgnoreCase("NOW")) {
            // We want all of today, so go back to midnight
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);
            return c.getTime();
        }

        // Try as ISO8601
        try {
            return ISO8601DateFormat.parse(date);
        } catch (Exception ignored) {
        }

        // Try YYYY/MM/DD
        try {
            return SLASH_TIME.parse(date);
        } catch (ParseException ignored) {
        }
        try {
            return SLASH_DATE.parse(date);
        } catch (ParseException ignored) {
        }

        // Try YYYY-MM-DD
        try {
            return DASH_TIME.parse(date);
        } catch (ParseException ignored) {
        }
        try {
            return DASH_DATE.parse(date);
        } catch (ParseException ignored) {
        }

        // We don't know what it is, object
        throw new RuntimeException("Invalid date '" + date + "'");
    }
}
