package ru.citeck.ecos.utils.converter.amount;

import java.util.Locale;

/**
 * Russian realization of converter
 *
 * @author Roman.Makarskiy on 10.07.2016.
 */
class AmountInWordRuConverter extends AmountInWordConverterFixedFemininely {

    AmountInWordRuConverter() {
        locale = new Locale("ru", "");

        one_hryvnia = "один гривна";
        feminine_one_hryvnia = "одна гривна";
        two_hryvnia = "два гривны";
        feminine_two_hryvnia = "две гривны";

        one_yen = "один японская иена";
        feminine_one_yen = "одна японская иена";
        two_yen = "два японских иены";
        feminine_two_yen = "две японских иены";
    }
}
