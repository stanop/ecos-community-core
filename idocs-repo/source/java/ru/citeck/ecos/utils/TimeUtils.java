package ru.citeck.ecos.utils;

import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.lang.time.DateUtils;
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
     * @param offset ISO 8601 Duration format with '-' at start if offset is negative. e.g. P10D - 10 days
     */
    public static Date getCurrentDateWithOffset(String offset) {

        String offsetExpression = offset;
        boolean isBackOffset = false;
        if (offsetExpression.startsWith("-")) {
            isBackOffset = true;
            offsetExpression = offsetExpression.substring(1);
        }

        Date currentDate = truncateByDurationPrecision(new Date(), offsetExpression);
        String currentISODate = ISO8601DateFormat.format(currentDate);

        Interval interval = Interval.parse(String.format("%s/%s", currentISODate, offsetExpression));
        if (isBackOffset) {
            long offsetMillis = interval.getEndMillis() - interval.getStartMillis();
            return new Date(interval.getStartMillis() - offsetMillis);
        } else {
            return interval.getEnd().toDate();
        }
    }

    public static Date truncateByDurationPrecision(Date date, String durationExpression) {
        return PrecisionByDuration.getPrecision(durationExpression).format(date);
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

        public Date format(Date input) {
            return DateUtils.truncate(input, field);
        }

        public static PrecisionByDuration getPrecision(String expression) {
            if (!expression.startsWith("P")) {
                throw new IllegalArgumentException("Duration expression must starts with 'P'. Expression: " + expression);
            }
            for (PrecisionByDuration precision : precisions) {
                if (precision.matches(expression)) {
                    return precision;
                }
            }
            throw new IllegalArgumentException("Expression must contains at least one of " +
                                               "Y, M, D, H, S symbols. expression: " + expression);
        }
    }
}
