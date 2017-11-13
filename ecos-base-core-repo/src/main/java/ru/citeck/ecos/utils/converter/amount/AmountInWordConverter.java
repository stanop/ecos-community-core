package ru.citeck.ecos.utils.converter.amount;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Convert Amount In Words class, essential for convert numeric amount to amount in words.
 * <p>
 * The need to add this class occurred during the creation of templates for payments and closing documents
 *
 * @author Roman.Makarskiy on 10.07.2016.
 * @author Oleg.Onischuk on 11.11.2017. Ukrainian language realization, possibility to set specific language.
 */
public abstract class AmountInWordConverter {

    protected final ConverterResources resources = new ConverterResources();

    /**
     * Convert an amount to words using language from current locale
     *
     * @param amount   - amount to convert
     * @param currencyCode - code of currency in ISO 4217 alpha 3 standard.
     * @return amount in words
     */
    public String convert(double amount, String currencyCode) {
        resources.initializationResources(new CurrencyFactory().getCurrency(currencyCode));
        return processConvert(amount);
    }

    String getDecade(int position) {
        return resources.DECADE[position] + resources.INDENT;
    }

    String getOne(int kind, int position) {
        return resources.ONE[kind][position] + resources.INDENT;
    }

    String getThousand(int position) {
        return resources.THOUSAND[position] + resources.INDENT;
    }

    String getTen(int position) {
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

    protected String processConvert(double amount) {

        BigDecimal BigDecimalAmount = new BigDecimal(amount);
        BigDecimalAmount = BigDecimalAmount.setScale(2, BigDecimal.ROUND_HALF_DOWN);

        ArrayList<Long> segments = new ArrayList<>();

        long total = BigDecimalAmount.longValue();
        String[] divided = BigDecimalAmount.toString().split("\\.");
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

        String result = "";
        if (total == 0) {
            result = resources.zero + resources.INDENT + getDeclination(0, resources.DECLINATION[1][0], resources.DECLINATION[1][1], resources.DECLINATION[1][2]);
            return result + resources.INDENT + fraction + resources.INDENT + getDeclination(fraction, resources.DECLINATION[0][0], resources.DECLINATION[0][1], resources.DECLINATION[0][2]);
        }

        int amt = segments.size();
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

            if (currentSegment > 99) result += getThousand(number1) + resources.INDENT;
            if (number23 > 20) {
                result += getDecade(number2);
                result += getOne(kind, number3);
            } else {
                if (number23 > 9) result += getTen(number23 - 9);
                else result += getOne(kind, number3);
            }

            result += getDeclination(currentSegment, resources.DECLINATION[amt][0], resources.DECLINATION[amt][1], resources.DECLINATION[amt][2]) + resources.INDENT;
            amt--;
        }
        result = result + "" + fractions + resources.INDENT + getDeclination(fraction, resources.DECLINATION[0][0], resources.DECLINATION[0][1], resources.DECLINATION[0][2]);
        result = result.replaceAll(" {2,}", resources.INDENT);
        result = result.substring(0, 1).toUpperCase() + result.substring(1);

        return result;
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }

    public ConverterResources getResources() {
        return resources;
    }
}
