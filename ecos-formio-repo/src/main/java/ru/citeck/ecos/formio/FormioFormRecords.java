package ru.citeck.ecos.formio;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
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
                                               .map(key -> formioFormService.getForm(key, query.isViewMode))
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

    static class Query {
        @Getter @Setter private RecordRef record;
        @Getter @Setter private String formKey;
        @Getter @Setter private Boolean isViewMode;
    }
}
