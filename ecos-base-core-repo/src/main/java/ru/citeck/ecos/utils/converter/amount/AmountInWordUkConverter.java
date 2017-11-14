package ru.citeck.ecos.utils.converter.amount;

import java.util.Locale;

/**
 * Ukrainian realization of converter
 *
 * @author Oleg.Onischuk on 11.11.2017.
 */
class AmountInWordUkConverter extends AmountInWordConverterFixedFemininely {

    AmountInWordUkConverter() {
        locale = new Locale("uk", "");

        one_hryvnia = "один гривня";
        feminine_one_hryvnia = "одна гривня";
        two_hryvnia = "два гривні";
        feminine_two_hryvnia = "дві гривні";

        one_yen = "один японська єна";
        feminine_one_yen = "одна японська єна";
        two_yen = "два японські єни";
        feminine_two_yen = "дві японські єни";
    }
}
