package ru.citeck.ecos.utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Convert Amount In Words class, essential for convert amount to amount in words.
 * <p>
 * The need to add this class occurred during the creation of templates for payments and closing documents
 *
 * @author Roman.Makarskiy on 23.04.2016.
 */
//TODO Refactoring, add support English language
public class ConvertAmountInWords {

    private static final String[][] ONE = {
            {"", "один", "два", "три", "четыре", "пять", "шесть", "семь", "восемь", "девять"},
            {"", "одна", "две", "три", "четыре", "пять", "шесть", "семь", "восемь", "девять"},
    };
    private static final String[] THOUSAND = {"", "сто", "двести", "триста", "четыреста", "пятьсот", "шестьсот", "семьсот", "восемьсот", "девятьсот"};
    private static final String[] TEN = {"", "десять", "одиннадцать", "двенадцать", "тринадцать", "четырнадцать", "пятнадцать", "шестнадцать", "семнадцать", "восемнадцать", "девятнадцать", "двадцать"};
    private static final String[] DECADE = {"", "десять", "двадцать", "тридцать", "сорок", "пятьдесят", "шестьдесят", "семьдесят", "восемьдесят", "девяносто"};

    private static String intact1, intact2, intact3;
    private static String fractional1, fractional2, fractional3;


    /**
     *  It return a amount in words
     * @param amount - amount to convert
     * @param currency - code of currency in ISO 4217 alpha 3 standard. Default - RUB
     * @return amount in words
     */
    public static String convert(double amount, String currency) {

        initializationCurrency(currency);

        String[][] DECLINATION = new String[][]{
                {fractional1, fractional2, fractional3, "1"},
                {intact1, intact2, intact3, "0"},
                {"тысяча", "тысячи", "тысяч", "1"},
                {"миллион", "миллиона", "миллионов", "0"},
                {"миллиард", "миллиарда", "миллиардов", "0"},
                {"триллион", "триллиона", "триллионов", "0"},
        };

        String s = String.valueOf(amount);
        if (!s.contains(".")) {
            s += ".0";
        }

        BigDecimal amountBig = new BigDecimal(s);
        ArrayList<Long> segments = new ArrayList<>();

        long total = amountBig.longValue();
        String[] divided = amountBig.toString().split("\\.");
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
            result = "ноль " + getDeclination(0, DECLINATION[1][0], DECLINATION[1][1], DECLINATION[1][2]);
            return result + " " + fraction + " " + getDeclination(fraction, DECLINATION[0][0], DECLINATION[0][1], DECLINATION[0][2]);
        }

        int amt = segments.size();
        for (Long segment : segments) {
            int kind = Integer.valueOf(DECLINATION[amt][3]);
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

            if (currentSegment > 99) result += THOUSAND[number1] + " ";
            if (number23 > 20) {
                result += DECADE[number2] + " ";
                result += ONE[kind][number3] + " ";
            } else {
                if (number23 > 9) result += TEN[number23 - 9] + " ";
                else result += ONE[kind][number3] + " ";
            }

            result += getDeclination(currentSegment, DECLINATION[amt][0], DECLINATION[amt][1], DECLINATION[amt][2]) + " ";
            amt--;
        }
        result = result + "" + fractions + " " + getDeclination(fraction, DECLINATION[0][0], DECLINATION[0][1], DECLINATION[0][2]);
        result = result.replaceAll(" {2,}", " ");
        result = result.substring(0, 1).toUpperCase() + result.substring(1);

        return result;
    }

    private static void initializationCurrency(String currency) {

        switch (currency) {
            case "USD": {
                fractional1 = "центр";
                fractional2 = "цента";
                fractional3 = "центов";

                intact1 = "доллар США";
                intact2 = "доллара США";
                intact3 = "долларов США";
                break;
            }
            case "RUB": {
                initializationRub();
                break;
            }
            case "EUR": {
                fractional1 = "цент";
                fractional2 = "цента";
                fractional3 = "центов";

                intact1 = "евро";
                intact2 = "евро";
                intact3 = "евро";
                break;
            }
            default: {
                initializationRub();
                break;
            }
        }
    }

    private static void initializationRub() {
        fractional1 = "копейка";
        fractional2 = "копейки";
        fractional3 = "копеек";

        intact1 = "рубль";
        intact2 = "рубля";
        intact3 = "рублей";
    }


    private static String getDeclination(long n, String form1, String from2, String form5) {
        n = Math.abs(n) % 100;
        long n1 = n % 10;
        if (n > 10 && n < 20) return form5;
        if (n1 > 1 && n1 < 5) return from2;
        if (n1 == 1) return form1;
        return form5;
    }
}
