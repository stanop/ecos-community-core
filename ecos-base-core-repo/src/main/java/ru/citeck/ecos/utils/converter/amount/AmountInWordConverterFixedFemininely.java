package ru.citeck.ecos.utils.converter.amount;

/**
 * @author Roman Makarskiy
 */
public abstract class AmountInWordConverterFixedFemininely extends AmountInWordConverter {

    String one_hryvnia;
    String feminine_one_hryvnia;
    String two_hryvnia;
    String feminine_two_hryvnia;

    String one_yen;
    String feminine_one_yen;
    String two_yen;
    String feminine_two_yen;

    @Override
    public String convert(double amount, String currencyCode) {
        String result = getFixedFeminineOfCurrency(super.convert(amount, currencyCode));
        result = result.substring(0, 1).toUpperCase() + result.substring(1);
        return result;
    }

    private String getFixedFeminineOfCurrency(String result) {
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

        return result;
    }

}
