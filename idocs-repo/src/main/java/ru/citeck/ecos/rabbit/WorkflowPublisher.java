package ru.citeck.ecos.rabbit;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.apps.module.type.DataType;
import ru.citeck.ecos.apps.module.type.impl.workflow.WorkflowModule;
import ru.citeck.ecos.apps.queue.ModulePublishMsg;
import ru.citeck.ecos.model.EcosBpmModel;
import ru.citeck.ecos.search.ftsquery.FTSQuery;
import ru.citeck.ecos.utils.TransactionUtils;
import ru.citeck.ecos.workflow.EcosBpmAppModelUtils;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Component
public class WorkflowPublisher implements EcosModulePublisher {

    private static final NodeRef ROOT = new NodeRef("workspace://SpacesStore/ecos-bpm-process-root");
    private static final NodeRef CATEGORY = new NodeRef("workspace://SpacesStore/cat-doc-kind-ecos-bpm-default");

    private NodeService nodeService;
    private SearchService searchService;
    private ContentService contentService;
    private EcosBpmAppModelUtils bpmAppUtils;

    @Autowired
    public WorkflowPublisher(ServiceRegistry serviceRegistry) {
        this.nodeService = serviceRegistry.getNodeService();
        this.searchService = serviceRegistry.getSearchService();
        this.contentService = serviceRegistry.getContentService();
    }

    @Override
    public void publish(ModulePublishMsg publishMsg) {

        String[] engineAndId = publishMsg.getId().split("\\$");
        String processId = engineAndId[1];
        String engineId = engineAndId[0];

        String localName = publishMsg.getId().replaceAll("[^a-zA-Z0-9_\\-]", "_");
        QName assocQName = QName.createQNameWithValidLocalName(NamespaceService.SYSTEM_MODEL_1_0_URI, localName);

        Map<QName, Serializable> props = new HashMap<>();

        props.put(ContentModel.PROP_NAME, localName);
        props.put(EcosBpmModel.PROP_ENGINE, engineId);
        props.put(EcosBpmModel.PROP_CATEGORY, CATEGORY);
        props.put(EcosBpmModel.PROP_PROCESS_ID, processId);
        props.put(ContentModel.PROP_TITLE, publishMsg.getName());

        NodeRef processNode = FTSQuery.create()
                .type(EcosBpmModel.TYPE_PROCESS_MODEL).and()
                .exact(EcosBpmModel.PROP_PROCESS_ID, processId).and()
                .exact(EcosBpmModel.PROP_ENGINE, engineId)
                .transactional()
                .queryOne(searchService)
                .orElse(null);

        if (processNode == null) {

            processNode = nodeService.createNode(
                    ROOT,
                    ContentModel.ASSOC_CONTAINS,
                    assocQName,
                    EcosBpmModel.TYPE_PROCESS_MODEL,
                    props
            ).getChildRef();

        } else {

            nodeService.addProperties(processNode, props);
        }

        QName prop;
        if (DataType.JSON.equals(publishMsg.getDataType())) {
            prop = EcosBpmModel.PROP_JSON_MODEL;
        } else {
            prop = ContentModel.PROP_CONTENT;
        }

        ContentWriter writer = contentService.getWriter(processNode, prop, true);
        writer.putContent(new ByteArrayInputStream(publishMsg.getData()));

        NodeRef finalNode = processNode;
        bpmAppUtils.deployProcess(finalNode);
    }

    @Autowired
    public void setBpmAppUtils(EcosBpmAppModelUtils bpmAppUtils) {
        this.bpmAppUtils = bpmAppUtils;
    }

    @Override
    public String getModuleType() {
        return WorkflowModule.TYPE;
    }
}
