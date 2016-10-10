package ru.citeck.ecos.utils.converter.amount;

import org.springframework.extensions.surf.util.I18NUtil;

/**
 * @author Roman.Makarskiy on 10.07.2016.
 */
class CurrencyEur extends Currency {

    @Override
    void initializationResources() {
        setFractional1(I18NUtil.getMessage("amount-in-word-converter.currency.eur.fractional-1"));
        setFractional2(I18NUtil.getMessage("amount-in-word-converter.currency.eur.fractional-2"));
        setFractional3(I18NUtil.getMessage("amount-in-word-converter.currency.eur.fractional-3"));

        setIntact1(I18NUtil.getMessage("amount-in-word-converter.currency.eur.intact-1"));
        setIntact2(I18NUtil.getMessage("amount-in-word-converter.currency.eur.intact-2"));
        setIntact3(I18NUtil.getMessage("amount-in-word-converter.currency.eur.intact-3"));
    }
}
