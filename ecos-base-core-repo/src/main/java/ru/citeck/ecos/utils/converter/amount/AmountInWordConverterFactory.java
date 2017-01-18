package ru.citeck.ecos.utils.converter.amount;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import java.util.Locale;

/**
 * @author Roman.Makarskiy on 10.07.2016.
 */
public class AmountInWordConverterFactory {

    public AmountInWordConverter getConverter() {
        Log logger = LogFactory.getLog(AmountInWordConverter.class);
        AmountInWordConverter defaultConverter = new AmountInWordEnConverter();
        Locale ruLocale = new Locale("ru", "");
        Locale locale = I18NUtil.getLocale();

        if (locale.equals(ruLocale)) {
            return new AmountInWordRuConverter();
        } else if (locale.equals(Locale.ENGLISH) || locale.equals(Locale.US)) {
            return new AmountInWordEnConverter();
        }

        logger.warn("Converter with locale: <" + locale + "> not found, using default converter: " + defaultConverter);
        return defaultConverter;
    }
}
