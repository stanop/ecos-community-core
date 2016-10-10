package ru.citeck.ecos.utils.converter.amount;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Roman.Makarskiy on 10.07.2016.
 */
class CurrencyFactory {

    Currency getCurrency(String currencyCode) {
        Log logger = LogFactory.getLog(CurrencyFactory.class);
        Currency currency;

        switch (currencyCode) {
            case "USD": {
                currency = new CurrencyUsd();
                break;
            }
            case "RUB": {
                currency = new CurrencyRub();
                break;
            }
            case "RUR": {
                currency = new CurrencyRub();
                break;
            }
            case "EUR": {
                currency = new CurrencyEur();
                break;
            }
            case "BYR": {
                currency = new CurrencyByr();
                break;
            }
            case "GBP": {
                currency = new CurrencyGbp();
                break;
            }
            case "JPY": {
                currency = new CurrencyJpy();
                break;
            }
            case "UAH": {
                currency = new CurrencyUah();
                break;
            }
            default: {
                logger.warn("Currency with code: <" + currencyCode + "> not found, using default currency - USD");
                currency = new CurrencyUsd();
            }
        }
        return currency;
    }
}
