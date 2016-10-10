package ru.citeck.ecos.utils.converter.amount;

import org.springframework.extensions.surf.util.I18NUtil;

/**
 * @author Roman.Makarskiy on 10/10/2016.
 */
class CurrencyJpy extends Currency {

    @Override
    void initializationResources() {
        setFractional1(I18NUtil.getMessage("amount-in-word-converter.currency.jpy.fractional-1"));
        setFractional2(I18NUtil.getMessage("amount-in-word-converter.currency.jpy.fractional-2"));
        setFractional3(I18NUtil.getMessage("amount-in-word-converter.currency.jpy.fractional-3"));

        setIntact1(I18NUtil.getMessage("amount-in-word-converter.currency.jpy.intact-1"));
        setIntact2(I18NUtil.getMessage("amount-in-word-converter.currency.jpy.intact-2"));
        setIntact3(I18NUtil.getMessage("amount-in-word-converter.currency.jpy.intact-3"));
    }
}
