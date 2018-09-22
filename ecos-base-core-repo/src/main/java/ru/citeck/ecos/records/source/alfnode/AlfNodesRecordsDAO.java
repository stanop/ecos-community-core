package ru.citeck.ecos.records.source.alfnode;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.alfnode.AlfNodeRecord;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.graphql.node.GqlAlfNode;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.query.RecordsResult;
import ru.citeck.ecos.records.query.RecordsQuery;
import ru.citeck.ecos.records.source.LocalRecordsDAO;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AlfNodesRecordsDAO extends LocalRecordsDAO {

    public static final String ID = "";

    private Map<String, AlfNodesSearch> searchByLanguage = new ConcurrentHashMap<>();

    public AlfNodesRecordsDAO() {
        setId(ID);
    }

    @Override
    public RecordsResult queryRecords(RecordsQuery query) {
        AlfNodesSearch alfNodesSearch = searchByLanguage.get(query.getLanguage());
        if (alfNodesSearch == null) {
            throw new IllegalArgumentException("Language " + query.getLanguage() +
                                               " is not supported! Query: " + query);
        }
        Long afterIdValue = null;
        if (query.isAfterIdMode()) {
            RecordRef afterId = query.getAfterId();
            if (afterId != null) {
                if (!ID.equals(afterId.getSourceId())) {
                    return new RecordsResult(query);
                }
                NodeRef afterIdNodeRef = new NodeRef(afterId.getId());
                afterIdValue = (Long) nodeService.getProperty(afterIdNodeRef, ContentModel.PROP_NODE_DBID);
            } else {
                afterIdValue = 0L;
            }
        }
        return alfNodesSearch.queryRecords(query, afterIdValue);
    }

    @Override
    public Optional<MetaValue> getMetaValue(GqlContext context, String id) {
        Optional<GqlAlfNode> node = context.getNode(id);
        return node.map(gqlAlfNode -> new AlfNodeRecord(gqlAlfNode, context));
    }

    public void register(AlfNodesSearch alfNodesSearch) {
        searchByLanguage.put(alfNodesSearch.getLanguage(), alfNodesSearch);
    }
}
