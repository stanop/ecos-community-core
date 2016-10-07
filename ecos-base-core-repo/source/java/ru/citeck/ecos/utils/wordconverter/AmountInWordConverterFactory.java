package ru.citeck.ecos.utils.wordconverter;

import org.springframework.extensions.surf.util.I18NUtil;

import java.util.Locale;

/**
 * Created by Roman on 10/7/2016.
 */
public class AmountInWordConverterFactory {

    public AmountInWordConverter getConverter() {
        AmountInWordConverter converter = null;
        Locale ruLocale = new Locale("ru", "");
        Locale locale = I18NUtil.getLocale();

        if (locale.equals(ruLocale)) {
            converter = new AmountInWordRuConverter();
        } else if (locale.equals(Locale.US) || locale.equals(Locale.ENGLISH)) {
            converter = new AmountInWordEnConverter();
        }
        return converter;
    }
}
