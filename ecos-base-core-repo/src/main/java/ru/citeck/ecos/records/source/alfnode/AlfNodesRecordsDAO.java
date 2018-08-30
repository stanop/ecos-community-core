package ru.citeck.ecos.records.source.alfnode;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.alfresco.model.ContentModel;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.action.group.GroupAction;
import ru.citeck.ecos.action.group.GroupActionConfig;
import ru.citeck.ecos.action.group.GroupActionService;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.MetaProvider;
import ru.citeck.ecos.graphql.meta.alfnode.AlfNodeRecord;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.graphql.node.GqlAlfNode;
import ru.citeck.ecos.records.AttributeInfo;
import ru.citeck.ecos.records.RecordsService;
import ru.citeck.ecos.records.query.DaoRecordsResult;
import ru.citeck.ecos.records.query.RecordsQuery;
import ru.citeck.ecos.records.source.AbstractRecordsDAO;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class AlfNodesRecordsDAO extends AbstractRecordsDAO {

    public static final String ID = "AlfrescoNode";
    public static final String META_BASE_QUERY = "record(source:\"" + ID + "\",id:\"%s\")";

    private NodeService nodeService;
    private GroupActionService groupActionService;

    private Map<String, AlfNodesSearch> searchByLanguage = new ConcurrentHashMap<>();

    private MetaProvider metaProvider;

    @Autowired
    public AlfNodesRecordsDAO(RecordsService recordsService,
                              ServiceRegistry serviceRegistry,
                              GroupActionService groupActionService) {
        super(ID);
        this.nodeService = serviceRegistry.getNodeService();
        this.groupActionService = groupActionService;
        recordsService.register(this);
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
    public Map<String, ObjectNode> queryMeta(Collection<String> records, String gqlSchema) {
        return metaProvider.queryMeta(META_BASE_QUERY, records, gqlSchema);
    }

    @Override
    public <V> Map<String, V> queryMeta(Collection<String> records, Class<V> metaClass) {
        return metaProvider.queryMeta(META_BASE_QUERY, records, metaClass);
    }

    @Override
    public Optional<MetaValue> getMetaValue(GqlContext context, String id) {
        Optional<GqlAlfNode> node = context.getNode(id);
        return node.map(gqlAlfNode -> new AlfNodeRecord(gqlAlfNode, context));
    }

    @Override
    public GroupAction<String> createAction(String actionId, GroupActionConfig config) {
        return groupActionService.createAction(actionId, config);
    }

    public void register(AlfNodesSearch alfNodesSearch) {
        searchByLanguage.put(alfNodesSearch.getLanguage(), alfNodesSearch);
    }

    public void setMetaProvider(MetaProvider metaProvider) {
        this.metaProvider = metaProvider;
    }
}
