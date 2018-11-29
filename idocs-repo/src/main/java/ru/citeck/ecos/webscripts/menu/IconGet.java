package ru.citeck.ecos.webscripts.menu;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.*;
import ru.citeck.ecos.model.EcosModel;
import ru.citeck.ecos.search.ftsquery.FTSQuery;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.Map;

public class IconGet extends AbstractWebScript {

    private static final String PARAM_ICON_NAME = "iconName";
    private static final NodeRef ICONS_ROOT = new NodeRef("workspace://SpacesStore/ecos-icons-root");
    private ObjectMapper objectMapper = new ObjectMapper();
    private SearchService searchService;
    private NodeService nodeService;
    private long cacheAgeSeconds = 600;

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {
        String iconName = req.getParameter(PARAM_ICON_NAME);
        resp.setContentType("application/json;charset=UTF-8");
        Cache cache = new Cache();
        cache.setMaxAge(cacheAgeSeconds);
        resp.setCache(cache);
        Icon result;
        try {
            result = FTSQuery.create()
                        .parent(ICONS_ROOT).and()
                        .exact(ContentModel.PROP_NAME, iconName)
                        .transactional().queryOne(searchService)
                        .map(this::getIconData)
                        .orElseThrow(() -> new InvalidParameterException("Error getting icon info for icon name: " + iconName));
        } catch (RuntimeException re) {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Error getting icon info.", re);
        }

        try (OutputStream os = resp.getOutputStream()) {
            objectMapper.writeValue(os, result);
        } catch (RuntimeException re) {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Error writing JSON response.", re);
        }

    }

    private Icon getIconData(NodeRef nodeRef) {
        Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
        String type = StringUtils.defaultString((String) props.get(EcosModel.PROP_ICON_TYPE));
        String faIconName = StringUtils.defaultString((String) props.get(EcosModel.PROP_FA_ICON_NAME));
        Icon result = new Icon();
        if (StringUtils.isNotEmpty(type)) {
            result.setType(type);
            if (StringUtils.equals(type, "fa")) {
                result.setValue(faIconName);
            } else {
                result.setValue(nodeRef.getId());
            }
        } else {
            result.setType("img");
            result.setValue(nodeRef.getId());
        }
        return result;
    }

    @Autowired
    public void setSearchService(SearchService searchService) {
        this.searchService = searchService;
    }

    @Autowired
    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    private class Icon {
        String type;
        String value;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }
}
