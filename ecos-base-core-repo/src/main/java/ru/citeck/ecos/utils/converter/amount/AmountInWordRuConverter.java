package ru.citeck.ecos.utils.converter.amount;

import java.util.Locale;

/**
 * Russian realization of converter
 *
 * @author Roman.Makarskiy on 10.07.2016.
 */
class AmountInWordRuConverter extends AmountInWordConverter {

    protected String one_hryvnia;
    protected String feminine_one_hryvnia;
    protected String two_hryvnia;
    protected String feminine_two_hryvnia;

    protected String one_yen;
    protected String feminine_one_yen;
    protected String two_yen;
    protected String feminine_two_yen;

    public AmountInWordRuConverter(){
        locale = new Locale("ru", "");

        one_hryvnia = "один гривна";
        feminine_one_hryvnia = "одна гривна";
        two_hryvnia = "два гривны";
        feminine_two_hryvnia = "две гривны";

        one_yen = "один японская иена";
        feminine_one_yen = "одна японская иена";
        two_yen = "два японских иены";
        feminine_two_yen = "две японских иены";
    }

    @Override
    public String convert(double amount, String currencyCode) {
        String result = getFixedFeminineOfCurrency(super.convert(amount, currencyCode));
        result = result.substring(0, 1).toUpperCase() + result.substring(1);
        return result;
    }

    protected String getFixedFeminineOfCurrency (String result) {
        result = result.substring(0, 1).toLowerCase() + result.substring(1);
        if (result.contains(one_hryvnia)) {
            result = result.replace(one_hryvnia, feminine_one_hryvnia);
        }
        if (result.contains(two_hryvnia)) {
            result = result.replace(two_hryvnia, feminine_two_hryvnia);
        }
        if (result.contains(one_yen)) {
            result = result.replace(one_yen, feminine_one_yen);
        }
        if (result.contains(two_yen)) {
            result = result.replace(two_yen, feminine_two_yen);
        }
        return  result;
    }
}
