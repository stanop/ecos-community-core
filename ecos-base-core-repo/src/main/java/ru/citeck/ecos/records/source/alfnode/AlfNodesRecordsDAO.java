package ru.citeck.ecos.records.source.alfnode;

import com.fasterxml.jackson.databind.JsonNode;
import graphql.ExecutionResult;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.GraphQLService;
import ru.citeck.ecos.graphql.meta.GqlMetaUtils;
import ru.citeck.ecos.graphql.meta.alfnode.AlfNodeRecord;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.graphql.node.GqlAlfNode;
import ru.citeck.ecos.records.AttributeInfo;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.query.DaoRecordsResult;
import ru.citeck.ecos.records.query.RecordsQuery;
import ru.citeck.ecos.records.source.AbstractRecordsDAO;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AlfNodesRecordsDAO extends AbstractRecordsDAO {

    public static final String ID = "";
    public static final String META_BASE_QUERY = "records(source:\"" + ID + "\",refs:[\"%s\"])";

    private NodeService nodeService;
    private GqlMetaUtils gqlMetaUtils;
    private GraphQLService graphQLService;

    private Map<String, AlfNodesSearch> searchByLanguage = new ConcurrentHashMap<>();

    @Autowired
    public AlfNodesRecordsDAO(ServiceRegistry serviceRegistry,
                              GraphQLService graphQLService,
                              GqlMetaUtils gqlMetaUtils) {
        setId(ID);
        this.nodeService = serviceRegistry.getNodeService();
        this.gqlMetaUtils = gqlMetaUtils;
        this.graphQLService = graphQLService;
    }

    @Override
    public DaoRecordsResult queryRecords(RecordsQuery query) {
        AlfNodesSearch alfNodesSearch = searchByLanguage.get(query.getLanguage());
        if (alfNodesSearch == null) {
            throw new IllegalArgumentException("Language " + query.getLanguage() +
                                               " is not supported! Query: " + query);
        }
        Long afterIdValue = null;
        if (query.isAfterIdMode()) {
            String afterId = query.getAfterId();
            if (afterId != null) {
                NodeRef afterIdNodeRef = new NodeRef(afterId);
                afterIdValue = (Long) nodeService.getProperty(afterIdNodeRef, ContentModel.PROP_NODE_DBID);
            } else {
                afterIdValue = 0L;
            }
        }
        return alfNodesSearch.queryRecords(query, afterIdValue);
    }

    @Override
    public Optional<AttributeInfo> getAttributeInfo(String name) {
        return Optional.empty();
    }

    @Override
    public Map<String, JsonNode> queryMeta(Collection<String> records, String gqlSchema) {
        List<String> recordsRefs = new ArrayList<>(records);
        String query = gqlMetaUtils.createQuery(META_BASE_QUERY, recordsRefs, gqlSchema);
        ExecutionResult executionResult = graphQLService.execute(query);
        return gqlMetaUtils.convertMeta(recordsRefs, executionResult);
    }

    @Override
    public <V> Map<String, V> queryMeta(Collection<String> records, Class<V> metaClass) {
        if (NodeRef.class.isAssignableFrom(metaClass)) {
            Map<String, NodeRef> results = new HashMap<>();
            records.forEach(r -> {
                String nodeRefStr;
                int sourceDelimIdx = r.indexOf(RecordRef.SOURCE_DELIMITER);
                if (sourceDelimIdx > -1) {
                    nodeRefStr = r.substring(sourceDelimIdx + 1);
                } else {
                    nodeRefStr = r;
                }
                if (NodeRef.isNodeRef(nodeRefStr)) {
                    results.put(r, new NodeRef(nodeRefStr));
                } else {
                    results.put(r, null);
                }
            });
            return (Map<String, V>) results;
        }
        List<String> recordsRefs = new ArrayList<>(records);
        String query = gqlMetaUtils.createQuery(META_BASE_QUERY, recordsRefs, metaClass);
        ExecutionResult executionResult = graphQLService.execute(query);
        return gqlMetaUtils.convertMeta(recordsRefs, executionResult, metaClass);
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
