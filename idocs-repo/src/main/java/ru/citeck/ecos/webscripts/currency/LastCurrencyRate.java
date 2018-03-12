package ru.citeck.ecos.webscripts.currency;

import lombok.Setter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.currency.Currency;
import ru.citeck.ecos.currency.CurrencyService;
import ru.citeck.ecos.model.IdocsModel;
import ru.citeck.ecos.search.SearchQuery;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LastCurrencyRate extends DeclarativeWebScript {

    private static final String BASE_CURRENCY_PARAM_NAME = "baseCurrency";
    private static final String TARGET_CURRENCY_PARAM_NAME = "targetCurrency";
    private static final String DATE_PARAM_NAME = "date";
    private static final String DATE_FORMAT = "yyyy-MM-dd";

    private static final String QUERY_TEMPLATE = "TYPE:\"idocs:currencyRateRecord\"" +
            " AND @idocs\\:crrBaseCurrency_added:\"%s\"" +
            " AND @idocs\\:crrTargetCurrency_added:\"%s\"" +
            " AND @idocs\\:crrDate:[MIN TO \"%s\"]";

    private SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);

    @Setter
    private SearchService searchService;
    @Setter
    private CurrencyService currencyService;
    @Setter
    private NodeService nodeService;

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

        String baseCurrency = getParameter(req, BASE_CURRENCY_PARAM_NAME);
        String targetCurrency = getParameter(req, TARGET_CURRENCY_PARAM_NAME);
        String date = getParameter(req, DATE_PARAM_NAME);

        String query = String.format(QUERY_TEMPLATE, baseCurrency, targetCurrency, date);
        List<NodeRef> result = search(query);

        double rate = (result.size() > 0)
                ? getRateFromCurrencyRateRecord(result.get(0))
                : getRateFromCurrency(baseCurrency, targetCurrency);

        return createModel(rate);
    }

    private List<NodeRef> search(String query) {
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        searchParameters.setQuery(query);
        searchParameters.setLanguage(SearchQuery.DEFAULT_LANGUAGE);
        searchParameters.setMaxItems(1);
        searchParameters.setSkipCount(0);
        searchParameters.addSort(IdocsModel.PROP_CRR_DATE.getPrefixString(), false);

        ResultSet resultSet = searchService.query(searchParameters);
        try {
            return resultSet.getNodeRefs();
        }
        finally {
            if (resultSet != null) {
                resultSet.close();
            }
        }
    }

    private String getParameter(WebScriptRequest req, String paramName) {
        String param = req.getParameter(paramName);

        if (param == null) {
            throw new WebScriptException("Parameter " + paramName + " is mandatory.");
        }

        return param;
    }

    private double getRateFromCurrencyRateRecord(NodeRef nodeRef) {
        Serializable value = nodeService.getProperty(nodeRef, IdocsModel.PROP_CRR_VALUE);
        return (double) value;
    }

    private double getRateFromCurrency(String base, String target) {
        Currency baseCurrency = currencyService.getCurrencyByNodeRef(new NodeRef(base));
        Currency targetCurrency = currencyService.getCurrencyByNodeRef(new NodeRef(target));
        BigDecimal result = currencyService.transferFromOneCurrencyToOther(baseCurrency, targetCurrency, new BigDecimal(1));
        return (new BigDecimal(1).divide(result, BigDecimal.ROUND_HALF_UP)).doubleValue();
    }

    private Map<String, Object> createModel(double rate) {
        Map<String, Object> model = new HashMap<>();
        model.put("data", rate);
        return model;
    }
}
