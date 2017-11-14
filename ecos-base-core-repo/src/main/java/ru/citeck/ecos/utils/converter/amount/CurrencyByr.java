package ru.citeck.ecos.utils.converter.amount;

import java.util.Locale;

/**
 * @author Roman.Makarskiy on 10/10/2016.
 */
class CurrencyByr extends Currency {

    CurrencyByr(Locale locale) {
        super(locale);
    }

    @Override
    void initializationResources() {
        setFractional1(getMessage("amount-in-word-converter.currency.byr.fractional-1"));
        setFractional2(getMessage("amount-in-word-converter.currency.byr.fractional-2"));
        setFractional3(getMessage("amount-in-word-converter.currency.byr.fractional-3"));

        setIntact1(getMessage("amount-in-word-converter.currency.byr.intact-1"));
        setIntact2(getMessage("amount-in-word-converter.currency.byr.intact-2"));
        setIntact3(getMessage("amount-in-word-converter.currency.byr.intact-3"));
    }
}
