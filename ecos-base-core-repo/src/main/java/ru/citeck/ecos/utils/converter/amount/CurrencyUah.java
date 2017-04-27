package ru.citeck.ecos.utils.converter.amount;

import org.springframework.extensions.surf.util.I18NUtil;

/**
 * @author Roman.Makarskiy on 10/10/2016.
 */
class CurrencyUah extends Currency {
    @Override
    void initializationResources() {
        setFractional1(I18NUtil.getMessage("amount-in-word-converter.currency.uah.fractional-1"));
        setFractional2(I18NUtil.getMessage("amount-in-word-converter.currency.uah.fractional-2"));
        setFractional3(I18NUtil.getMessage("amount-in-word-converter.currency.uah.fractional-3"));

        setIntact1(I18NUtil.getMessage("amount-in-word-converter.currency.uah.intact-1"));
        setIntact2(I18NUtil.getMessage("amount-in-word-converter.currency.uah.intact-2"));
        setIntact3(I18NUtil.getMessage("amount-in-word-converter.currency.uah.intact-3"));
    }
}
