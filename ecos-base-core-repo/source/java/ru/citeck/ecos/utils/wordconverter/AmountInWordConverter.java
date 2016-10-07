package ru.citeck.ecos.utils.wordconverter;

import org.springframework.extensions.surf.util.I18NUtil;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Roman on 10/7/2016.
 */
public abstract class AmountInWordConverter {

    private static final String NUMERAL_ZERO = "amount-in-word-converter.numeral.zero";
    private static final String NUMERAL_ONE = "amount-in-word-converter.numeral.one";
    private static final String NUMERAL_TWO = "amount-in-word-converter.numeral.two";
    private static final String NUMERAL_THREE = "amount-in-word-converter.numeral.three";
    private static final String NUMERAL_FOUR = "amount-in-word-converter.numeral.four";
    private static final String NUMERAL_FIVE = "amount-in-word-converter.numeral.five";
    private static final String NUMERAL_SIX = "amount-in-word-converter.numeral.six";
    private static final String NUMERAL_SEVEN = "amount-in-word-converter.numeral.seven";
    private static final String NUMERAL_EIGHT = "amount-in-word-converter.numeral.eight";
    private static final String NUMERAL_NINE = "amount-in-word-converter.numeral.nine";

    private static final String NUMERAL_DECLENSION_ONE = "amount-in-word-converter.numeral.declension.one";
    private static final String NUMERAL_DECLENSION_TWO = "amount-in-word-converter.numeral.declension.two";

    private static final String ONE_HUNDRED = "amount-in-word-converter.thousand.one-hundred";
    private static final String TWO_HUNDRED = "amount-in-word-converter.thousand.two-hundred";
    private static final String THREE_HUNDRED = "amount-in-word-converter.thousand.three-hundred";
    private static final String FOUR_HUNDRED = "amount-in-word-converter.thousand.four-hundred";
    private static final String FIVE_HUNDRED = "amount-in-word-converter.thousand.five-hundred";
    private static final String SIX_HUNDRED = "amount-in-word-converter.thousand.six-hundred";
    private static final String SEVEN_HUNDRED = "amount-in-word-converter.thousand.seven-hundred";
    private static final String EIGHT_HUNDRED = "amount-in-word-converter.thousand.eight-hundred";
    private static final String NINE_HUNDRED = "amount-in-word-converter.thousand.nine-hundred";

    private static final String TEN_TEN = "amount-in-word-converter.ten.ten";
    private static final String TEN_ELEVEN = "amount-in-word-converter.ten.eleven";
    private static final String TEN_TWELVE = "amount-in-word-converter.ten.twelve";
    private static final String TEN_THIRTEEN = "amount-in-word-converter.ten.thirteen";
    private static final String TEN_FOURTEEN = "amount-in-word-converter.ten.fourteen";
    private static final String TEN_FIFTEEN = "amount-in-word-converter.ten.fifteen";
    private static final String TEN_SIXTEEN = "amount-in-word-converter.ten.sixteen";
    private static final String TEN_SEVENTEEN = "amount-in-word-converter.ten.seventeen";
    private static final String TEN_EIGHTEEN = "amount-in-word-converter.ten.eighteen";
    private static final String TEN_NINETEEN = "amount-in-word-converter.ten.nineteen";
    private static final String TEN_TWENTY = "amount-in-word-converter.ten.twenty";

    private static final String DECADE_TEN = "amount-in-word-converter.decade.ten";
    private static final String DECADE_TWENTY = "amount-in-word-converter.decade.twenty";
    private static final String DECADE_THIRTY = "amount-in-word-converter.decade.thirty";
    private static final String DECADE_FORTY = "amount-in-word-converter.decade.forty";
    private static final String DECADE_FIFTY = "amount-in-word-converter.decade.fifty";
    private static final String DECADE_SIXTY = "amount-in-word-converter.decade.sixty";
    private static final String DECADE_SEVENTY = "amount-in-word-converter.decade.seventy";
    private static final String DECADE_EIGHTY = "amount-in-word-converter.decade.eighty";
    private static final String DECADE_NINETY = "amount-in-word-converter.decade.ninety";

    private static final String THOUSAND_DECLENSION_1 = "amount-in-word-converter.thousand.1";
    private static final String THOUSAND_DECLENSION_2 = "amount-in-word-converter.thousand.2";
    private static final String THOUSAND_DECLENSION_3 = "amount-in-word-converter.thousand.3";

    private static final String MILLION_DECLENSION_1 = "amount-in-word-converter.million.1";
    private static final String MILLION_DECLENSION_2 = "amount-in-word-converter.million.2";
    private final static String MILLION_DECLENSION_3 = "amount-in-word-converter.million.3";

    private static final String BILLION_DECLENSION_1 = "amount-in-word-converter.billion.1";
    private static final String BILLION_DECLENSION_2 = "amount-in-word-converter.billion.2";
    private static final String BILLION_DECLENSION_3 = "amount-in-word-converter.billion.3";

    private static final String TRILLION_DECLENSION_1 = "amount-in-word-converter.trillion.1";
    private static final String TRILLION_DECLENSION_2 = "amount-in-word-converter.trillion.2";
    private static final String TRILLION_DECLENSION_3 = "amount-in-word-converter.trillion.3";




    private String[][] ONE = {};
    private String[] THOUSAND = {};
    private String[] TEN = {};
    private String[] DECADE = {};

    private String thousand1, thousand2, thousand3;
    private String million1, million2, million3;
    private String billion1, billion2, billion3;
    private String trillion1, trillion2, trillion3;
    private String zero;

