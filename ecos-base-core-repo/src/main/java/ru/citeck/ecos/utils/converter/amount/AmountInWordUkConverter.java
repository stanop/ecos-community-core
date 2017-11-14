package ru.citeck.ecos.utils.converter.amount;

import java.util.Locale;

/**
 * Ukrainian realization of converter
 *
 * @author Oleg.Onischuk on 11.11.2017.
 */
class AmountInWordUkConverter extends AmountInWordConverter {

    private static final String ONE_HRYVNIA = "один гривня";
    private static final String FEMININE_ONE_HRYVNIA = "одна гривня";
    private static final String TWO_HRYVNIA = "два гривні";
    private static final String FEMININE_TWO_HRYVNIA = "дві гривні";

    private static final String ONE_YEN = "один японська єна";
    private static final String FEMININE_ONE_YEN = "одна японська єна";
    private static final String TWO_YEN = "два японських єни";
    private static final String FEMININE_TWO_YEN = "дві японських єни";

    public AmountInWordUkConverter(){
        locale = new Locale("uk", "");
    }

    @Override
    public String convert(double amount, String currencyCode) {
        String result = getFixedFeminineOfCurrency(super.convert(amount, currencyCode));
        result = result.substring(0, 1).toUpperCase() + result.substring(1);
        return result;
    }

    private String getFixedFeminineOfCurrency (String result) {
        result = result.substring(0, 1).toLowerCase() + result.substring(1);
        if (result.contains(ONE_HRYVNIA)) {
            result = result.replace(ONE_HRYVNIA, FEMININE_ONE_HRYVNIA);
        }
        if (result.contains(TWO_HRYVNIA)) {
            result = result.replace(TWO_HRYVNIA, FEMININE_TWO_HRYVNIA);
        }
        if (result.contains(ONE_YEN)) {
            result = result.replace(ONE_YEN, FEMININE_ONE_YEN);
        }
        if (result.contains(TWO_YEN)) {
            result = result.replace(TWO_YEN, FEMININE_TWO_YEN);
        }
        return  result;
    }
}
