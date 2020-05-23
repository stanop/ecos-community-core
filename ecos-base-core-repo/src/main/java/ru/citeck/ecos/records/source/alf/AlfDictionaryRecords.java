package ru.citeck.ecos.records.source.alf;

import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.graphql.AlfGqlContext;
import ru.citeck.ecos.records2.QueryContext;
import ru.citeck.ecos.records2.RecordConstants;
import ru.citeck.ecos.records2.RecordMeta;
import ru.citeck.ecos.records2.RecordRef;
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
            return new DictRecord(typeName, r.getId(), "alf_" + r.getId());

        }).collect(Collectors.toList());
    }

    @Override
    public RecordsMutResult mutateImpl(RecordsMutation mutation) {

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

        private QName fullName;
        private String formKey;
        private String shortName;

        private AlfGqlContext context;
        private DictUtils dictUtils;

        DictRecord(QName fullName, String shortName, String formKey) {
            this.formKey = formKey;
            this.fullName = fullName;
            this.shortName = shortName;
        }

        @Override
        public String getId() {
            return shortName;
        }

        @Override
        public <T extends QueryContext> void init(T context, MetaField field) {
            this.context = (AlfGqlContext) context;
            this.dictUtils = this.context.getService(DictUtils.QNAME);
        }

        @Override
        public Object getAttribute(String name, MetaField field) {

            switch (name) {
                case RecordConstants.ATT_FORM_KEY:
                    return formKey;
                case RecordConstants.ATT_FORM_MODE:
                    return RecordConstants.FORM_MODE_CREATE;
                default:
                    return null;
            }
        }

        @Override
        public String getDisplayName() {
            return dictUtils.getTypeTitle(fullName);
        }

        @Override
        public MetaEdge getEdge(String name, MetaField field) {
            return new AlfNodeMetaEdge(context, fullName, name, this);
        }
    }
}
