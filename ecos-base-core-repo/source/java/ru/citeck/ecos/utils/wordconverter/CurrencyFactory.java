package ru.citeck.ecos.utils.wordconverter;

/**
 * @author Roman.Makarskiy on 10.07.2016.
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
            default: currency = new CurrencyUsd();
        }
        return currency;
    }
}
