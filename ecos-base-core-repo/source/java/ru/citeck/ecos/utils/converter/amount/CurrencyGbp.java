package ru.citeck.ecos.utils.converter.amount;

import org.springframework.extensions.surf.util.I18NUtil;

/**
 * @author Roman.Makarskiy on 10/10/2016.
 */
class CurrencyGbp extends Currency {

    @Override
    void initializationResources() {
        setFractional1(I18NUtil.getMessage("amount-in-word-converter.currency.gbp.fractional-1"));
        setFractional2(I18NUtil.getMessage("amount-in-word-converter.currency.gbp.fractional-2"));
        setFractional3(I18NUtil.getMessage("amount-in-word-converter.currency.gbp.fractional-3"));

        setIntact1(I18NUtil.getMessage("amount-in-word-converter.currency.gbp.intact-1"));
        setIntact2(I18NUtil.getMessage("amount-in-word-converter.currency.gbp.intact-2"));
        setIntact3(I18NUtil.getMessage("amount-in-word-converter.currency.gbp.intact-3"));
    }
}
