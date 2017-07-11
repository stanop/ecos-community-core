package ru.citeck.ecos.history.impl;

import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import ru.citeck.ecos.constants.DocumentHistoryConstants;
import ru.citeck.ecos.model.HistoryModel;

import java.util.*;

/**
 * History get service
 */
public class HistoryGetServiceImpl implements HistoryGetService {

    /**
     * Logger
     */
    private static Log logger = LogFactory.getLog(HistoryGetServiceImpl.class);

    /**
     * Services
     */
    private NodeService nodeService;

    private PersonService personService;

    /**
     * Get history events by document refs
     * @param documentNodeRef Document node reference
     * @return List of transformed events (for web script)
     */
    public List<Map> getHistoryEventsByDocumentRef(NodeRef documentNodeRef) {
        /** Loaf associations */
        List<AssociationRef> associations = nodeService.getSourceAssocs(documentNodeRef, HistoryModel.ASSOC_DOCUMENT);
        List<Map> result = new ArrayList<>(associations.size());
        /** Create entries */
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
                    getInitiatorName(eventRef));
            result.add(entryMap);
        }
        Collections.sort(result, (firstMap, secondMap) -> {
            Long firstDate = (Long) firstMap.get(DocumentHistoryConstants.DOCUMENT_DATE.getValue());
            Long secondDate = (Long) secondMap.get(DocumentHistoryConstants.DOCUMENT_DATE.getValue());
            return firstDate.compareTo(secondDate);
        });
        return result;
    }

    /**
     * Get initiator name
     * @param eventRef Event node reference
     * @return Initiator username or null
     */
    private String getInitiatorName(NodeRef eventRef) {
        List<String> initiators = (List<String>) nodeService.getProperty(eventRef, HistoryModel.INITIATOR);
        String initiatorUUid = CollectionUtils.isNotEmpty(initiators) ? initiators.get(0) : "";
        if (StringUtils.isEmpty(initiatorUUid)) {
            return null;
        } else {
            return personService.getPerson(new NodeRef(initiatorUUid)).getUserName();
        }
    }

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }
}
