package ru.citeck.ecos.currency;

import org.alfresco.service.cmr.repository.NodeRef;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * Service for different operations with currencies. That can return currency
 * rates, transfer from one to other currency, update currency rates from other
 * system, and other things.
 *
 * @author alexander.nemerov
 *         date 03.11.2016.
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
}
