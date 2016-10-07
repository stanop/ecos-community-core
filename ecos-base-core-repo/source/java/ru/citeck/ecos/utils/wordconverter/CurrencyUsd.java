package ru.citeck.ecos.utils.wordconverter;

import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Created by Roman on 10/7/2016.
 */
class CurrencyUsd extends Currency{
    CurrencyUsd() {
        setFractional1(I18NUtil.getMessage("amount-in-word-converter.currency.usd.fractional-1"));
        setFractional2(I18NUtil.getMessage("amount-in-word-converter.currency.usd.fractional-2"));
        setFractional3(I18NUtil.getMessage("amount-in-word-converter.currency.usd.fractional-3"));

        setIntact1(I18NUtil.getMessage("amount-in-word-converter.currency.usd.intact-1"));
        setIntact2(I18NUtil.getMessage("amount-in-word-converter.currency.usd.intact-2"));
        setIntact3(I18NUtil.getMessage("amount-in-word-converter.currency.usd.intact-3"));
    }
}
