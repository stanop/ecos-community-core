package ru.citeck.ecos.utils.converter.amount;

import org.springframework.extensions.surf.util.I18NUtil;

/**
 * @author Roman.Makarskiy on 10.07.2016.
 */
class ConverterResources {

    String[][] ONE = {};
    String[] THOUSAND = {};
    String[] TEN = {};
    String[] DECADE = {};
    String[][] DECLINATION = {};
    final String INDENT = " ";
    String zero;

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
    private static final String MILLION_DECLENSION_3 = "amount-in-word-converter.million.3";

    private static final String BILLION_DECLENSION_1 = "amount-in-word-converter.billion.1";
    private static final String BILLION_DECLENSION_2 = "amount-in-word-converter.billion.2";
    private static final String BILLION_DECLENSION_3 = "amount-in-word-converter.billion.3";

    private static final String TRILLION_DECLENSION_1 = "amount-in-word-converter.trillion.1";
    private static final String TRILLION_DECLENSION_2 = "amount-in-word-converter.trillion.2";
    private static final String TRILLION_DECLENSION_3 = "amount-in-word-converter.trillion.3";

    void initializationResources(Currency currency) {

        DECLINATION = new String[][]{
                {currency.getFractional1(), currency.getFractional2(), currency.getFractional3(), "1"},
                {currency.getIntact1(), currency.getIntact2(), currency.getIntact3(), "0"},
                {
                        I18NUtil.getMessage(THOUSAND_DECLENSION_1),
                        I18NUtil.getMessage(THOUSAND_DECLENSION_2),
                        I18NUtil.getMessage(THOUSAND_DECLENSION_3), "1"
                },
                {
                        I18NUtil.getMessage(MILLION_DECLENSION_1),
                        I18NUtil.getMessage(MILLION_DECLENSION_2),
                        I18NUtil.getMessage(MILLION_DECLENSION_3), "0"
                },
                {
                        I18NUtil.getMessage(BILLION_DECLENSION_1),
                        I18NUtil.getMessage(BILLION_DECLENSION_2),
                        I18NUtil.getMessage(BILLION_DECLENSION_3), "0"
                },
                {
                        I18NUtil.getMessage(TRILLION_DECLENSION_1),
                        I18NUtil.getMessage(TRILLION_DECLENSION_2),
                        I18NUtil.getMessage(TRILLION_DECLENSION_3), "0"
                },
        };

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



        zero = I18NUtil.getMessage(NUMERAL_ZERO);
    }

}
