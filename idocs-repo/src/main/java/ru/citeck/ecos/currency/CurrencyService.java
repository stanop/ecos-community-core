package ru.citeck.ecos.currency;

import org.alfresco.service.cmr.repository.NodeRef;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

/**
 * Service for different operations with currencies. That can return currency
 * rates, transfer from one to other currency, update currency rates from other
 * system, and other things.
 *
 * @author alexander.nemerov
 * date 03.11.2016.
 */
public interface CurrencyService {

    BigDecimal getCurrencyRate(Currency currency);

    BigDecimal getCurrencyRateByCode(String code);

    Currency getCurrencyByCode(String code);

    Currency getCurrencyByNumberCode(Integer code);

    BigDecimal transferFromOneCurrencyToOther(Currency from,
                                              Currency to,
                                              BigDecimal money);

    Collection<Currency> getAllCurrencies();

    void updateCurrenciesFromRepo();

    Currency getCurrencyByNodeRef(NodeRef nodeRef);

    BigDecimal getLastCurrencyRate(NodeRef baseCurrency, NodeRef targetCurrency, Date date);

    /**
     * Method to get last currency rate with manual conversion.</br>
     * Manual conversion implies that if no pair of rate record baseCurrency-targetCurrency found,</br>
     * then trying to find reversed pair targetCurrency-baseCurrency and calculate currency rate </br>
     * (with scale = 4, round = {@link BigDecimal#ROUND_HALF_UP}).
     * <p>
     * If no rate found in currency records, then return rate by {@link Currency}
     *
     * @param baseCurrency   base currency
     * @param targetCurrency target currency
     * @param date           currency rate date
     * @return last currency rate
     */
    BigDecimal getLastCurrencyRateWithManualConversion(NodeRef baseCurrency, NodeRef targetCurrency, Date date);
}
