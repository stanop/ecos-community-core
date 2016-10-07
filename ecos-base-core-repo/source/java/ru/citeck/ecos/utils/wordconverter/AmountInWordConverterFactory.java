package ru.citeck.ecos.utils.wordconverter;

import org.springframework.extensions.surf.util.I18NUtil;

import java.util.Locale;

/**
 * @author Roman.Makarskiy on 10.07.2016.
 */
public class AmountInWordConverterFactory {

    public AmountInWordConverter getConverter() {
        AmountInWordConverter converter = new AmountInWordEnConverter();
        Locale ruLocale = new Locale("ru", "");
        Locale locale = I18NUtil.getLocale();

        if (locale.equals(ruLocale)) {
            converter = new AmountInWordRuConverter();
        }

        return converter;
    }
}
