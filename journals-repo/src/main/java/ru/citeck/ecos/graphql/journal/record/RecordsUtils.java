package ru.citeck.ecos.graphql.journal.record;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.journal.datasource.alfnode.AlfNodeRecord;
import ru.citeck.ecos.repo.RemoteRef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class RecordsUtils {

    private NodeService nodeService;

    @Autowired
    public RecordsUtils(ServiceRegistry serviceRegistry) {
        this.nodeService = serviceRegistry.getNodeService();
    }

    public List<JGqlAttributeValue> wrapToAttValue(GqlContext context, Iterable<NodeRef> nodeRefs) {

        if (nodeRefs == null) {
            return Collections.emptyList();
        }

        List<JGqlAttributeValue> records = new ArrayList<>();

        for (NodeRef nodeRef : nodeRefs) {
            context.getNode(nodeRef)
                    .ifPresent(n -> records.add(new AlfNodeRecord(n, context)));
        }

        return records;
    }

    public Long getRecordDbId(String recordId) {
        if (StringUtils.isNotBlank(recordId)) {
            RemoteRef ref = new RemoteRef(recordId);
            if (ref.isLocal()) {
                return (Long) nodeService.getProperty(ref.getNodeRef(), ContentModel.PROP_NODE_DBID);
            } else {
                throw new IllegalArgumentException("Record ID is not local! recordId: " + recordId);
            }
        } else {
            return -1L;
        }
    }
}
