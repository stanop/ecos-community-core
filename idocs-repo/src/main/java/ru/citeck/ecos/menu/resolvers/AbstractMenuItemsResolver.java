package ru.citeck.ecos.menu.resolvers;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import ru.citeck.ecos.menu.dto.Element;
import ru.citeck.ecos.menu.dto.MenuFactory;

import javax.annotation.PostConstruct;
import java.util.Map;

public abstract class AbstractMenuItemsResolver implements MenuItemsResolver {

    protected static final String JOURNAL_ID_KEY = "journalId";
    protected static final String SITE_ID_KEY = "siteId";

    private MenuFactory menuFactory;

    @PostConstruct
    public void registerResolver() {
        menuFactory.addResolver(this);
    }

    @Autowired
    @Qualifier("ecos.menu.menuFactory")
    public void setMenuFactory(MenuFactory menuFactory) {
        this.menuFactory = menuFactory;
    }

    /**
     * First trying to get parameter by @key from @context, then from resolver @params
     * @param params resolver params
     * @param context element context
     * @param key parameter key
     * @return parameter or empty String
     */
    static String getParam(Map<String, String> params, Element context, String key) {
        String result = null;
        if (context == null) {
            return "";
        }
        Map<String, String> contextParams = context.getParams();
        if (MapUtils.isNotEmpty(contextParams)) {
            result = contextParams.get(key);
        }
        if (StringUtils.isEmpty(result) && MapUtils.isNotEmpty(params)) {
            result = params.get(key);
        }
        if (result == null) {
            return "";
        }
        return result;
    }

}
