package ru.citeck.ecos.utils.wordconverter;

/**
 * English realization of converter
 *
 * @author Roman.Makarskiy on 10.07.2016.
 */
 class AmountInWordEnConverter extends AmountInWordConverter {
    private static final String EN_DECADE_HYPHEN = "-";
    private static final String INDENT =" ";

    AmountInWordEnConverter() {
        initializationResources();
    }

    @Override
    String getDecade(int position) {
        String decade = super.getDecade(position);
        return decade.replace(INDENT, EN_DECADE_HYPHEN);
    }

    @Override
    public String convert(double amount, String currencyCode) {
        String result = super.convert(amount, currencyCode);
        return result.replace("- ", INDENT);
    }
}
