package ru.citeck.ecos.history.impl;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import ru.citeck.ecos.constants.DocumentHistoryConstants;
import ru.citeck.ecos.model.HistoryModel;

import java.util.*;

/**
 * History get service
 */
public class HistoryGetServiceImpl implements HistoryGetService {

    private NodeService nodeService;

    /**
     * Get history events by document refs
     *
     * @param documentNodeRef Document node reference
     * @return List of transformed events (for web script)
     */
    @SuppressWarnings("unchecked")
    public List<Map> getHistoryEventsByDocumentRef(NodeRef documentNodeRef) {
        /* Loaf associations */
        List<AssociationRef> associations = nodeService.getSourceAssocs(documentNodeRef, HistoryModel.ASSOC_DOCUMENT);
        List<Map> result = new ArrayList<>(associations.size());
        /* Create entries */
        for (AssociationRef associationRef : associations) {
            Map<String, Object> entryMap = new HashMap<>();
            NodeRef eventRef = associationRef.getSourceRef();
            entryMap.put(DocumentHistoryConstants.NODE_REF.getValue(), eventRef.getId());
            entryMap.put(DocumentHistoryConstants.EVENT_TYPE.getValue(),
                    nodeService.getProperty(eventRef, HistoryModel.PROP_NAME));
            entryMap.put(DocumentHistoryConstants.DOCUMENT_VERSION.getValue(),
                    nodeService.getProperty(eventRef, HistoryModel.PROP_DOCUMENT_VERSION));
            entryMap.put(DocumentHistoryConstants.COMMENTS.getValue(),
                    nodeService.getProperty(eventRef, HistoryModel.PROP_TASK_COMMENT));
            entryMap.put(DocumentHistoryConstants.DOCUMENT_DATE.getValue(),
                    ((Date) nodeService.getProperty(eventRef, HistoryModel.PROP_DATE)).getTime());
            entryMap.put(DocumentHistoryConstants.EVENT_INITIATOR.getValue(),
                    nodeService.getProperty(eventRef, HistoryModel.MODIFIER_PROPERTY));
            entryMap.put(DocumentHistoryConstants.TASK_ROLE.getValue(),
                    nodeService.getProperty(eventRef, HistoryModel.PROP_TASK_ROLE));
            entryMap.put(DocumentHistoryConstants.TASK_OUTCOME.getValue(),
                    nodeService.getProperty(eventRef, HistoryModel.PROP_TASK_OUTCOME));
            entryMap.put(DocumentHistoryConstants.TASK_INSTANCE_ID.getValue(),
                    nodeService.getProperty(eventRef, HistoryModel.PROP_TASK_INSTANCE_ID));
            QName taskTypeValue = (QName) nodeService.getProperty(eventRef, HistoryModel.PROP_TASK_TYPE);
            entryMap.put(DocumentHistoryConstants.TASK_TYPE.getValue(), taskTypeValue != null ? taskTypeValue.toString()
                    : "");

            ArrayList<NodeRef> attachments = (ArrayList<NodeRef>) nodeService.getProperty(eventRef,
                    HistoryModel.PROP_TASK_ATTACHMENTS);
            entryMap.put(DocumentHistoryConstants.TASK_ATTACHMENTS.getValue(),
                    Optional.ofNullable(attachments).orElse(new ArrayList<>()));

            ArrayList<NodeRef> pooledActors = (ArrayList<NodeRef>) nodeService.getProperty(eventRef,
                    HistoryModel.PROP_TASK_POOLED_ACTORS);
            entryMap.put(DocumentHistoryConstants.TASK_POOLED_ACTORS.getValue(),
                    Optional.ofNullable(pooledActors).orElse(new ArrayList<>()));

            result.add(entryMap);
        }
        result.sort((firstMap, secondMap) -> {
            Long firstDate = (Long) firstMap.get(DocumentHistoryConstants.DOCUMENT_DATE.getValue());
            Long secondDate = (Long) secondMap.get(DocumentHistoryConstants.DOCUMENT_DATE.getValue());
            return firstDate.compareTo(secondDate);
        });
        return result;
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }
}
