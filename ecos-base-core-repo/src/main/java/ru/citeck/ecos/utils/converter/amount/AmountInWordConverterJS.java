package ru.citeck.ecos.utils.converter.amount;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * JavaScript interface of {@link AmountInWordConverter} for testing purpose.
 */
public class AmountInWordConverterJS extends BaseScopableProcessorExtension {
    private final AmountInWordConverter aiwConverterEn = new AmountInWordConverterFactory().getConverter("en");
    private final AmountInWordConverter aiwConverterRu = new AmountInWordConverterFactory().getConverter("ru");
    private final AmountInWordConverter aiwConverterUk = new AmountInWordConverterFactory().getConverter("uk");


    /**
     * Convert an amount to words using language from current locale
     * @param amount   - amount to convert
     * @param currencyCode - code of currency in ISO 4217 alpha 3 standard.
     * @return amount in words
     */
    public String convert(double amount, String currencyCode){
        String lang = I18NUtil.getLocale().getLanguage();
        return convert(amount, currencyCode, lang);
    }

    /**
     * @param amount   - amount to convert
     * @param currencyCode - code of currency in ISO 4217 alpha 3 standard.
     * @param language - language param "en", "ru", "uk".
     *                 ISO 639 alpha-2 code for supported languages (English, Russian, Ukrainian)
     * @return amount in words
     */
    public String convert(double amount, String currencyCode, String language) {
        switch (language) {
            case "en":
                return convertEn(amount, currencyCode);
            case "ru":
                return convertRu(amount, currencyCode);
            case "uk":
                return convertUk(amount, currencyCode);
            default:
                return convertEn(amount, currencyCode);
        }
    }

    public String convertEn(double amount, String currencyCode) {
        return aiwConverterEn.convert(amount, currencyCode);
    }

    public String convertRu(double amount, String currencyCode) {
        return aiwConverterRu.convert(amount, currencyCode);
    }

    public String convertUk(double amount, String currencyCode) {
        return aiwConverterUk.convert(amount, currencyCode);
    }
}
