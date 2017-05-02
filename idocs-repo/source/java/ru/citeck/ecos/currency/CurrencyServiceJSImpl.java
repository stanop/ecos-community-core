package ru.citeck.ecos.currency;

import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.utils.AlfrescoScopableProcessorExtension;

import java.math.BigDecimal;
import java.util.Collection;

/**
 * @author alexander.nemerov
 *         date 04.11.2016.
 */
public class CurrencyServiceJSImpl extends AlfrescoScopableProcessorExtension
        implements CurrencyService {

    private CurrencyService currencyService;

    @Override
    public BigDecimal getCurrencyRate(Currency currency) {
        return currencyService.getCurrencyRate(currency);
    }

    @Override
    public BigDecimal getCurrencyRateByCode(String code) {
        return currencyService.getCurrencyRateByCode(code);
    }

    @Override
    public Currency getCurrencyByCode(String code) {
        return currencyService.getCurrencyByCode(code);
    }

    @Override
    public Currency getCurrencyByNumberCode(Integer code) {
        return currencyService.getCurrencyByNumberCode(code);
    }

    @Override
    public BigDecimal transferFromOneCurrencyToOther(Currency from, Currency to, BigDecimal money) {
        return currencyService.transferFromOneCurrencyToOther(from, to, money);
    }

    public double transfer(String fromCurrencyRef, String toCurrencyRef, double amount) {
        NodeRef fromCurrencyNodeRef = new NodeRef(fromCurrencyRef);
        NodeRef toCurrencyNodeRef = new NodeRef(toCurrencyRef);
        BigDecimal bigDecimal = new BigDecimal(amount);
        Currency fromCurrency = currencyService.getCurrencyByNodeRef(fromCurrencyNodeRef);
        Currency toCurrency = currencyService.getCurrencyByNodeRef(toCurrencyNodeRef);
        return currencyService.transferFromOneCurrencyToOther(fromCurrency, toCurrency, bigDecimal).doubleValue();
    }

    @Override
    public Collection<Currency> getAllCurrencies() {
        return currencyService.getAllCurrencies();
    }

    @Override
    public void updateCurrenciesFromRepo() {
        currencyService.updateCurrenciesFromRepo();
    }

    @Override
    public Currency getCurrencyByNodeRef(NodeRef nodeRef) {
        return currencyService.getCurrencyByNodeRef(nodeRef);
    }

    public void setCurrencyService(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }
}
