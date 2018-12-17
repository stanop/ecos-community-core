package ru.citeck.ecos.menu.resolvers;

import org.apache.commons.lang3.StringUtils;
import org.springframework.extensions.surf.util.I18NUtil;
import ru.citeck.ecos.menu.dto.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SiteElementResolver extends AbstractMenuItemsResolver {

    private String id;
    private String pageLinkTemplate;
    private String elementTitleKey;

    @Override
    public List<Element> resolve(Map<String, String> params, Element context) {
        String siteId = getParam(params, context, SITE_ID_KEY);
        List<Element> result = new ArrayList<>();
        result.add(siteElement(siteId, context));
        return result;
    }

    protected Element siteElement(String siteId, Element context) {
        String title = I18NUtil.getMessage(elementTitleKey);
        String parentElemId = StringUtils.defaultString(context.getId());
        String id = String.format("%s_%s", parentElemId, this.getId());
        String pageId = String.format(pageLinkTemplate, siteId);
        Map<String, String> actionParams = new HashMap<>();
        actionParams.put(PAGE_ID_KEY, pageId);
        Element element = new Element();
        element.setId(id);
        element.setLabel(title);
        element.setAction(PAGE_LINK_KEY, actionParams);
        return element;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPageLinkTemplate(String pageLinkTemplate) {
        this.pageLinkTemplate = pageLinkTemplate;
    }

    public void setElementTitleKey(String elementTitleKey) {
        this.elementTitleKey = elementTitleKey;
    }

    @Override
    public String getId() {
        return id;
    }

}
