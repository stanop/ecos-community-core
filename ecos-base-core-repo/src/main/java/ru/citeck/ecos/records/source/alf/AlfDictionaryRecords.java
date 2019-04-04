package ru.citeck.ecos.records.source.alf;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.AlfGqlContext;
import ru.citeck.ecos.records.RecordConstants;
import ru.citeck.ecos.records2.RecordMeta;
import ru.citeck.ecos.records2.RecordRef;
import ru.citeck.ecos.records2.graphql.GqlContext;
import ru.citeck.ecos.records2.graphql.meta.value.MetaEdge;
import ru.citeck.ecos.records2.graphql.meta.value.MetaField;
import ru.citeck.ecos.records2.graphql.meta.value.MetaValue;
import ru.citeck.ecos.records2.request.delete.RecordsDelResult;
import ru.citeck.ecos.records2.request.delete.RecordsDeletion;
import ru.citeck.ecos.records2.request.mutation.RecordsMutResult;
import ru.citeck.ecos.records2.request.mutation.RecordsMutation;
import ru.citeck.ecos.records2.source.dao.MutableRecordsDAO;
import ru.citeck.ecos.records2.source.dao.local.LocalRecordsDAO;
import ru.citeck.ecos.records2.source.dao.local.RecordsMetaLocalDAO;
import ru.citeck.ecos.utils.DictUtils;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AlfDictionaryRecords extends LocalRecordsDAO
                                  implements RecordsMetaLocalDAO<MetaValue>,
                                             MutableRecordsDAO {

    public static final String ID = "dict";

    private AlfNodesRecordsDAO alfNodesRecordsDAO;
    private NamespaceService namespaceService;

    @Autowired
    public AlfDictionaryRecords(AlfNodesRecordsDAO alfNodesRecordsDAO) {
        setId(ID);
        this.alfNodesRecordsDAO = alfNodesRecordsDAO;
    }

    @Override
    public List<MetaValue> getMetaValues(List<RecordRef> records) {

        return records.stream().map(r -> {
            QName typeName = QName.resolveToQName(namespaceService, r.getId());
            return new DictRecord(typeName, "alf_" + r.getId());

        }).collect(Collectors.toList());
    }

    @Override
    public RecordsMutResult mutate(RecordsMutation mutation) {

        RecordsMutation alfNodesMut = new RecordsMutation(mutation, m -> {
            RecordMeta alfNodeMeta = new RecordMeta(m, id -> RecordRef.EMPTY);
            alfNodeMeta.setAttribute(RecordConstants.ATT_TYPE, m.getId().getId());
            return alfNodeMeta;
        });
        return alfNodesRecordsDAO.mutate(alfNodesMut);
    }

    @Override
    public RecordsDelResult delete(RecordsDeletion deletion) {
        return new RecordsDelResult();
    }

    @Autowired
    public void setNamespaceService(NamespaceService namespaceService) {
        this.namespaceService = namespaceService;
    }

    public static class DictRecord implements MetaValue {

        private QName typeName;
        private String formKey;

        private AlfGqlContext context;
        private DictUtils dictUtils;

        DictRecord(QName typeName, String formKey) {
            this.formKey = formKey;
            this.typeName = typeName;
        }

        @Override
        public <T extends GqlContext> void init(T context, MetaField field) {
            this.context = (AlfGqlContext) context;
            this.dictUtils = this.context.getService(DictUtils.QNAME);
        }

        @Override
        public Object getAttribute(String name, MetaField field) {

            if (RecordConstants.ATT_FORM_KEY.equals(name)){
                return formKey;
            }

            return null;
        }

        @Override
        public String getDisplayName() {
            return dictUtils.getTypeTitle(typeName);
        }

        @Override
        public MetaEdge getEdge(String name, MetaField field) {
            return new AlfNodeMetaEdge(context, typeName, name, this);
        }
    }
}
