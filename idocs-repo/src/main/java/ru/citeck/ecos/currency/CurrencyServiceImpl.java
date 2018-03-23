package ru.citeck.ecos.currency;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import ru.citeck.ecos.model.IdocsModel;
import ru.citeck.ecos.search.SearchQuery;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * @author alexander.nemerov
 *         date 03.11.2016.
 */
public class CurrencyServiceImpl implements CurrencyService {

    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private static final String CURRENCY_RATE_QUERY_TEMPLATE = "TYPE:\"idocs:currencyRateRecord\"" +
            " AND @idocs\\:crrBaseCurrency_added:\"%s\"" +
            " AND @idocs\\:crrTargetCurrency_added:\"%s\"" +
            " AND @idocs\\:crrDate:[MIN TO \"%s\"]";

    private static SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

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
    public Currency getCurrencyByNumberCode(Integer code) {
        return currencyDAO.getCurrencyByNumberCode(code);
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

    @Override
    public BigDecimal getLastCurrencyRate(NodeRef baseCurrency, NodeRef targetCurrency, Date date) {
        String dateStr = sdf.format(date);
        String query = String.format(CURRENCY_RATE_QUERY_TEMPLATE, baseCurrency, targetCurrency, dateStr);
        Optional<NodeRef> result = search(query);

        return result
                .map(nodeRef -> getRateFromCurrencyRateRecord(nodeRef, baseCurrency, targetCurrency))
                .orElseGet(() -> getRateFromCurrency(baseCurrency, targetCurrency));
    }

    public void setCurrencyDAO(CurrencyDAO currencyDAO) {
        this.currencyDAO = currencyDAO;
    }

    private Optional<NodeRef> search(String query) {
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        searchParameters.setQuery(query);
        searchParameters.setLanguage(SearchQuery.DEFAULT_LANGUAGE);
        searchParameters.setMaxItems(1);
        searchParameters.setSkipCount(0);
        searchParameters.addSort(IdocsModel.PROP_CRR_DATE.toString(), false);

        ResultSet resultSet = currencyDAO.getSearchService().query(searchParameters);
        try {
            List<NodeRef> result = resultSet.getNodeRefs();
            return result.stream().findFirst();
        } finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
    }

    private BigDecimal getRateFromCurrencyRateRecord(NodeRef nodeRef, NodeRef baseCurrency, NodeRef targetCurrency) {
        Serializable value = currencyDAO.getNodeService().getProperty(nodeRef, IdocsModel.PROP_CRR_VALUE);
        return (value != null)
                ? BigDecimal.valueOf((double) value)
                : getRateFromCurrency(baseCurrency, targetCurrency);
    }

    private BigDecimal getRateFromCurrency(NodeRef base, NodeRef target) {
        Currency baseCurrency = getCurrencyByNodeRef(base);
        Currency targetCurrency = getCurrencyByNodeRef(target);
        BigDecimal result = transferFromOneCurrencyToOther(baseCurrency, targetCurrency, new BigDecimal(1));
        return new BigDecimal(1).divide(result, BigDecimal.ROUND_HALF_UP);
    }

}
