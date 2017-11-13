package ru.citeck.ecos.utils.converter.amount;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import java.util.Locale;

/**
 * @author Roman.Makarskiy on 10.07.2016.
 * @author Added the ability to set specific language by Oleg.Onischuk on 11.11.2017
 */
public class AmountInWordConverterFactory {

    private static Log logger = LogFactory.getLog(AmountInWordConverter.class);

    /**
     * @return language specific (Ru or En) AmountInWordConverter according to current locale
     * @see AmountInWordRuConverter
     * @see AmountInWordEnConverter
     */
    public AmountInWordConverter getConverter() {
        Locale ruLocale = new Locale("ru", "");
        Locale locale = I18NUtil.getLocale();

        if (locale.equals(ruLocale)) {
            return new AmountInWordRuConverter();
        } else if (locale.equals(Locale.ENGLISH) || locale.equals(Locale.US)) {
            return new AmountInWordEnConverter();
        }
        logger.warn("Converter with locale: <" + locale + "> not found.");
        return getDefaultConverter();
    }

    /**
     * @param language - language param "en", "ru", "uk".
     *                 ISO 639 alpha-2 code for supported languages (English, Russian, Ukrainian)
     * @return language specific AmountInWordConverter (En, Ru or Uk) according to language param
     * @see AmountInWordEnConverter
     * @see AmountInWordRuConverter
     * @see AmountInWordUkConverter
     */
    public AmountInWordConverter getConverter(String language) {
        switch (language) {
            case "en":
                return new AmountInWordEnConverter();
            case "ru":
                return new AmountInWordRuConverter();
            case "uk":
                return new AmountInWordUkConverter();
            default:
                logger.warn("Converter with language: <" + language + "> not found.");
                return getDefaultConverter();
        }
    }

    private AmountInWordConverter getDefaultConverter(){
        AmountInWordConverter defaultConverter = new AmountInWordEnConverter();
        logger.warn("Using default converter: " + defaultConverter);
        return defaultConverter;
    }
}
