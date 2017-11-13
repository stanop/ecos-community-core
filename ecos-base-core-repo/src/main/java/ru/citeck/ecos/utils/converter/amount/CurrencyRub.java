package ru.citeck.ecos.utils.converter.amount;

import org.springframework.extensions.surf.util.I18NUtil;

import java.util.Locale;

/**
 * @author Roman.Makarskiy on 10.07.2016.
 */
class CurrencyRub extends Currency {

    public CurrencyRub (Locale locale){
        super(locale);
    }

    @Override
    void initializationResources() {
        setFractional1(I18NUtil.getMessage("amount-in-word-converter.currency.rub.fractional-1", locale));
        setFractional2(I18NUtil.getMessage("amount-in-word-converter.currency.rub.fractional-2", locale));
        setFractional3(I18NUtil.getMessage("amount-in-word-converter.currency.rub.fractional-3", locale));

        setIntact1(I18NUtil.getMessage("amount-in-word-converter.currency.rub.intact-1", locale));
        setIntact2(I18NUtil.getMessage("amount-in-word-converter.currency.rub.intact-2", locale));
        setIntact3(I18NUtil.getMessage("amount-in-word-converter.currency.rub.intact-3", locale));
    }
}
