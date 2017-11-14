package ru.citeck.ecos.utils.converter.amount;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * JavaScript implementation of {@link AmountInWordConverter}.
 */
public class AmountInWordConverterJS extends BaseScopableProcessorExtension {
    //private AmountInWordConverter aiwConverter = new AmountInWordConverterFactory().getConverter(I18NUtil.getLocale().getLanguage());

    /**
     * Convert an amount to words.
     * Using language from current locale by default
     * or En if current locale is not supported by converter.
     * @param amount   - amount to convert
     * @param currencyCode - code of currency in ISO 4217 alpha 3 standard.
     *                     using USD if currency is not supported by converter.
     *                     Supported currency codes: USD, RUB, RUR, EUR, BYR, GBP, GPY, UAH
     * @return amount in words
     */
    public String convert(double amount, String currencyCode){
        AmountInWordConverter aiwConverter = new AmountInWordConverterFactory().getConverter(I18NUtil.getLocale().getLanguage());
        return aiwConverter.convert(amount, currencyCode);
    }

    /**
     * Convert an amount to words.
     * @param amount   - amount to convert
     * @param currencyCode - code of currency in ISO 4217 alpha 3 standard.
     * @param language - language param "en", "ru", "uk".
     *                 ISO 639 alpha-2 code for supported languages
     *                 (English, Russian, Ukrainian)
     * @return amount in words
     */
    public String convert(double amount, String currencyCode, String language){
        AmountInWordConverter converter = new AmountInWordConverterFactory().getConverter(language);
        return converter.convert(amount, currencyCode);
    }


}
