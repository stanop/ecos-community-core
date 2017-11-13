package ru.citeck.ecos.utils.converter.amount;

import java.util.Locale;

/**
 * @author Roman.Makarskiy on 10.07.2016.
 */
class CurrencyUsd extends Currency{

    CurrencyUsd (Locale locale){
        super(locale);
    }

    @Override
    void initializationResources() {
        setFractional1(getMessage("amount-in-word-converter.currency.usd.fractional-1"));
        setFractional2(getMessage("amount-in-word-converter.currency.usd.fractional-2"));
        setFractional3(getMessage("amount-in-word-converter.currency.usd.fractional-3"));

        setIntact1(getMessage("amount-in-word-converter.currency.usd.intact-1"));
        setIntact2(getMessage("amount-in-word-converter.currency.usd.intact-2"));
        setIntact3(getMessage("amount-in-word-converter.currency.usd.intact-3"));
    }
}
