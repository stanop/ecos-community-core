package ru.citeck.ecos.currency;

import org.alfresco.service.cmr.repository.NodeRef;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Collection;
import java.util.Map;

/**
 * @author alexander.nemerov
 *         date 03.11.2016.
 */
public class CurrencyServiceImpl implements CurrencyService {


    private CurrencyDAO currencyDAO;

    @Override
    public BigDecimal getCurrencyRate(Currency currency) {
        return currency.getRate();
    }

    @Override
    public BigDecimal getCurrencyRateByCode(String code) {
        return getCurrencyByCode(code).getRate();
    }

    @Override
    public Currency getCurrencyByCode(String code) {
        return currencyDAO.getCurrencyStorage().get(code);
    }

    @Override
    public BigDecimal transferFromOneCurrencyToOther(Currency from,
                                                     Currency to,
                                                     BigDecimal money) {
        if (from == null || to == null || to.equals(from)) {
            return money;
        }
        BigDecimal mult = money.multiply(from.getRate());
        return mult.divide(to.getRate(), MathContext.DECIMAL64);
    }

    @Override
    public Collection<Currency> getAllCurrencies() {
        return currencyDAO.getCurrencyStorage().values();
    }

    @Override
    public void updateCurrenciesFromRepo() {
        currencyDAO.readAllCurrencies();
    }

    @Override
    public Currency getCurrencyByNodeRef(NodeRef nodeRef) {
        return currencyDAO.getCurrencyByNodeRef(nodeRef);
    }

    public void setCurrencyDAO(CurrencyDAO currencyDAO) {
        this.currencyDAO = currencyDAO;
    }
}
