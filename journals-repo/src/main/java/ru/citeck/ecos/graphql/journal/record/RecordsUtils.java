package ru.citeck.ecos.graphql.journal.record;

import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.alfnode.AlfNodeRecord;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.source.alfnode.AlfNodesRecordsDAO;
import ru.citeck.ecos.records.RecordRef;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

@Component
public class RecordsUtils {

    private NodeService nodeService;

    @Autowired
    public RecordsUtils(ServiceRegistry serviceRegistry) {
        this.nodeService = serviceRegistry.getNodeService();
    }

    public List<MetaValue> wrapRecords(GqlContext context, Iterable<ru.citeck.ecos.records.RecordRef> recordRefs) {
        return wrapNodeRefs(context, recordRefs, recordRef -> {
            if (AlfNodesRecordsDAO.ID.equals(recordRef.getSourceId())) {
                return new NodeRef(recordRef.getId());
            }
            return null;
        });
    }

    public List<MetaValue> wrapNodeRefs(GqlContext context, Iterable<NodeRef> nodeRefs) {
        return wrapNodeRefs(context, nodeRefs, n -> n);
    }

    private <T> List<MetaValue> wrapNodeRefs(GqlContext context,
                                                      Iterable<T> records,
                                                      Function<T, NodeRef> converter) {
        if (records == null) {
            return Collections.emptyList();
        }

        List<MetaValue> result = new ArrayList<>();

        for (T record : records) {
            context.getNode(converter.apply(record))
                    .ifPresent(n -> result.add(new AlfNodeRecord(n, context)));
        }

        return result;
    }

    public List<MetaValue> wrapRefsToLocalValue(GqlContext context, Iterable<RecordRef> remoteRefs) {
        if (remoteRefs == null) {
            return Collections.emptyList();
        }

        List<MetaValue> records = new ArrayList<>();

        /*remoteRefs.forEach(item -> {
            if (item.isLocal()) {
                context.getNode(item.getNodeRef())
                        .ifPresent(nodeRef -> records.add(new AlfNodeRecord(nodeRef, context)));
            }
        });*/

        return records;
    }

    public Long getRecordDbId(String recordId) {
        if (StringUtils.isNotBlank(recordId)) {
            RecordRef ref = new RecordRef(recordId);
            /*if (ref.isLocal()) {
                return (Long) nodeService.getProperty(ref.getNodeRef(), ContentModel.PROP_NODE_DBID);
            } else {
                throw new IllegalArgumentException("Record ID is not local! recordId: " + recordId);
            }*/
        } else {
        }
        return -1L;
    }
}
