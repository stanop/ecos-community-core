package ru.citeck.ecos.utils;

import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;

import java.util.Set;

/**
 * Node utils
 */
public class NodeUtils {

    /**
     * Constants
     */
    public static final String KEY_PENDING_DELETE_NODES = "DbNodeServiceImpl.pendingDeleteNodes";

    /**
     * Check is node pending for delete or not exist
     * @param nodeRef Node reference
     * @param nodeService Node service
     * @return Check result
     */
    public static boolean isNodeForDeleteOrNotExist(NodeRef nodeRef, NodeService nodeService) {
        if (!nodeService.exists(nodeRef)) {
            return true;
        }
        if(AlfrescoTransactionSupport.getTransactionReadState() != AlfrescoTransactionSupport.TxnReadState.TXN_READ_WRITE) {
            return false;
        } else {
            Set<NodeRef> nodesPendingDelete = TransactionalResourceHelper.getSet(KEY_PENDING_DELETE_NODES);
            return nodesPendingDelete.contains(nodeRef);
        }
    }
}
