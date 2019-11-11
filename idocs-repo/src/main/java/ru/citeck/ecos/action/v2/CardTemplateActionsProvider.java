package ru.citeck.ecos.action.v2;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import ecos.com.google.common.cache.CacheBuilder;
import ecos.com.google.common.cache.CacheLoader;
import ecos.com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.apps.app.module.type.type.action.ActionDto;
import ru.citeck.ecos.model.CDLModel;
import ru.citeck.ecos.model.DmsModel;
import ru.citeck.ecos.search.ftsquery.FTSQuery;
import ru.citeck.ecos.template.CardTemplateService;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CardTemplateActionsProvider implements NodeActionsV2Provider {

    private static final String URL = "/share/proxy/alfresco/citeck/print/metadata-printpdf" +
                                                                            "?nodeRef=%s" +
                                                                            "&templateType=%s" +
                                                                            "&print=true" +
                                                                            "&format=%s";

    private NodeService nodeService;
    private SearchService searchService;
    private CardTemplateService templateService;

    private LoadingCache<String, MLText> titleByTemplateType;

    private CardTemplateActionsProvider() {
        titleByTemplateType = CacheBuilder.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .build(CacheLoader.from(this::getTemplateTypeTitleImpl));
    }

    private MLText getTemplateTypeTitleImpl(String type) {

        NodeRef typeRef = FTSQuery.create()
                                  .type(CDLModel.TYPE_CARD_TEMPLATE_TYPE).and()
                                  .exact(ContentModel.PROP_NAME, type)
                                  .transactional()
                                  .queryOne(searchService)
                                  .orElse(null);
        if (typeRef == null) {
            return new MLText("");
        }

        MLPropertyInterceptor.setMLAware(true);
        try {

            MLText title = (MLText) nodeService.getProperty(typeRef, ContentModel.PROP_TITLE);
            if (title == null) {
                return new MLText("");
            } else {
                return title;
            }

        } finally {
            MLPropertyInterceptor.setMLAware(false);
        }
    }

    @Override
    public List<ActionDto> getActions(NodeRef nodeRef) {

        if (!nodeService.exists(nodeRef)) {
            return Collections.emptyList();
        }

        QName type = nodeService.getType(nodeRef);

        List<NodeRef> templatesForDocType = templateService.getTemplatesForDocType(type);

        List<ActionDto> result = new ArrayList<>();

        for (NodeRef template : templatesForDocType) {

            Map<QName, Serializable> props = nodeService.getProperties(template);
            String templateType = (String) props.get(DmsModel.PROP_TEMPLATE_TYPE);

            if (templateType == null || templateType.isEmpty())  {
                continue;
            }

            result.add(createDownloadAction(nodeRef, templateType, "html"));
            result.add(createDownloadAction(nodeRef, templateType, "pdf"));
        }

        return result;
    }

    private ActionDto createDownloadAction(NodeRef nodeRef, String templateType, String format) {

        ActionDto action = new ActionDto();

        action.setName(templateType);

        try {

            MLText mlTitle = titleByTemplateType.get(templateType);
            String title = mlTitle.getClosestValue(I18NUtil.getLocale());
            title += " (" + format + ")";
            action.setName(title);

        } catch (ExecutionException e) {
            log.warn("Title can't be received: ", e);
            action.setName(templateType);
        }

        action.setKey("card-template");
        action.setType("download");

        String url = String.format(URL, nodeRef.toString(), templateType, format);

        ObjectNode config = JsonNodeFactory.instance.objectNode();
        config.set("url", TextNode.valueOf(url));
        config.set("filename", TextNode.valueOf("template." + format));

        action.setConfig(config);

        return action;
    }

    @Override
    public String getScope() {
        return "card-template";
    }

    @Autowired
    public void setTemplateService(CardTemplateService templateService) {
        this.templateService = templateService;
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.nodeService = serviceRegistry.getNodeService();
        this.searchService = serviceRegistry.getSearchService();
    }
}
