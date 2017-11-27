package ru.citeck.ecos.utils.converter.amount;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Locale;

/**
 * @author Roman.Makarskiy on 10.07.2016.
 */
class CurrencyFactory {

    Currency getCurrency(String currencyCode, Locale locale) {
        Log logger = LogFactory.getLog(CurrencyFactory.class);
        Currency currency;

        switch (currencyCode) {
            case "USD": {
                currency = new CurrencyUsd(locale);
                break;
            }
            case "RUB": {
                currency = new CurrencyRub(locale);
                break;
            }
            case "RUR": {
                currency = new CurrencyRub(locale);
                break;
            }
            case "EUR": {
                currency = new CurrencyEur(locale);
                break;
            }
            case "BYR": {
                currency = new CurrencyByr(locale);
                break;
            }
            case "GBP": {
                currency = new CurrencyGbp(locale);
                break;
            }
            case "JPY": {
                currency = new CurrencyJpy(locale);
                break;
            }
            case "UAH": {
                currency = new CurrencyUah(locale);
                break;
            }
            default: {
                logger.warn("Currency with code: <" + currencyCode + "> not found, using default currency - USD");
                currency = new CurrencyUsd(locale);
            }
        }
        return currency;
    }
}
