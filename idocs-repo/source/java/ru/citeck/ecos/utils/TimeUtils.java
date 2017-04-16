package ru.citeck.ecos.utils;

import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.util.ParameterCheck;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.poi.util.StringUtil;
import org.joda.time.Interval;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Pavel Simonov
 */
public class TimeUtils {

    /**
     * @param date date to shift
     * @param offset ISO8601 Duration format with '-' at start if offset is negative.
     *               e.g. P10D == 10 days, -PT10M == -10 minutes
     */
    public static Date shiftDate(Date date, String offset) {

        ParameterCheck.mandatory("date", date);
        if (StringUtils.isBlank(offset)) return date;

        String offsetExpression = offset;
        boolean isBackOffset = false;
        if (offsetExpression.startsWith("-")) {
            isBackOffset = true;
            offsetExpression = offsetExpression.substring(1);
        }

        String currentISODate = ISO8601DateFormat.format(date);

        Interval interval = Interval.parse(String.format("%s/%s", currentISODate, offsetExpression));
        if (isBackOffset) {
            long offsetMillis = interval.getEndMillis() - interval.getStartMillis();
            return new Date(interval.getStartMillis() - offsetMillis);
        } else {
            return interval.getEnd().toDate();
        }
    }

    /**
     * @param duration - ISO8601 duration
     * @return Calendar code of smallest field. e.g. P1Y1M1D -> Day, P1DT0H0M -> Minute
     * @see Calendar
     */
    public static int getDurationPrecision(String duration) {
        if (StringUtils.isBlank(duration)) {
            return Calendar.MILLISECOND;
        }
        return PrecisionByDuration.getPrecision(duration).getField();
    }

    private static class PrecisionByDuration {

        private static final List<PrecisionByDuration> precisions = new ArrayList<PrecisionByDuration>() {{
            add(new PrecisionByDuration("S", Calendar.SECOND));
            add(new PrecisionByDuration("T.*[0-9]+M", Calendar.MINUTE));
            add(new PrecisionByDuration("H", Calendar.HOUR));
            add(new PrecisionByDuration("D", Calendar.DATE));
            add(new PrecisionByDuration("^[^T]*[0-9]+M", Calendar.MONTH));
            add(new PrecisionByDuration("Y", Calendar.YEAR));
        }};

        private final Pattern pattern;
        private final int field;

        private PrecisionByDuration(String pattern, int field) {
            this.pattern = Pattern.compile(pattern);
            this.field = field;
        }

        private boolean matches(String expression) {
            return pattern.matcher(expression).find();
        }

        public int getField() {
            return field;
        }

        static PrecisionByDuration getPrecision(String duration) {
            if (!duration.startsWith("P") && !duration.startsWith("-P")) {
                throw new IllegalArgumentException("Duration must starts with 'P' or '-P'. " +
                                                   "Duration: " + duration);
            }
            for (PrecisionByDuration precision : precisions) {
                if (precision.matches(duration)) {
                    return precision;
                }
            }
            throw new IllegalArgumentException("Duration must contains at least one of " +
                                               "Y, M, D, H, S symbols. Duration: " + duration);
        }
    }
}
