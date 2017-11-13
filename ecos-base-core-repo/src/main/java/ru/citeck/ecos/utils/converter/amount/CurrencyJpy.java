package ru.citeck.ecos.utils.converter.amount;

import java.util.Locale;

/**
 * @author Roman.Makarskiy on 10/10/2016.
 */
class CurrencyJpy extends Currency {

//    public CurrencyJpy (){
//        super();
//    }
    CurrencyJpy (Locale locale){
        super(locale);
    }


    @Override
    void initializationResources() {
        setFractional1(getMessage("amount-in-word-converter.currency.jpy.fractional-1"));
        setFractional2(getMessage("amount-in-word-converter.currency.jpy.fractional-2"));
        setFractional3(getMessage("amount-in-word-converter.currency.jpy.fractional-3"));

        setIntact1(getMessage("amount-in-word-converter.currency.jpy.intact-1"));
        setIntact2(getMessage("amount-in-word-converter.currency.jpy.intact-2"));
        setIntact3(getMessage("amount-in-word-converter.currency.jpy.intact-3"));
    }
}
