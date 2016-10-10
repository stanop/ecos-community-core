package ru.citeck.ecos.utils.converter.amount;

/**
 * Russian realization of converter
 *
 * @author Roman.Makarskiy on 10.07.2016.
 */
class AmountInWordRuConverter extends AmountInWordConverter {

    private static final String ONE_HRYVNIA = "один гривна";
    private static final String FEMININE_ONE_HRYVNIA = "одна гривна";
    private static final String TWO_HRYVNIA = "два гривны";
    private static final String FEMININE_TWO_HRYVNIA = "две гривны";

    private static final String ONE_YEN = "один японская иена";
    private static final String FEMININE_ONE_YEN = "одна японская иена";
    private static final String TWO_YEN = "два японских иены";
    private static final String FEMININE_TWO_YEN = "две японских иены";

    @Override
    public String convert(double amount, String currencyCode) {
        String result = getFixedFeminineOfCurrency(super.convert(amount, currencyCode));
        result = result.substring(0, 1).toUpperCase() + result.substring(1);
        return result;
    }

    private String getFixedFeminineOfCurrency (String result) {
        result = result.toLowerCase();
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
