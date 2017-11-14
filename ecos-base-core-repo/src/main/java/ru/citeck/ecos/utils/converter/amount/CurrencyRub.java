package ru.citeck.ecos.utils.converter.amount;

import java.util.Locale;

/**
 * @author Roman.Makarskiy on 10.07.2016.
 */
class CurrencyRub extends Currency {

    public CurrencyRub(Locale locale) {
        super(locale);
    }

    @Override
    void initializationResources() {
        setFractional1(getMessage("amount-in-word-converter.currency.rub.fractional-1"));
        setFractional2(getMessage("amount-in-word-converter.currency.rub.fractional-2"));
        setFractional3(getMessage("amount-in-word-converter.currency.rub.fractional-3"));

        setIntact1(getMessage("amount-in-word-converter.currency.rub.intact-1"));
        setIntact2(getMessage("amount-in-word-converter.currency.rub.intact-2"));
        setIntact3(getMessage("amount-in-word-converter.currency.rub.intact-3"));
    }
}