    public String convert(double amount, String currencyCode) {

        Currency currency = new CurrencyFactory().createCurrency(currencyCode);

        String[][] DECLINATION = new String[][]{
                {currency.getFractional1(), currency.getFractional2(), currency.getFractional3(), "1"},
                {currency.getIntact1(), currency.getIntact2(), currency.getIntact3(), "0"},
                {thousand1, thousand2, thousand3, "1"},
                {million1, million2, million3, "0"},
                {billion1, billion2, billion3, "0"},
                {trillion1, trillion2, trillion3, "0"},
        };

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
            result = zero + " " + getDeclination(0, DECLINATION[1][0], DECLINATION[1][1], DECLINATION[1][2]);
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

    private String getDeclination(long n, String form1, String from2, String form5) {
        n = Math.abs(n) % 100;
        long n1 = n % 10;
        if (n > 10 && n < 20) return form5;
        if (n1 > 1 && n1 < 5) return from2;
        if (n1 == 1) return form1;
        return form5;
    }

    void initializationStrings() {
        ONE = new String[][]{
                {"", I18NUtil.getMessage(NUMERAL_ONE),
                        I18NUtil.getMessage(NUMERAL_TWO),
                        I18NUtil.getMessage(NUMERAL_THREE),
                        I18NUtil.getMessage(NUMERAL_FOUR),
                        I18NUtil.getMessage(NUMERAL_FIVE),
                        I18NUtil.getMessage(NUMERAL_SIX),
                        I18NUtil.getMessage(NUMERAL_SEVEN),
                        I18NUtil.getMessage(NUMERAL_EIGHT),
                        I18NUtil.getMessage(NUMERAL_NINE)
                },
                {"", I18NUtil.getMessage(NUMERAL_DECLENSION_ONE),
                        I18NUtil.getMessage(NUMERAL_DECLENSION_TWO),
                        I18NUtil.getMessage(NUMERAL_THREE),
                        I18NUtil.getMessage(NUMERAL_FOUR),
                        I18NUtil.getMessage(NUMERAL_FIVE),
                        I18NUtil.getMessage(NUMERAL_SIX),
                        I18NUtil.getMessage(NUMERAL_SEVEN),
                        I18NUtil.getMessage(NUMERAL_EIGHT),
                        I18NUtil.getMessage(NUMERAL_NINE)
                }
        };
        THOUSAND = new String[]{"",
                I18NUtil.getMessage(ONE_HUNDRED),
                I18NUtil.getMessage(TWO_HUNDRED),
                I18NUtil.getMessage(THREE_HUNDRED),
                I18NUtil.getMessage(FOUR_HUNDRED),
                I18NUtil.getMessage(FIVE_HUNDRED),
                I18NUtil.getMessage(SIX_HUNDRED),
                I18NUtil.getMessage(SEVEN_HUNDRED),
                I18NUtil.getMessage(EIGHT_HUNDRED),
                I18NUtil.getMessage(NINE_HUNDRED)
        };
        TEN = new String[]{"",
                I18NUtil.getMessage(TEN_TEN),
                I18NUtil.getMessage(TEN_ELEVEN),
                I18NUtil.getMessage(TEN_TWELVE),
                I18NUtil.getMessage(TEN_THIRTEEN),
                I18NUtil.getMessage(TEN_FOURTEEN),
                I18NUtil.getMessage(TEN_FIFTEEN),
                I18NUtil.getMessage(TEN_SIXTEEN),
                I18NUtil.getMessage(TEN_SEVENTEEN),
                I18NUtil.getMessage(TEN_EIGHTEEN),
                I18NUtil.getMessage(TEN_NINETEEN),
                I18NUtil.getMessage(TEN_TWENTY)};
        DECADE = new String[]{"",
                I18NUtil.getMessage(DECADE_TEN),
                I18NUtil.getMessage(DECADE_TWENTY),
                I18NUtil.getMessage(DECADE_THIRTY),
                I18NUtil.getMessage(DECADE_FORTY),
                I18NUtil.getMessage(DECADE_FIFTY),
                I18NUtil.getMessage(DECADE_SIXTY),
                I18NUtil.getMessage(DECADE_SEVENTY),
                I18NUtil.getMessage(DECADE_EIGHTY),
                I18NUtil.getMessage(DECADE_NINETY)};

        thousand1 = I18NUtil.getMessage(THOUSAND_DECLENSION_1);
        thousand2 = I18NUtil.getMessage(THOUSAND_DECLENSION_2);
        thousand3 = I18NUtil.getMessage(THOUSAND_DECLENSION_3);

        million1 = I18NUtil.getMessage(MILLION_DECLENSION_1);
        million2 = I18NUtil.getMessage(MILLION_DECLENSION_2);
        million3 = I18NUtil.getMessage(MILLION_DECLENSION_3);

        billion1 = I18NUtil.getMessage(BILLION_DECLENSION_1);
        billion2 = I18NUtil.getMessage(BILLION_DECLENSION_2);
        billion3 = I18NUtil.getMessage(BILLION_DECLENSION_3);

        trillion1 = I18NUtil.getMessage(TRILLION_DECLENSION_1);
        trillion2 = I18NUtil.getMessage(TRILLION_DECLENSION_2);
        trillion3 = I18NUtil.getMessage(TRILLION_DECLENSION_3);

        zero = I18NUtil.getMessage(NUMERAL_ZERO);
    }

}
