package ru.citeck.ecos.utils.wordconverter;

/**
 * Created by Roman on 10/7/2016.
 */
class CurrencyFactory {

    Currency createCurrency(String currencyCode) {
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
            case "EUR": {
                currency = new CurrencyEur();
                break;
            }
            default: currency = new CurrencyRub();
        }
        return currency;
    }
}
