package ru.citeck.ecos.currency;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import ru.citeck.ecos.model.IdocsModel;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author alexander.nemerov
 *         date 03.11.2016.
 */
public class CurrencyDAO {

    private NodeService nodeService;
    private SearchService searchService;

    private Map<NodeRef, Currency> currencyNodeRef;
    private Map<String, Currency> currencyStorage = null;
    private Map<Integer, Currency> currencyByNumberCodeStorage;

    public Map<String, Currency> readAllCurrencies() {
        SearchParameters parameters = new SearchParameters();
        parameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        parameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        parameters.setQuery("TYPE:\"" + IdocsModel.TYPE_CURRENCY + "\"");
        ResultSet resultSet = searchService.query(parameters);

        Map<String, Currency> result = new HashMap<>();
        currencyNodeRef = new HashMap<>();
        currencyByNumberCodeStorage = new HashMap<>();
        for (ResultSetRow resultSetRow : resultSet) {
            NodeRef node = resultSetRow.getNodeRef();
            if(!nodeService.exists(node)) {
                continue;
            }
            Currency currency = new Currency();
            currency.setNodeRef(node);
            currency.setCode((String) nodeService.getProperty(node,
                    IdocsModel.PROP_CURRENCY_CODE));
            currency.setNumberCode((Integer) nodeService.getProperty(node,
                    IdocsModel.PROP_CURRENCY_NUMBER_CODE));
            BigDecimal rate = (nodeService.getProperty(node, IdocsModel.PROP_CURRENCY_RATE) != null)
                    ? new BigDecimal(String.valueOf(nodeService.getProperty(node, IdocsModel.PROP_CURRENCY_RATE)))
                    : BigDecimal.ONE;
            currency.setRate(rate);
            result.put(currency.getCode(), currency);
            currencyNodeRef.put(node, currency);
            currencyByNumberCodeStorage.put(currency.getNumberCode(), currency);
        }

        currencyStorage = result;
        return result;
    }

    public void saveCurrency(Currency currency) {

    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    public Map<String, Currency> getCurrencyStorage() {
        if(currencyStorage == null) {
            return readAllCurrencies();
        }
        return currencyStorage;
    }

    public Currency getCurrencyByNodeRef(NodeRef nodeRef) {
        if (currencyNodeRef == null) {
            readAllCurrencies();
        }
        return currencyNodeRef.get(nodeRef);
    }

    public Currency getCurrencyByNumberCode(Integer numberCode) {
        if (currencyByNumberCodeStorage == null) {
            readAllCurrencies();
        }
        return currencyByNumberCodeStorage.get(numberCode);
    }
}
