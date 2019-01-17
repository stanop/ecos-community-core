package ru.citeck.ecos.formio;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.citeck.ecos.formio.model.FormioForm;
import ru.citeck.ecos.records.RecordConstants;
import ru.citeck.ecos.records.RecordMeta;
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
    protected RecordsQueryResult<FormWithData> getMetaValues(RecordsQuery recordsQuery) {

        RecordsQueryResult<FormWithData> result = new RecordsQueryResult<>();

        Query query = recordsQuery.getQuery(Query.class);

        List<String> formKeys = new ArrayList<>();

        if (StringUtils.isNotBlank(query.formKey)) {

            formKeys.addAll(Arrays.asList(query.formKey.split(",")));

        } else if (query.record != null) {

            JsonNode recFormKeys = recordsService.getAttribute(query.record, RecordConstants.ATT_FORM_KEYS);

            for (JsonNode key : recFormKeys) {
                formKeys.add(key.asText());
            }

            if (formKeys.isEmpty()) {
                formKeys.add(query.record.toString());
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

        FormioForm form = optForm.get();
        ObjectNode attributes = null;

        if (query.record != null && form.getDefinition() != null) {

            JsonNode components = form.getDefinition().path("components");
            Map<String, String> attributesToRequest = fillAttributes(components, new HashMap<>());

            RecordMeta meta = recordsService.getAttributes(query.record, attributesToRequest);
            attributes = meta.getAttributes();
        }

        FormWithData formWithData = new FormWithData(form, new FormSubmission(attributes));
        result.setRecords(Collections.singletonList(formWithData));
        result.setTotalCount(1);

        return result;
    }

    private Map<String, String> fillAttributes(JsonNode components, Map<String, String> attributes) {

        for (JsonNode component : components) {

            if (component.path("input").asBoolean() && !"button".equals(component.path("type").asText())) {

                String fieldKey = component.path("key").asText();
                if (StringUtils.isBlank(fieldKey)) {
                    continue;
                }

                JsonNode compAttribute = component.path("properties").path("attribute");
                String attribute = null;
                if (!compAttribute.isMissingNode() && !compAttribute.isNull()) {
                    attribute = compAttribute.asText();
                }
                if (StringUtils.isBlank(attribute)) {
                    attribute = fieldKey;
                }

                attributes.put(fieldKey, attribute);
            }

            fillAttributes(components.path("components"), attributes);
        }

        return attributes;
    }

    public static class FormWithData {

        private FormioForm form;
        @Getter private FormSubmission submission;

        FormWithData(FormioForm form, FormSubmission submission) {
            this.form = form;
            this.submission = submission;
        }

        public String getId() {
            return form.getId();
        }

        public String getFormKey() {
            return form.getFormKey();
        }

        public JsonNode getDefinition() {
            return form.getDefinition();
        }
    }

    public static class FormSubmission {

        @Getter private ObjectNode data;

        public FormSubmission(ObjectNode data) {
            this.data = data != null ? data : JsonNodeFactory.instance.objectNode();
        }
    }

    static class Query {
        @Getter @Setter private RecordRef record;
        @Getter @Setter private String formKey;
    }
}
