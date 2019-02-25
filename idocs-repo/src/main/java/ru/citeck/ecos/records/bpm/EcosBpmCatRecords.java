package ru.citeck.ecos.records.bpm;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import ru.citeck.ecos.graphql.GqlContext;
import ru.citeck.ecos.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records.RecordMeta;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.RecordsUtils;
import ru.citeck.ecos.records.request.query.RecordsQuery;
import ru.citeck.ecos.records.request.query.RecordsQueryResult;
import ru.citeck.ecos.records.request.result.RecordsResult;
import ru.citeck.ecos.records.source.dao.AbstractRecordsDAO;
import ru.citeck.ecos.records.source.dao.RecordsMetaDAO;
import ru.citeck.ecos.records.source.dao.RecordsQueryDAO;
import ru.citeck.ecos.records.source.alf.meta.AlfNodeAttValue;
import ru.citeck.ecos.records.source.alf.search.ChildrenAlfNodesSearch;

import java.util.*;

public class EcosBpmCatRecords extends AbstractRecordsDAO implements RecordsMetaDAO, RecordsQueryDAO {

    public static final String ID = "ebpmcat";

    private static final Map<String, String> FIELDS_MAPPING = new HashMap<String, String>() {{
        put("title", "cm:title");
        put("parent", "attr:parent");
    }};

    public EcosBpmCatRecords() {
        setId(ID);
    }

    @Override
    public RecordsQueryResult<RecordRef> getRecords(RecordsQuery query) {

        ChildrenAlfNodesSearch.Query childrenQuery = new ChildrenAlfNodesSearch.Query();
        childrenQuery.parent = "workspace://SpacesStore/ecos-bpm-category-root";
        childrenQuery.assocName = ContentModel.ASSOC_SUBCATEGORIES.toString();
        childrenQuery.recursive = true;

        RecordsQuery processNodesQuery = new RecordsQuery();
        processNodesQuery.setLanguage(ChildrenAlfNodesSearch.LANGUAGE);
        processNodesQuery.setQuery(childrenQuery);
        RecordsQueryResult<RecordRef> result = recordsService.getRecords(processNodesQuery);

        return RecordsUtils.toScoped(ID, result);
    }

    @Override
    public RecordsResult<RecordMeta> getMeta(List<RecordRef> records, String gqlSchema) {
        return null;
    }

    private static class Category implements MetaValue {

        private RecordRef id;
        private AlfNodeAttValue node;
        private GqlContext context;

        public Category(RecordRef id) {
            this.id = id;
        }

        @Override
        public MetaValue init(GqlContext context) {
            this.context = context;
            return this;
        }

        private AlfNodeAttValue getValue() {
            if (node == null) {
                node = new AlfNodeAttValue(new NodeRef(id.getId()));
            }
            return node;
        }

        @Override
        public String getId() {
            return id.getId();
        }

        @Override
        public List<MetaValue> getAttribute(String name) {
            if (FIELDS_MAPPING.containsKey(name)) {
                /*return getValue().getAttribute(FIELDS_MAPPING.get(name), context);*/
            }
            return Collections.emptyList();
        }

        @Override
        public String getString() {
            return id.toString();
        }
    }
}