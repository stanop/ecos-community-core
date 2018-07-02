package ru.citeck.ecos.utils;

import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class NodeUtils {

    private static final String KEY_PENDING_DELETE_NODES = "DbNodeServiceImpl.pendingDeleteNodes";

    private NodeService nodeService;
    private DictionaryService dictionaryService;

    /**
     * Check is node pending for delete or not exist
     *
     * @param nodeRef Node reference
     * @return Check result
     */
    public boolean isValidNode(NodeRef nodeRef) {
        if (nodeRef == null || !nodeService.exists(nodeRef)) {
            return false;
        }
        if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_READ_WRITE) {
            return true;
        }
        return !TransactionalResourceHelper.getSet(KEY_PENDING_DELETE_NODES).contains(nodeRef);
    }

    /**
     * Get node property
     */
    public <T> T getProperty(NodeRef nodeRef, QName propName) {
        @SuppressWarnings("unchecked")
        T result = (T) nodeService.getProperty(nodeRef, propName);
        return result;
    }

    /**
     * Create association with specified node
     *
     * @return true if association was created or false if association already exists or arguments are invalid
     */
    public boolean createAssoc(NodeRef sourceRef, NodeRef targetRef, QName assocName) {

        if (!isValidNode(sourceRef) || !isValidNode(targetRef) || assocName == null) {
            return false;
        }
        AssociationDefinition assocDef = dictionaryService.getAssociation(assocName);
        if (assocDef != null) {
            List<NodeRef> storedRefs = getAssocsImpl(sourceRef, assocDef, true);
            if (!storedRefs.contains(targetRef)) {
                nodeService.createAssociation(sourceRef, targetRef, assocName);
                return true;
            }
        }
        return false;
    }

    /**
     * Remove association with specified target node
     *
     * @return true if association was removed or false if association not exists or arguments are invalid
     */
    public boolean removeAssoc(NodeRef sourceRef, NodeRef targetRef, QName assocName) {

        if (!isValidNode(sourceRef) || !isValidNode(targetRef) || assocName == null) {
            return false;
        }
        AssociationDefinition assocDef = dictionaryService.getAssociation(assocName);
        if (assocDef != null) {
            List<NodeRef> storedRefs = getAssocsImpl(sourceRef, assocDef, true);
            if (storedRefs.contains(targetRef)) {
                nodeService.removeAssociation(sourceRef, targetRef, assocName);
                return true;
            }
        }
        return false;
    }

    /**
     * Get first node associated as target or child of specified sourceRef
     *
     * @return associated node
     */
    public Optional<NodeRef> getAssocTarget(NodeRef sourceRef, QName assocName) {
        return getAssocTargets(sourceRef, assocName).stream().findFirst();
    }

    /**
     * Get nodes associated as target or child of specified sourceRef
     *
     * @return list of associated nodes or empty list if arguments is not valid
     */
    public List<NodeRef> getAssocTargets(NodeRef sourceRef, QName assocName) {
        return getAssocs(sourceRef, assocName, true);
    }

    /**
     * Get nodes associated as source or parent of specified targetRef
     *
     * @return list of associated nodes or empty list if arguments is not valid
     */
    public List<NodeRef> getAssocSources(NodeRef targetRef, QName assocName) {
        return getAssocs(targetRef, assocName, false);
    }

    private List<NodeRef> getAssocs(NodeRef nodeRef, QName assocName, boolean nodeIsSource) {
        if (isValidNode(nodeRef) && assocName != null) {
            AssociationDefinition assocDef = dictionaryService.getAssociation(assocName);
            if (assocDef != null) {
                return getAssocsImpl(nodeRef, assocDef, nodeIsSource);
            }
        }
        return Collections.emptyList();
    }

    private List<NodeRef> getAssocsImpl(NodeRef nodeRef, AssociationDefinition assocDef, boolean nodeIsSource) {

        if (assocDef.isChild()) {

            List<ChildAssociationRef> assocsRefs;

            if (nodeIsSource) {
                assocsRefs = nodeService.getChildAssocs(nodeRef, assocDef.getName(), RegexQNamePattern.MATCH_ALL);
            } else {
                assocsRefs = nodeService.getParentAssocs(nodeRef, assocDef.getName(), RegexQNamePattern.MATCH_ALL);
            }

            return assocsRefs.stream()
                             .map(r -> nodeIsSource ? r.getChildRef() : r.getParentRef())
                             .collect(Collectors.toList());
        } else {

            List<AssociationRef> assocsRefs;

            if (nodeIsSource) {
                assocsRefs = nodeService.getTargetAssocs(nodeRef, assocDef.getName());
            } else {
                assocsRefs = nodeService.getSourceAssocs(nodeRef, assocDef.getName());
            }

            return assocsRefs.stream()
                             .map(r -> nodeIsSource ? r.getTargetRef() : r.getSourceRef())
                             .collect(Collectors.toList());
        }
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        nodeService = serviceRegistry.getNodeService();
        dictionaryService = serviceRegistry.getDictionaryService();
    }
}
