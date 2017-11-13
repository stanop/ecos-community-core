package ru.citeck.ecos.utils.converter.amount;

import java.util.Locale;

/**
 * English realization of converter
 *
 * @author Roman.Makarskiy on 10.07.2016.
 */
 class AmountInWordEnConverter extends AmountInWordConverter {

    private final Locale locale = new Locale("en", "");

    private static final String EN_DECADE_HYPHEN = "-";
    private static final String INDENT =" ";

    @Override
    String getDecade(int position) {
        String decade = super.getDecade(position);
        return decade.replace(INDENT, EN_DECADE_HYPHEN);
    }

    @Override
    public String convert(double amount, String currencyCode) {
        Currency currency = new CurrencyFactory().getCurrency(currencyCode, locale);
        resources.initializationResources(currency, locale);
        String result = processConvert(amount);
        return result.replace("- ", INDENT);
    }
}
