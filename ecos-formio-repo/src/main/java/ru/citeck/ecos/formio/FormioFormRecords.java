package ru.citeck.ecos.formio;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.formio.model.FormioForm;
import ru.citeck.ecos.records.RecordConstants;
import ru.citeck.ecos.records.RecordRef;
import ru.citeck.ecos.records.RecordsService;
import ru.citeck.ecos.records.request.query.RecordsQuery;
import ru.citeck.ecos.records.request.query.RecordsQueryResult;
import ru.citeck.ecos.records.source.LocalRecordsDAO;
import ru.citeck.ecos.records.source.RecordsMetaDAO;
import ru.citeck.ecos.records.source.RecordsWithMetaDAO;

import java.util.*;

@Component
public class FormioFormRecords extends LocalRecordsDAO
                               implements RecordsWithMetaDAO,
                                          RecordsMetaDAO {

    public static final String ID = "formio";
/*
    private static final Map<String, String> TO_ALF_NODE_MAPPING;

    static {
        Map<String, String> localToAlfNodeMapping = new HashMap<>();
        localToAlfNodeMapping.put("title", "cm:title");
        localToAlfNodeMapping.put("formId", "ecosFormio:formId");
        localToAlfNodeMapping.put("formKey", "ecosFormio:formKey");
        localToAlfNodeMapping.put("formType", "ecosFormio:formType");
        localToAlfNodeMapping.put("description", "cm:description");
        TO_ALF_NODE_MAPPING = Collections.unmodifiableMap(localToAlfNodeMapping);
    }*/

    private RecordsService recordsService;
    private FormioFormService formioFormService;

    @Autowired
    public FormioFormRecords(RecordsService recordsService,
                             FormioFormService formioFormService) {
        setId(ID);
        this.recordsService = recordsService;
        this.formioFormService = formioFormService;
    }

    @Override
    protected RecordsQueryResult<FormioForm> getMetaValues(RecordsQuery recordsQuery) {

        RecordsQueryResult<FormioForm> result = new RecordsQueryResult<>();

        Query query = recordsQuery.getQuery(Query.class);

        List<String> formKeys = new ArrayList<>();

        if (query.formKey != null) {

            formKeys.addAll(Arrays.asList(query.formKey.split(",")));

        } else if (query.record != null) {

            JsonNode recFormKeys = recordsService.getAttribute(query.record, RecordConstants.ATT_FORM_KEYS);

            for (JsonNode key : recFormKeys) {
                formKeys.add(key.asText());
            }
        }

        if (formKeys.isEmpty()) {
            return result;
        }

        Optional<FormioForm> optForm = formKeys.stream()
                                               .map(formioFormService::getForm)
                                               .filter(Optional::isPresent)
                                               .map(Optional::get)
                                               .findFirst();

        if (!optForm.isPresent()) {
            return result;
        }

        result.setRecords(Collections.singletonList(optForm.get()));
        result.setTotalCount(1);

        return result;
    }

    /*@Override
    public RecordsMutResult mutate(RecordsMutation mutation) {

        RepoContentDAO<FormioFormModel> contentDAO = formioFormService.getContentDAO();

        for (RecordMeta record : mutation.getRecords()) {

            ObjectNode attributes = record.getAttributes();

            RecordRef id = record.getId();

            if (id != null) {
                record.setId(new RecordRef(formioFormService.toNodeRef(id)));
            } else {
                attributes.put(RecordConstants.ATT_PARENT, contentDAO.getRootRef().toString());
                attributes.put(RecordConstants.ATT_PARENT_ATT, toPrefix(contentDAO.getChildAssocType()));
                attributes.put(RecordConstants.ATT_TYPE, toPrefix(contentDAO.getConfigNodeType()));
            }

            TO_ALF_NODE_MAPPING.forEach((local, alfAtt) -> {

                JsonNode value = attributes.remove(local);
                if (value != null) {
                    attributes.put(alfAtt, value);
                }
            });
        }

        RecordsMutResult result = alfNodesRecordsDAO.mutate(mutation);

        result.getRecords().forEach(record -> {
            String localId = record.getId().getId().replace("workspace://SpacesStore/", "");
            record.setId(new RecordRef(ID, localId));
        });

        return result;
    }

    *//*target=formio&record=*//*

    @Override
    public RecordsDelResult delete(RecordsDeletion deletion) {

        deletion.setRecords(deletion.getRecords().stream().map(r ->
            new RecordRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE + r.getId())
        ).collect(Collectors.toList()));

        RecordsDelResult result = alfNodesRecordsDAO.delete(deletion);
        result.getRecords().forEach(r -> r.setId(new RecordRef(ID, r.getId())));

        return result;
    }

    private String toPrefix(QName name) {
        return name.toPrefixString(namespaceService);
    }
*/
    static class Query {
        public RecordRef record;
        public String formKey;
    }
}
