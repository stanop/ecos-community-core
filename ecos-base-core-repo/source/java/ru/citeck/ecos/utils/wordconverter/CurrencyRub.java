package ru.citeck.ecos.utils.wordconverter;

import org.springframework.extensions.surf.util.I18NUtil;

/**
 * @author Roman.Makarskiy on 10.07.2016.
 */
class CurrencyRub extends Currency {
    CurrencyRub() {
        setFractional1(I18NUtil.getMessage("amount-in-word-converter.currency.rub.fractional-1"));
        setFractional2(I18NUtil.getMessage("amount-in-word-converter.currency.rub.fractional-2"));
        setFractional3(I18NUtil.getMessage("amount-in-word-converter.currency.rub.fractional-3"));

        setIntact1(I18NUtil.getMessage("amount-in-word-converter.currency.rub.intact-1"));
        setIntact2(I18NUtil.getMessage("amount-in-word-converter.currency.rub.intact-2"));
        setIntact3(I18NUtil.getMessage("amount-in-word-converter.currency.rub.intact-3"));
    }
}
