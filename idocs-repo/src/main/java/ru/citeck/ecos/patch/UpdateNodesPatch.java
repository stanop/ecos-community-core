package ru.citeck.ecos.patch;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

public class UpdateNodesPatch extends AbstractPatch {

    private static final Log logger = LogFactory.getLog(UpdateNodesPatch.class);

    private String name = UpdateNodesPatch.class.getSimpleName();

    private int threads = 1;
    private int batchSize = 1;

    private String parentPath;
    private String typeName;
    private String childAssoc;

    private NodeService nodeService;
    private NamespaceService namespaceService;
    private RetryingTransactionHelper retryingTransactionHelper;
    private DictionaryService dictionaryService;

    private BatchProcessWorker<NodeRef> worker;

    @Override
    protected String applyInternal() throws Exception {

        List<NodeRef> nodeRefs = getNodeRefs();

        if (nodeRefs.size() > 0) {

            BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>(
                    name,
                    retryingTransactionHelper,
                    new WorkProvider(nodeRefs),
                    threads, batchSize, null,
                    logger, Math.max(1, Math.round(nodeRefs.size() / 100f))
            );

            batchProcessor.process(worker, true);
        }

        return "Success";
    }

    private List<NodeRef> getNodeRefs() {

        QName nodesType = QName.resolveToQName(namespaceService, typeName);
        QName assocType = QName.resolveToQName(namespaceService, childAssoc);

        NodeRef parentRef = getParentRef();
        if (parentRef == null) {
            logger.error("Node not found by path " + parentPath);
            return Collections.emptyList();
        }

        Set<QName> types = new HashSet<>(dictionaryService.getSubTypes(nodesType, true));
        List<ChildAssociationRef> assocs = nodeService.getChildAssocs(parentRef, types);

        return assocs.stream().filter(a -> a.getTypeQName().equals(assocType))
                              .map(ChildAssociationRef::getChildRef)
                              .collect(Collectors.toList());
    }

    private NodeRef getParentRef() {
        NodeRef storeRoot = nodeService.getRootNode(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        List<NodeRef> refs = searchService.selectNodes(storeRoot, parentPath, null, namespaceService, false);
        return refs.size() > 0 ? refs.get(0) : null;
    }

    public void setWorker(BatchProcessWorker<NodeRef> worker) {
        this.worker = worker;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setChildAssoc(String childAssoc) {
        this.childAssoc = childAssoc;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public void setThreads(int threads) {
        this.threads = threads;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setParentPath(String parentPath) {
        this.parentPath = parentPath;
    }

    @Autowired
    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        nodeService = serviceRegistry.getNodeService();
        retryingTransactionHelper = serviceRegistry.getRetryingTransactionHelper();
        namespaceService = serviceRegistry.getNamespaceService();
        dictionaryService = serviceRegistry.getDictionaryService();
    }

    private static class WorkProvider implements BatchProcessWorkProvider<NodeRef> {

        private Collection<NodeRef> nodeRefs;
        private boolean hasMore = true;

        WorkProvider(Collection<NodeRef> nodeRefs) {
            this.nodeRefs = nodeRefs;
        }

        @Override
        public int getTotalEstimatedWorkSize() {
            return nodeRefs.size();
        }

        @Override
        public Collection<NodeRef> getNextWork() {
            if (hasMore) {
                hasMore = false;
                return nodeRefs;
            } else {
                return Collections.emptyList();
            }
        }
    }
}
