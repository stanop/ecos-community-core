package ru.citeck.ecos.records.bpm;

import com.fasterxml.jackson.databind.node.ObjectNode;
import graphql.annotations.annotationTypes.GraphQLID;
import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.beans.factory.annotation.Autowired;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.GraphQLMetaService;
import ru.citeck.ecos.graphql.meta.attribute.MetaAttribute;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.graphql.node.GqlAlfNode;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.RecordsUtils;
import ru.citeck.ecos.records.request.query.RecordsQuery;
import ru.citeck.ecos.records.request.query.RecordsResult;
import ru.citeck.ecos.records.source.AbstractRecordsDAO;
import ru.citeck.ecos.records.source.RecordsMetaDAO;
import ru.citeck.ecos.records.source.alfnode.meta.AlfNodeAtt;
import ru.citeck.ecos.records.source.alfnode.meta.AlfNodeAttValue;
import ru.citeck.ecos.records.source.alfnode.search.ChildrenAlfNodesSearch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class EcosBpmCatRecords extends AbstractRecordsDAO, RecordsMetaDAO {

    public static final String ID = "ebpmcat";

    private static final Map<String, String> FIELDS_MAPPING = new HashMap<String, String>() {{
        put("title", "cm:title");
        put("parent", "attr:parent");
    }};

    private GraphQLMetaService graphQLMetaService;

    public EcosBpmCatRecords() {
        setId(ID);
    }

    @Override
    public RecordsResult<RecordRef> getRecords(RecordsQuery query) {

        ChildrenAlfNodesSearch.Query childrenQuery = new ChildrenAlfNodesSearch.Query();
        childrenQuery.parent = "workspace://SpacesStore/ecos-bpm-category-root";
        childrenQuery.assocName = ContentModel.ASSOC_SUBCATEGORIES.toString();
        childrenQuery.recursive = true;

        RecordsQuery processNodesQuery = new RecordsQuery();
        processNodesQuery.setLanguage(ChildrenAlfNodesSearch.LANGUAGE);
        processNodesQuery.setQuery(childrenQuery);
        RecordsResult<RecordRef> result = recordsService.getRecords(processNodesQuery);

        return RecordsUtils.toScoped(ID, result);
    }

    @Override
    public List<ObjectNode> getMeta(List<RecordRef> records, String gqlSchema) {

        List<ObjectNode> meta = graphQLMetaService.getMeta(records, Category::new, gqlSchema);




        return null;
    }

    @Autowired
    public void setGraphQLMetaService(GraphQLMetaService graphQLMetaService) {
        this.graphQLMetaService = graphQLMetaService;
    }

    private static class Category implements MetaValue {

        private RecordRef id;
        private AlfNodeAttValue node;

        public Category(RecordRef id, GqlContext context) {
            this.id = id;
            node = new AlfNodeAttValue(new NodeRef(id.getId()), context);
        }

        @Override
        public @GraphQLID String id() {
            return id.getId();
        }

        @Override
        public Optional<MetaAttribute> att(String name) {
            if (FIELDS_MAPPING.containsKey(name)) {
                return node.att(FIELDS_MAPPING.get(name));
            }
            return Optional.empty();
        }
    }
}