package ru.citeck.ecos.utils.converter.amount;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

/**
 * Convert Amount In Words class, essential for convert numeric amount to amount in words.
 * The maximum degree of the number - is trillions.
 *
 * @author Roman.Makarskiy on 10.07.2016.
 * @author Oleg.Onischuk on 11.11.2017. Ukrainian language realization, possibility to set specific language.
 * @author Andrey.Platunov on 21.03.2018. Added possibility to convert integer amount only (with no currency).
 */
public abstract class AmountInWordConverter {

    private static final int MAX_SEGMENTS_COUNT = 5;

    private final ConverterResources resources = new ConverterResources();

    Locale locale;

    /**
     * Convert an integer amount to words using language from current locale
     *
     * @param amount       - amount to convert
     * @return amount in words
     */
    public String convert(int amount) {
        resources.initializationResources(locale);
        return processConvert(amount);
    }

    /**
     * Convert an amount to words using language from current locale
     *
     * @param amount       - amount to convert
     * @param currencyCode - code of currency in ISO 4217 alpha 3 standard.
     *                     using USD if currency is not supported by converter.
     *                     Supported currency codes: USD, RUB, RUR, EUR, BYR, GBP, GPY, UAH
     * @return amount in words
     */
    public String convert(double amount, String currencyCode) {
        Currency currency = new CurrencyFactory().getCurrency(currencyCode, locale);
        resources.initializationResources(currency, locale);
        return processConvert(amount);
    }

    String getDecade(int position) {
        return resources.DECADE[position] + resources.INDENT;
    }

    private String getOne(int kind, int position) {
        return resources.ONE[kind][position] + resources.INDENT;
    }

    private String getThousand(int position) {
        return resources.THOUSAND[position] + resources.INDENT;
    }

    private String getTen(int position) {
        return resources.TEN[position] + resources.INDENT;
    }

    private String getDeclination(long n, String form1, String from2, String form5) {
        n = Math.abs(n) % 100;
        long n1 = n % 10;
        if (n > 10 && n < 20) return form5;
        if (n1 > 1 && n1 < 5) return from2;
        if (n1 == 1) return form1;
        return form5;
    }

    private String processConvert(int amount) {
        BigDecimal bigDecimalAmount = new BigDecimal(amount);
        return processConvert(bigDecimalAmount);
    }

    private String processConvert(double amount) {
        BigDecimal bigDecimalAmount = new BigDecimal(amount);
        return processConvert(bigDecimalAmount);
    }
    
    private String processConvert(BigDecimal bigDecimalAmount) {
        bigDecimalAmount = bigDecimalAmount.setScale(2, BigDecimal.ROUND_HALF_DOWN);

        ArrayList<Long> segments = new ArrayList<>();

        long total = bigDecimalAmount.longValue();
        String[] divided = bigDecimalAmount.toString().split("\\.");
        divided[1] = divided[1].substring(0, 2);
        long fraction = Long.valueOf(divided[1]);
        if (!divided[1].substring(0, 1).equals("0")) {
            if (fraction < 10)
                fraction *= 10;
        }

        String fractions = String.valueOf(fraction);
        if (fractions.length() == 1)
            fractions = "0" + fractions;
        long totalSegment = total;

        while (totalSegment > 999) {
            long seg = totalSegment / 1000;
            segments.add(totalSegment - (seg * 1000));
            totalSegment = seg;
        }
        segments.add(totalSegment);
        Collections.reverse(segments);

        StringBuilder result = new StringBuilder();
        if (total == 0) {
            result = new StringBuilder(resources.zero + resources.INDENT + getDeclination(0, resources.DECLINATION[1][0],
                    resources.DECLINATION[1][1], resources.DECLINATION[1][2]));
            return result + resources.INDENT + fraction + resources.INDENT + getDeclination(fraction,
                    resources.DECLINATION[0][0], resources.DECLINATION[0][1], resources.DECLINATION[0][2]);
        }

        int amt = segments.size();

        if (amt > MAX_SEGMENTS_COUNT) {
            throw new IllegalArgumentException("Amount (" + bigDecimalAmount + ") to convert is too large. " +
                    "The maximum degree of the number - is trillions.");
        }

        for (Long segment : segments) {
            int kind = Integer.valueOf(resources.DECLINATION[amt][3]);
            int currentSegment = Integer.valueOf(segment.toString());
            if (currentSegment == 0 && amt > 1) {
                amt--;
                continue;
            }
            String stringNumber = String.valueOf(currentSegment);

            if (stringNumber.length() == 1) stringNumber = "00" + stringNumber;
            if (stringNumber.length() == 2) stringNumber = "0" + stringNumber;

            int number1 = Integer.valueOf(stringNumber.substring(0, 1));
            int number2 = Integer.valueOf(stringNumber.substring(1, 2));
            int number3 = Integer.valueOf(stringNumber.substring(2, 3));
            int number23 = Integer.valueOf(stringNumber.substring(1, 3));

            if (currentSegment > 99) result.append(getThousand(number1)).append(resources.INDENT);
            if (number23 > 20) {
                result.append(getDecade(number2));
                result.append(getOne(kind, number3));
            } else {
                if (number23 > 9) result.append(getTen(number23 - 9));
                else result.append(getOne(kind, number3));
            }

            result.append(getDeclination(currentSegment, resources.DECLINATION[amt][0], resources.DECLINATION[amt][1],
                    resources.DECLINATION[amt][2])).append(resources.INDENT);
            amt--;
        }
        result.append("").append(fractions).append(resources.INDENT).append(getDeclination(fraction,
                resources.DECLINATION[0][0],
                resources.DECLINATION[0][1], resources.DECLINATION[0][2]));
        result = new StringBuilder(result.toString().replaceAll(" {2,}", resources.INDENT));
        result = new StringBuilder(result.substring(0, 1).toUpperCase() + result.substring(1));

        return result.toString();
    } 

    @Override
    public String toString() {
        return this.getClass().getName();
    }

    public ConverterResources getResources() {
        return resources;
    }
}
